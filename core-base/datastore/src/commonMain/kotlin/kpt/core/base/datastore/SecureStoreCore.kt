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

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

/** Marks a value as AES-GCM ciphertext. Its ABSENCE marks a pre-encryption cleartext value. */
internal const val ENC_PREFIX = "kpt.enc.v1:"

/** Single storage entry holding the whole encrypted store. */
internal const val BLOB_KEY = "kpt.secure.v1"

/**
 * Seam over the browser key-value store.
 *
 * It exists so the store's LOGIC — legacy adoption, prefix discrimination, degraded mode — carries
 * no platform dependency. That is what lets this file live in commonMain and be tested on desktop,
 * iOS and JS alike, rather than only where a browser exists. The browser-backed implementation is
 * `BrowserStorage` in jsCommonMain.
 */
internal interface SecureBlobStorage {
    fun get(key: String): String?
    fun set(key: String, value: String)
    fun remove(key: String)
    fun keys(): List<String>
}

/** Seam over WebCrypto. [warmUp] returns false when crypto is unavailable rather than throwing. */
internal interface SecureCipher {
    suspend fun warmUp(): Boolean
    suspend fun encrypt(plaintext: String): String
    suspend fun decrypt(ciphertext: String): String
}

/**
 * The web secure store's behaviour, independent of the browser.
 *
 * Reads are synchronous off [cache]; persistence is asynchronous because WebCrypto is. See
 * [WebSecureStore] for how that is bridged to the synchronous `SecureSettingsFactory.create()`.
 */
internal class SecureStoreCore(
    private val storage: SecureBlobStorage,
    private val cipher: SecureCipher,
) {

    internal val cache = linkedMapOf<String, String>()

    /** True once [warmUp] has run. [settings] refuses to hand out a view before then. */
    var warmed: Boolean = false
        private set

    /** True when crypto was unavailable: the store works in memory and persists NOTHING. */
    var degraded: Boolean = false
        private set

    /**
     * Loads the key, decrypts the store, and adopts any pre-encryption plaintext. Idempotent.
     *
     * [legacyKeyPrefixes] scopes the one-time adoption. The scoping is load-bearing: the web `plain`
     * and `secure` stores historically shared one storage namespace, so adopting everything would
     * swallow the plain store's keys and then encrypt them, leaving the plain store reading
     * ciphertext it cannot decode.
     */
    suspend fun warmUp(legacyKeyPrefixes: List<String>) {
        if (warmed) return

        if (!cipher.warmUp()) {
            // Crypto unavailable (some private-browsing modes, insecure origin). Degrade to
            // memory-only: the app keeps working and the user re-authenticates next session.
            // Falling back to plaintext would be the exact defect this store was written to remove.
            degraded = true
            warmed = true
            return
        }

        val blob = storage.get(BLOB_KEY)
        if (blob != null && blob.startsWith(ENC_PREFIX)) {
            // The prefix already told us this is ciphertext, so a failure here is a rotated key or
            // corruption — never legacy plaintext. Starting empty is the honest outcome.
            runCatching { cipher.decrypt(blob.removePrefix(ENC_PREFIX)) }
                .getOrNull()
                ?.let { cache.putAll(Json.decodeFromString<Map<String, String>>(it)) }
            warmed = true
        } else {
            val adopted = adoptLegacyPlaintext(legacyKeyPrefixes)
            warmed = true
            // Re-encrypt eagerly so the cleartext window is this one launch, not "until the user
            // next changes a preference".
            if (adopted) flushNow()
        }
    }

    /** Moves pre-encryption plaintext into the cache and deletes it from storage. */
    private fun adoptLegacyPlaintext(prefixes: List<String>): Boolean {
        val legacy = storage.keys().filter { k -> prefixes.any { k == it || k.startsWith("$it.") } }
        legacy.forEach { k -> storage.get(k)?.let { cache[k] = it } }
        legacy.forEach { storage.remove(it) }
        return legacy.isNotEmpty()
    }

    /** Encrypts the whole cache and replaces the stored blob. No-op while [degraded]. */
    suspend fun flushNow() {
        if (degraded) return
        val json = Json.encodeToString<Map<String, String>>(cache)
        storage.set(BLOB_KEY, ENC_PREFIX + cipher.encrypt(json))
    }

    /** A synchronous view over the warmed cache. */
    fun settings(onMutate: () -> Unit): Settings {
        check(warmed) {
            "Web secure settings used before warm-up. Call SecureSettingsFactory.warmUp() and let " +
                "it complete BEFORE initKoin() in your web entry point — see cmp-web Main.kt."
        }
        return CachedSecureSettings(cache, onMutate)
    }
}
