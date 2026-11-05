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

import java.io.File
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Desktop secure-settings encryption + legacy upgrade.
 *
 * Both [SecureSettingsFactory] and `SecureKeyProvider` resolve `user.home` lazily, so pointing that
 * property at a temp dir isolates the store AND its key from the real `~/.mifos-secure`.
 */
class SecureSettingsFactoryTest {

    private lateinit var home: File
    private var realHome: String? = null

    private val storeFile: File get() = File(File(home, ".mifos-secure"), "secure_settings.properties")

    @BeforeTest
    fun setUp() {
        realHome = System.getProperty("user.home")
        home = File.createTempFile("kpt-secure-", "").let {
            it.delete()
            it.mkdirs()
            it
        }
        System.setProperty("user.home", home.absolutePath)
    }

    @AfterTest
    fun tearDown() {
        realHome?.let { System.setProperty("user.home", it) }
        home.deleteRecursively()
    }

    /** Writes a store in the pre-encryption format: plain `Properties`, values in the clear. */
    private fun writeLegacyStore(vararg entries: Pair<String, String>) {
        val dir = File(home, ".mifos-secure").apply { mkdirs() }
        val props = Properties().apply { entries.forEach { (k, v) -> setProperty(k, v) } }
        File(dir, "secure_settings.properties").outputStream().use { props.store(it, null) }
    }

    private fun readRaw(): Properties =
        Properties().apply { storeFile.inputStream().use { load(it) } }

    @Test
    fun `values written through the factory are encrypted at rest`() {
        SecureSettingsFactory().create().putString("passcode", "1234")

        val raw = readRaw()
        assertTrue(raw.getProperty("passcode").startsWith("kpt.enc.v1:"), "value should be marked ciphertext")
        assertTrue(
            !storeFile.readText().contains("1234"),
            "cleartext passcode must not survive on disk",
        )
        assertEquals("1234", SecureSettingsFactory().create().getStringOrNull("passcode"))
    }

    @Test
    fun `a legacy cleartext store is readable and upgraded in place`() {
        writeLegacyStore("passcode" to "1234", "auth_token" to "tok-abc")

        // The upgrade is the point: existing desktop installs must not lose their secrets.
        val settings = SecureSettingsFactory().create()
        assertEquals("1234", settings.getStringOrNull("passcode"))
        assertEquals("tok-abc", settings.getStringOrNull("auth_token"))

        val raw = readRaw()
        assertTrue(raw.stringPropertyNames().isNotEmpty(), "upgrade must not empty the store")
        raw.stringPropertyNames().forEach {
            assertTrue(raw.getProperty(it).startsWith("kpt.enc.v1:"), "$it should be encrypted after load")
        }
        val onDisk = storeFile.readText()
        assertTrue(!onDisk.contains("1234") && !onDisk.contains("tok-abc"), "cleartext must be gone after upgrade")

        // And it survives the next launch, when nothing is legacy any more.
        assertEquals("1234", SecureSettingsFactory().create().getStringOrNull("passcode"))
    }

    @Test
    fun `a corrupt ciphertext value is dropped without taking its siblings with it`() {
        SecureSettingsFactory().create().apply {
            putString("passcode", "1234")
            putString("auth_token", "tok-abc")
        }
        val raw = readRaw().apply { setProperty("passcode", "kpt.enc.v1:not-valid-base64-@@@") }
        storeFile.outputStream().use { raw.store(it, null) }

        // Prefixed-but-undecryptable is a rotated key or corruption — dropping it is correct.
        // Without the prefix this is indistinguishable from legacy cleartext, which is why it exists.
        val settings = SecureSettingsFactory().create()
        assertNull(settings.getStringOrNull("passcode"))
        assertEquals("tok-abc", settings.getStringOrNull("auth_token"))
    }
}
