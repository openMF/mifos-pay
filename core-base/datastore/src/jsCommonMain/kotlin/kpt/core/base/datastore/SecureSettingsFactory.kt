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

/**
 * Web secure settings — AES-GCM via WebCrypto under a **non-extractable** key in IndexedDB.
 *
 * This previously returned a bare `StorageSettings()`: the passcode and auth state sat in
 * localStorage as readable JSON, one DevTools panel away, while the `expect` KDoc described web as
 * "in-memory" — which would at least have died with the tab.
 *
 * ## Call [warmUp] before Koin
 * `create()` is synchronous because every other platform's is, but WebCrypto is async-only. So the
 * async work happens once at startup and `create()` returns a synchronous view over the result.
 * If warm-up has not completed, `create()` THROWS rather than quietly handing back an empty or
 * plaintext store — a silent fallback is how the original defect stayed invisible.
 *
 * ## Bounds worth knowing
 * The key is non-extractable, so no script can export it — not even injected script, which is
 * limited to using it on the live page instead of taking it away. That is a real improvement over
 * plaintext and not a claim of XSS immunity; no browser storage scheme offers that.
 */
actual class SecureSettingsFactory {

    /**
     * Web secure settings, backed by `WebSecureStore`'s AES-GCM store. Requires `warmUp()` to have completed — the
     * decryption key loads asynchronously, and building this before it lands throws.
     */
    actual fun create(): Settings = WebSecureStore.settings()

    /** Factory entry points. */
    companion object {

        /**
         * Default scope for the one-time plaintext adoption: the only key the web secure store has
         * ever held. See `WebSecureStore.warmUp` for why this is scoped rather than "everything".
         */
        private val LEGACY_KEY_PREFIXES = listOf("secure_data_key")

        /** Idempotent. Must complete before `initKoin()`. */
        suspend fun warmUp(legacyKeyPrefixes: List<String> = LEGACY_KEY_PREFIXES) =
            WebSecureStore.warmUp(legacyKeyPrefixes)
    }
}
