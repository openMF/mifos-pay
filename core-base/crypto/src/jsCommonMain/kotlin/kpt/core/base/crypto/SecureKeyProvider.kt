/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.crypto

/**
 * Web key provider — stores keys in memory only.
 *
 * Full IndexedDB CryptoKey storage deferred to Phase 4 (T18).
 */
actual class SecureKeyProvider {
    private var storedKey: ByteArray? = null

    /**
     * `getKey` on this target. Web (JS/WasmJS) — **NO-OP stub: data is NOT encrypted.** WebCrypto is async-only and
     * cannot satisfy this synchronous contract; use `WebSecureCrypto` for real confidentiality.
     */
    actual fun getKey(): ByteArray? = storedKey?.copyOf()

    /**
     * `generateKey` on this target. Web (JS/WasmJS) — **NO-OP stub: data is NOT encrypted.** WebCrypto is async-only
     * and cannot satisfy this synchronous contract; use `WebSecureCrypto` for real confidentiality.
     */
    actual fun generateKey(): ByteArray {
        val key = SecureRandom().nextBytes(32)
        storedKey = key.copyOf()
        return key
    }

    /**
     * `deleteKey` on this target. Web (JS/WasmJS) — **NO-OP stub: data is NOT encrypted.** WebCrypto is async-only and
     * cannot satisfy this synchronous contract; use `WebSecureCrypto` for real confidentiality.
     */
    actual fun deleteKey() {
        storedKey?.fill(0)
        storedKey = null
    }
}
