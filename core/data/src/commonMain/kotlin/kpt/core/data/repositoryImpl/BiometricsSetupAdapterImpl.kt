/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import com.russhwolf.settings.Settings
import kpt.core.base.data.annotation.RepositoryBinding
import org.mifos.authenticator.biometrics.BiometricStorageAdapter

/** Multiplatform-Settings key for the registration blob (string). */
const val REGISTRATION_DATA_KEY = "org.mifospay.mifos.registration_data"

/**
 * Multiplatform-Settings key for the boolean "registration has occurred"
 * flag. Decoupled from [REGISTRATION_DATA_KEY] so we can distinguish
 * "registered with an empty blob" (valid on Android, where
 * `PlatformAuthenticator.registerUser` returns `Success("")`) from "never
 * registered."
 */
const val BIOMETRIC_REGISTERED_KEY = "org.mifospay.mifos.biometric_registered"

/**
 * [BiometricStorageAdapter] implementation backed by
 * `com.russhwolf.settings.Settings`. Two-key layout: a boolean "registered"
 * flag plus the registration blob string.
 *
 * **Called by the library only.** App code should not invoke these methods
 * directly; use the provider's `registerUser()`, `onAuthenticatorClick()`,
 * and `unregister()` methods, which drive this adapter internally.
 */
@RepositoryBinding(binds = BiometricStorageAdapter::class)
class BiometricsSetupAdapterImpl(
    private val settings: Settings,
) : BiometricStorageAdapter {
    override fun saveRegistrationData(registrationData: String) {
        settings.putBoolean(BIOMETRIC_REGISTERED_KEY, true)
        settings.putString(REGISTRATION_DATA_KEY, registrationData)
    }

    override fun loadRegistrationData(): String? {
        if (!settings.getBoolean(BIOMETRIC_REGISTERED_KEY, false)) return null
        return settings.getString(REGISTRATION_DATA_KEY, "")
    }

    override fun deleteRegistrationData() {
        settings.remove(BIOMETRIC_REGISTERED_KEY)
        settings.remove(REGISTRATION_DATA_KEY)
    }
}
