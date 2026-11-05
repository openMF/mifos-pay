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
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kpt.core.base.crypto.WebSecureCrypto

/**
 * Coalesces the many `putX` calls that one `encodeValue` produces into a single encrypt + write.
 * Serializing an object flattens it into one entry per field, so without this a single preference
 * save would trigger a dozen AES operations and a dozen storage writes.
 */
private const val FLUSH_DEBOUNCE_MS = 50L

/** [SecureBlobStorage] over the browser's localStorage. */
private object BrowserStorage : SecureBlobStorage {
    override fun get(key: String): String? = localStorage.getItem(key)
    override fun set(key: String, value: String) = localStorage.setItem(key, value)
    override fun remove(key: String) = localStorage.removeItem(key)
    override fun keys(): List<String> =
        (0 until localStorage.length).mapNotNull { localStorage.key(it) }
}

/** [SecureCipher] over WebCrypto. A failed warm-up degrades the store instead of throwing. */
private object WebCryptoCipher : SecureCipher {
    private val crypto = WebSecureCrypto()
    override suspend fun warmUp(): Boolean = runCatching { crypto.warmUp() }.isSuccess
    override suspend fun encrypt(plaintext: String): String = crypto.encrypt(plaintext)
    override suspend fun decrypt(ciphertext: String): String = crypto.decrypt(ciphertext)
}

/**
 * Process-wide backing store for web secure settings.
 *
 * ## Why the state is process-wide rather than per-instance
 * `SecureSettingsFactory.create()` is SYNCHRONOUS (it is the same `expect` every platform
 * implements) while WebCrypto is async-only. The bridge is to do the async work ONCE at startup —
 * [warmUp] — and let `create()` hand out a synchronous view over the already-decrypted cache. Koin
 * builds `Settings` lazily and long after boot, so the warmed state has to outlive any one factory.
 *
 * ## Reads are synchronous, writes are asynchronous
 * Reads hit the in-memory cache. Writes update the cache immediately (so a read-after-write is
 * always correct) and schedule a debounced encrypt-and-persist. A tab closed within
 * [FLUSH_DEBOUNCE_MS] of a write can lose that last write — a real, bounded limitation, and the
 * cost of putting a synchronous interface over an asynchronous crypto API.
 *
 * The behaviour itself lives in [SecureStoreCore] so it can be tested without a browser.
 */
internal object WebSecureStore {

    private val core = SecureStoreCore(BrowserStorage, WebCryptoCipher)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var flushJob: Job? = null

    suspend fun warmUp(legacyKeyPrefixes: List<String>) {
        core.warmUp(legacyKeyPrefixes)
        if (core.degraded) {
            println(
                "[kpt] SECURE STORAGE UNAVAILABLE - WebCrypto/IndexedDB could not be initialised. " +
                    "Secure preferences are memory-only for this session and will NOT persist. " +
                    "Nothing is written in plaintext.",
            )
        }
    }

    fun settings(): Settings = core.settings(::scheduleFlush)

    private fun scheduleFlush() {
        flushJob?.cancel()
        flushJob = scope.launch {
            delay(FLUSH_DEBOUNCE_MS)
            core.flushNow()
        }
    }
}
