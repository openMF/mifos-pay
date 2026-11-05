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
 * Web/JS [SecureRandom] stub.
 *
 * **WARNING: NOT cryptographically secure.** Uses [kotlin.random.Random] which is a PRNG,
 * not a CSPRNG. This implementation exists only to satisfy the [FieldEncryptor] no-op stub
 * on JS/WasmJS targets. Do NOT use for real cryptographic key generation.
 *
 * Full WebCrypto (`crypto.getRandomValues`) integration is deferred to Phase 4 (T18).
 */
@Suppress("MagicNumber")
actual class SecureRandom {
    /**
     * `nextBytes` on this target. Web (JS/WasmJS) — **NO-OP stub: data is NOT encrypted.** WebCrypto is async-only and
     * cannot satisfy this synchronous contract; use `WebSecureCrypto` for real confidentiality.
     */
    actual fun nextBytes(size: Int): ByteArray {
        return kotlin.random.Random.nextBytes(size)
    }
}
