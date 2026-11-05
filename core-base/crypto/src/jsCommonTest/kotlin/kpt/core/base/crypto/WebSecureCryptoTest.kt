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

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * Exercises [WebSecureCrypto] against REAL WebCrypto and IndexedDB.
 *
 * These run in headless Chrome (`jsBrowserTest` / `wasmJsBrowserTest`) because this module applies
 * the Compose plugin, which switches its web tests to a browser environment. That matters: neither
 * `crypto.subtle` nor `indexedDB` exists under Node, so the counterpart tests for the datastore's
 * store logic use fakes. Here nothing is faked — the key is really generated, really persisted, and
 * really used.
 *
 * The store's LOGIC (legacy adoption, prefix discrimination, degraded mode) is covered by
 * `SecureStoreCoreTest` in `core-base/datastore`.
 */
class WebSecureCryptoTest {

    @Test
    fun round_trip_returns_the_original() = runTest {
        val crypto = WebSecureCrypto().apply { warmUp() }
        assertEquals("passcode-1234", crypto.decrypt(crypto.encrypt("passcode-1234")))
    }

    @Test
    fun round_trip_survives_unicode_and_empty_input() = runTest {
        val crypto = WebSecureCrypto().apply { warmUp() }
        // The JS bridge walks bytes through String.fromCharCode/btoa, so a multi-byte payload is
        // the case most likely to be mangled by that path.
        val payload = "पासकोड ☃ 🔐 \"quoted\" \\slash"
        assertEquals(payload, crypto.decrypt(crypto.encrypt(payload)))
        assertEquals("", crypto.decrypt(crypto.encrypt("")))
    }

    @Test
    fun ciphertext_does_not_leak_the_plaintext() = runTest {
        val crypto = WebSecureCrypto().apply { warmUp() }
        val ciphertext = crypto.encrypt("passcode-1234")
        assertFalse(ciphertext.contains("passcode-1234"))
        assertNotEquals("passcode-1234", ciphertext)
    }

    @Test
    fun the_same_plaintext_encrypts_differently_each_time() = runTest {
        val crypto = WebSecureCrypto().apply { warmUp() }
        // A fresh IV per encryption. Reusing an IV under AES-GCM is catastrophic, not cosmetic:
        // two messages under one key/IV pair leak their XOR and break authentication.
        val a = crypto.encrypt("same")
        val b = crypto.encrypt("same")
        assertNotEquals(a, b, "each encryption must use a fresh IV")
        assertEquals("same", crypto.decrypt(a))
        assertEquals("same", crypto.decrypt(b))
    }

    @Test
    fun the_key_persists_so_a_later_instance_can_decrypt() = runTest {
        // Stands in for the next page load: a separate instance must find the SAME key in
        // IndexedDB, or every reload would silently orphan the user's stored secrets.
        val ciphertext = WebSecureCrypto().apply { warmUp() }.encrypt("across-reloads")
        val next = WebSecureCrypto().apply { warmUp() }
        assertEquals("across-reloads", next.decrypt(ciphertext))
    }

    @Test
    fun the_persisted_key_cannot_be_exported() = runTest {
        WebSecureCrypto().warmUp()
        // THE security property. The key is generated with `extractable: false`, so even script
        // that can reach IndexedDB cannot read its raw bytes and use them offline or elsewhere.
        // If this ever passes, the store has degraded to obfuscation and the KDoc's claims are false.
        assertFalse(canExportStoredKey(), "stored CryptoKey must be non-extractable")
    }

    @Test
    fun decrypting_garbage_fails_rather_than_returning_something() = runTest {
        val crypto = WebSecureCrypto().apply { warmUp() }
        assertFailsWith<Throwable> { crypto.decrypt("bm90LWEtdmFsaWQtY2lwaGVydGV4dA==") }
    }

    @Test
    fun tampered_ciphertext_is_rejected() = runTest {
        val crypto = WebSecureCrypto().apply { warmUp() }
        val ciphertext = crypto.encrypt("authentic")
        // Flip the final Base64 character. AES-GCM authenticates, so a modified payload must fail
        // rather than decrypt to garbage - that is the difference between GCM and raw CTR/CBC.
        val flipped = ciphertext.dropLast(2) + if (ciphertext[ciphertext.length - 2] == 'A') "B=" else "A="
        assertFailsWith<Throwable> { crypto.decrypt(flipped) }
    }

    @Test
    fun use_before_warm_up_fails_loudly() = runTest {
        val crypto = WebSecureCrypto()
        // Never silently encrypt under a key that was never loaded.
        assertFailsWith<IllegalArgumentException> { crypto.encrypt("x") }
        assertFailsWith<IllegalArgumentException> { crypto.decrypt("x") }
    }

    @Test
    fun warm_up_is_idempotent() = runTest {
        val crypto = WebSecureCrypto()
        crypto.warmUp()
        val ciphertext = crypto.encrypt("stable")
        crypto.warmUp()
        assertEquals("stable", crypto.decrypt(ciphertext), "second warm-up must not swap the key")
    }
}
