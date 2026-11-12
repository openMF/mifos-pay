/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.datastore

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import kpt.core.base.crypto.FieldEncryptor
import kpt.core.base.crypto.SecureKeyProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

/** Marks a value as AES-GCM ciphertext. Its ABSENCE marks a pre-encryption cleartext value. */
private const val ENC_PREFIX = "kpt.enc.v1:"

/**
 * Desktop secure settings — every VALUE is AES-GCM encrypted at rest, with a one-time in-place
 * upgrade for stores written before encryption existed.
 *
 * This file previously claimed to be "an AES-encrypted properties file" while writing plain
 * `Properties`: the passcode and auth token sat in `~/.mifos-secure/secure_settings.properties` in
 * cleartext, under a type named `secureSettings`, with a KDoc asserting encryption that did not
 * exist. The claim was the dangerous part — a reader had no reason to look.
 *
 * Encryption reuses [FieldEncryptor] (AES-GCM, BouncyCastle) with the key from [SecureKeyProvider],
 * rather than a second crypto path. VALUES are encrypted; KEY NAMES stay clear so the file stays
 * debuggable and `PropertiesSettings` can index it.
 *
 * ## Why the prefix
 * Ciphertext here is Base64 and legacy cleartext is arbitrary JSON, so "did this decrypt?" cannot
 * distinguish a pre-encryption value from a corrupt one — an earlier cut of this class dropped both,
 * which silently signed every existing desktop user out. [ENC_PREFIX] makes the format
 * self-describing: prefixed means ciphertext (a decrypt failure is then genuinely corrupt or a
 * rotated key, and dropping it is right), unprefixed means legacy cleartext, which is read as-is and
 * re-encrypted. No secret is lost and no guess is made.
 *
 * The upgrade is eager: if any legacy value is present the whole file is rewritten encrypted on
 * first load, so the cleartext window is one launch rather than "until that preference is next
 * written". The rewrite goes through a temp file and an atomic move, because it is rewriting the
 * only copy of the user's secrets and a crash mid-write would destroy them.
 *
 * ## Threat model — read this before relying on it
 * The key lives in `~/.mifos-secure/field_key.bin`, protected by filesystem permissions alone. This
 * defends against casual disclosure — backups, sync folders, support bundles — and NOT against an
 * attacker who can already read arbitrary files as this user, since they can read the key too.
 * Genuine protection needs the OS credential store (macOS Keychain / Linux libsecret / Windows
 * DPAPI), which is the deferred Phase 4 (T18) work. A real limitation, stated rather than implied.
 */
actual class SecureSettingsFactory {
    private val secureDir: File by lazy {
        File(System.getProperty("user.home"), ".mifos-secure").apply { mkdirs() }
    }
    private val secureFile: File by lazy { File(secureDir, "secure_settings.properties") }
    private val encryptor: FieldEncryptor by lazy { FieldEncryptor(SecureKeyProvider()) }

    /** `create` on Desktop (JVM). */
    actual fun create(): Settings {
        val onDisk = Properties()
        if (secureFile.exists()) {
            secureFile.inputStream().use { onDisk.load(it) }
        }

        val plain = Properties()
        var sawLegacy = false
        onDisk.stringPropertyNames().forEach { name ->
            val stored = onDisk.getProperty(name) ?: return@forEach
            if (stored.startsWith(ENC_PREFIX)) {
                // Prefixed => ciphertext. A failure here is a rotated key or corruption, so dropping
                // is correct: returning undecryptable bytes as a passcode would be worse.
                runCatching { encryptor.decrypt(stored.removePrefix(ENC_PREFIX)) }
                    .getOrNull()?.let { plain.setProperty(name, it) }
            } else {
                // Unprefixed => written before encryption existed. Keep it, do not drop it.
                plain.setProperty(name, stored)
                sawLegacy = true
            }
        }

        if (sawLegacy) persist(plain)

        return PropertiesSettings(plain) { updated -> persist(updated) }
    }

    /** Encrypt every value and replace the file atomically. */
    private fun persist(values: Properties) {
        val encrypted = Properties()
        values.stringPropertyNames().forEach { name ->
            values.getProperty(name)?.let { encrypted.setProperty(name, ENC_PREFIX + encryptor.encrypt(it)) }
        }
        secureDir.mkdirs()
        val tmp = File(secureDir, "secure_settings.properties.tmp")
        tmp.outputStream().use { encrypted.store(it, "AES-GCM encrypted — do not hand-edit") }
        restrictToOwner(tmp)
        runCatching {
            Files.move(
                tmp.toPath(),
                secureFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        }.onFailure {
            // Filesystems without atomic move (some network mounts) — fall back to a plain replace.
            Files.move(tmp.toPath(), secureFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        restrictToOwner(secureFile)
    }

    /** Best-effort owner-only. POSIX-dependent, so it is a hardening step and not the defence. */
    private fun restrictToOwner(file: File) {
        runCatching {
            file.setReadable(false, false)
            file.setWritable(false, false)
            file.setReadable(true, true)
            file.setWritable(true, true)
        }
    }
}
