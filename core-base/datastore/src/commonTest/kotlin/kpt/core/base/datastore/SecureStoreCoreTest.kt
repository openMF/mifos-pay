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

import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val SECURE = "secure_data_key"

private class FakeStorage(initial: Map<String, String> = emptyMap()) : SecureBlobStorage {
    val data = linkedMapOf<String, String>().apply { putAll(initial) }
    override fun get(key: String): String? = data[key]
    override fun set(key: String, value: String) {
        data[key] = value
    }
    override fun remove(key: String) {
        data.remove(key)
    }
    override fun keys(): List<String> = data.keys.toList()
}

/**
 * Reversible stand-in for AES-GCM. Base64 rather than something like `reversed()` so the plaintext
 * does not survive verbatim in the output — otherwise "the cleartext is gone" assertions would pass
 * or fail for the wrong reason.
 */
@OptIn(ExperimentalEncodingApi::class)
private class FakeCipher(private val available: Boolean = true) : SecureCipher {
    override suspend fun warmUp(): Boolean = available
    override suspend fun encrypt(plaintext: String): String =
        Base64.encode(plaintext.encodeToByteArray())
    override suspend fun decrypt(ciphertext: String): String =
        Base64.decode(ciphertext).decodeToString()
}

/** Stands in for a rotated key or a corrupted blob. */
private class UndecryptableCipher : SecureCipher {
    override suspend fun warmUp(): Boolean = true
    override suspend fun encrypt(plaintext: String): String = plaintext
    override suspend fun decrypt(ciphertext: String): String = error("cannot decrypt")
}

class SecureStoreCoreTest {

    @Test
    fun values_are_encrypted_at_rest() = runTest {
        val storage = FakeStorage()
        val core = SecureStoreCore(storage, FakeCipher())
        core.warmUp(listOf(SECURE))

        core.settings {}.putString("passcode", "1234")
        core.flushNow()

        val blob = storage.data.getValue(BLOB_KEY)
        assertTrue(blob.startsWith(ENC_PREFIX), "stored blob should be marked as ciphertext")
        assertFalse(blob.contains("1234"), "cleartext passcode must not survive in storage")
    }

    @Test
    fun legacy_plaintext_is_adopted_and_upgraded_in_place() = runTest {
        // What an existing web install actually looks like: plain JSON written by StorageSettings.
        val storage = FakeStorage(
            mapOf("$SECURE.passcode" to "1234", "$SECURE.token" to "tok-abc"),
        )
        val core = SecureStoreCore(storage, FakeCipher())
        core.warmUp(listOf(SECURE))

        // The point of the whole migration: existing users must not lose their secrets.
        val settings = core.settings {}
        assertEquals("1234", settings.getStringOrNull("$SECURE.passcode"))
        assertEquals("tok-abc", settings.getStringOrNull("$SECURE.token"))

        // Upgraded eagerly, so the cleartext window is this one launch.
        assertTrue(storage.data.getValue(BLOB_KEY).startsWith(ENC_PREFIX))
        assertFalse(storage.data.containsKey("$SECURE.passcode"), "cleartext key must be deleted")
        assertFalse(storage.data.values.any { it.contains("tok-abc") }, "no cleartext left anywhere")
    }

    @Test
    fun adoption_is_scoped_and_leaves_the_plain_store_alone() = runTest {
        // The web `plain` and `secure` stores share one namespace. Adopting everything would
        // encrypt the plain store's keys and leave it reading ciphertext it cannot decode.
        val storage = FakeStorage(
            mapOf(
                "$SECURE.passcode" to "1234",
                "user_data_key.themeBrand" to "DEFAULT",
                "app_language" to "en",
            ),
        )
        val core = SecureStoreCore(storage, FakeCipher())
        core.warmUp(listOf(SECURE))

        assertEquals("DEFAULT", storage.data["user_data_key.themeBrand"], "plain key must survive")
        assertEquals("en", storage.data["app_language"], "unrelated key must survive")

        val settings = core.settings {}
        assertNull(settings.getStringOrNull("app_language"), "must not be pulled into the secure store")
        assertNull(settings.getStringOrNull("user_data_key.themeBrand"))
    }

    @Test
    fun a_corrupt_blob_starts_empty_rather_than_reading_ciphertext_as_plaintext() = runTest {
        val storage = FakeStorage(mapOf(BLOB_KEY to ENC_PREFIX + "@@@not-decryptable"))
        val core = SecureStoreCore(storage, UndecryptableCipher())
        core.warmUp(listOf(SECURE))

        // The prefix already established this is ciphertext, so a decrypt failure is a rotated key
        // or corruption — never legacy plaintext to be adopted.
        assertEquals(0, core.settings {}.size)
    }

    @Test
    fun an_unprefixed_blob_is_not_mistaken_for_ciphertext() = runTest {
        // Guards the discriminator itself: without the prefix rule this value would be handed to
        // decrypt and dropped, which is precisely how the desktop implementation lost secrets.
        val storage = FakeStorage(mapOf("$SECURE.passcode" to "1234"))
        val core = SecureStoreCore(storage, UndecryptableCipher())
        core.warmUp(listOf(SECURE))

        assertEquals("1234", core.settings {}.getStringOrNull("$SECURE.passcode"))
    }

    @Test
    fun degraded_mode_works_in_memory_and_persists_nothing() = runTest {
        val storage = FakeStorage()
        val core = SecureStoreCore(storage, FakeCipher(available = false))
        core.warmUp(listOf(SECURE))
        assertTrue(core.degraded)

        val settings = core.settings {}
        settings.putString("passcode", "1234")
        core.flushNow()

        assertTrue(storage.data.isEmpty(), "degraded mode must never write - especially not plaintext")
        assertEquals("1234", settings.getStringOrNull("passcode"), "but must still work in memory")
    }

    @Test
    fun settings_before_warm_up_fails_loudly() {
        val core = SecureStoreCore(FakeStorage(), FakeCipher())
        // Silently returning an empty store is how the original defect stayed invisible.
        assertFailsWith<IllegalStateException> { core.settings {} }
    }

    @Test
    fun typed_values_round_trip_across_a_reload() = runTest {
        val storage = FakeStorage()
        SecureStoreCore(storage, FakeCipher()).run {
            warmUp(listOf(SECURE))
            settings {}.run {
                putInt("i", 42)
                putLong("l", 9L)
                putBoolean("b", true)
                putDouble("d", 1.5)
                putFloat("f", 2.5f)
                putString("s", "hi")
            }
            flushNow()
        }

        val reloaded = SecureStoreCore(storage, FakeCipher()).apply { warmUp(listOf(SECURE)) }
        val settings = reloaded.settings {}
        assertEquals(42, settings.getInt("i", 0))
        assertEquals(9L, settings.getLong("l", 0L))
        assertEquals(true, settings.getBoolean("b", false))
        assertEquals(1.5, settings.getDouble("d", 0.0))
        assertEquals(2.5f, settings.getFloat("f", 0f))
        assertEquals("hi", settings.getString("s", ""))
    }

    @Test
    fun mutations_signal_the_owner_so_a_flush_can_be_scheduled() = runTest {
        val core = SecureStoreCore(FakeStorage(), FakeCipher())
        core.warmUp(listOf(SECURE))

        var signals = 0
        val settings = core.settings { signals++ }
        settings.putString("a", "1")
        settings.putInt("b", 2)
        assertEquals(2, signals)

        settings.remove("missing")
        assertEquals(2, signals, "removing an absent key should not schedule a write")

        settings.remove("a")
        assertEquals(3, signals)
    }

    @Test
    fun warm_up_is_idempotent() = runTest {
        val storage = FakeStorage(mapOf("$SECURE.passcode" to "1234"))
        val core = SecureStoreCore(storage, FakeCipher())
        core.warmUp(listOf(SECURE))
        core.settings {}.putString("$SECURE.passcode", "5678")
        core.warmUp(listOf(SECURE))

        assertEquals("5678", core.settings {}.getStringOrNull("$SECURE.passcode"))
    }
}
