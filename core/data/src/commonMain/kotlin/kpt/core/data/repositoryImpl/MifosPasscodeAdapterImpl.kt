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
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

/** Multiplatform-Settings key for the user's passcode (string). */
const val MIFOS_PASSCODE = "org.mifospay.mifos.passcode"

/**
 * [PasscodeStorageAdapter] implementation backed by
 * `com.russhwolf.settings.Settings`. Single-key layout — just the passcode
 * itself. The biometric registration blob lives in a separate adapter
 * ([BiometricsSetupAdapterImpl]) since v2.2.0 of the library decoupled the
 * two surfaces.
 *
 * **Called by the library only.** App code should not invoke these methods
 * directly — they're the contract the passcode library uses to persist its
 * state. App-side reads of "is a passcode set" should go via
 * [cmp.navigation.registry.app.ForkAppViewModel.isPasscodeCreated], which
 * canonicalises the blank-vs-absent check.
 *
 * [loadPasscode] returns `null` for both absent and blank values, so the
 * library treats them identically as "no passcode set."
 */
class MifosPasscodeAdapterImpl(
    private val settings: Settings,
) : PasscodeStorageAdapter {
    override fun savePasscode(passcode: String) {
        settings.putString(MIFOS_PASSCODE, passcode)
    }

    override fun loadPasscode(): String? {
        val passcode = settings.getString(MIFOS_PASSCODE, "")
        if (passcode.isBlank()) return null
        return passcode
    }

    override fun deletePasscode() {
        settings.remove(MIFOS_PASSCODE)
    }
}
