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
 * Web AES-GCM backed by WebCrypto, with a **non-extractable** key held in IndexedDB.
 *
 * ## Why this exists alongside [FieldEncryptor]
 * [FieldEncryptor] is a SYNCHRONOUS `expect`, and WebCrypto is async-only — which is exactly why
 * the web [FieldEncryptor] is an honest no-op stub rather than a real implementation. This class is
 * the async counterpart: callers warm it up once, then encrypt and decrypt from a coroutine.
 *
 * ## The security property that matters
 * The AES key is generated with `extractable: false` and persisted as a `CryptoKey` OBJECT in
 * IndexedDB (localStorage cannot hold one — it stores strings, and a key that survives as a string
 * is by definition extractable). Its raw bytes therefore CANNOT be read back by any JavaScript on
 * the page, including injected script. The key can be USED but never exported.
 *
 * ## What this defends against — and what it does not
 * Defends: another script reading credentials out of localStorage, DevTools inspection, the browser
 * profile on disk, and profile sync/backup carrying secrets off the machine.
 *
 * Does NOT defend against live XSS while the page is open: injected script can call `decrypt` the
 * same way the app does. No browser storage scheme prevents that — non-extractability limits the
 * attacker to the live page instead of handing them a key that works offline and forever. Stated
 * plainly because the value of this class is bounded, and a reader deserves the bound.
 */
expect class WebSecureCrypto() {

    /**
     * Loads the existing key or generates one. MUST complete before [encrypt] / [decrypt];
     * both throw if it has not.
     */
    suspend fun warmUp()

    /** Returns Base64 of `iv (12 bytes) || ciphertext`. */
    suspend fun encrypt(plaintext: String): String

    /** Throws if [ciphertext] was not produced by [encrypt] under the current key. */
    suspend fun decrypt(ciphertext: String): String
}
