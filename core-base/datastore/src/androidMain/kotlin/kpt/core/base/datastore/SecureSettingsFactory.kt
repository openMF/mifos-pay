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

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import java.security.GeneralSecurityException

/** Android implementation of `SecureSettingsFactory`. */
actual class SecureSettingsFactory(private val context: Context) {
    /**
     * Android secure settings, backed by EncryptedSharedPreferences with a Keystore-held master key. Synchronous: the
     * key never leaves the Keystore but reads do not need to await it.
     *
     * A prefs file the current Keystore key cannot decrypt — restored by Android Auto Backup onto a
     * new install, or left behind by a reset Keystore — throws `AEADBadTagException` here and crashed
     * app start on every launch. Its contents are unrecoverable by design (the key that wrote them is
     * gone), so the file is deleted and recreated empty.
     */
    actual fun create(): Settings {
        val encryptedPrefs = try {
            openEncryptedPrefs()
        } catch (e: GeneralSecurityException) {
            context.deleteSharedPreferences(PREFS_NAME)
            openEncryptedPrefs()
        }
        return SharedPreferencesSettings(encryptedPrefs)
    }

    private fun openEncryptedPrefs() = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private companion object {
        const val PREFS_NAME = "secure_prefs"
    }
}
