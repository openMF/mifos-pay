/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.security

/**
 * Configurable security policy. Consumer apps can adjust thresholds
 * based on their risk profile.
 */
data class SecurityPolicy(
    /** Whether a biometric prompt gates sensitive operations. */
    val requireBiometricForSensitiveOps: Boolean = true,
    /** Failed attempts before the app locks. Lockout is recoverable. */
    val lockAfterFailedAttempts: Int = 5,
    /**
     * Failed attempts before local data is WIPED. Terminal and irreversible — must exceed [lockAfterFailedAttempts] so
     * the user meets the recoverable limit first.
     */
    val wipeAfterFailedAttempts: Int = 10,
    /** Idle minutes before the session ends and re-authentication is required. */
    val sessionTimeoutMinutes: Int = 30,
    /** Seconds after which a copied sensitive value is cleared from the clipboard. */
    val clipboardWipeSeconds: Int = 60,
) {
    /** Policy presets. */
    companion object {
        /** The default policy — the values above, which suit a consumer app holding personal financial data. */
        fun default(): SecurityPolicy = SecurityPolicy()
    }
}
