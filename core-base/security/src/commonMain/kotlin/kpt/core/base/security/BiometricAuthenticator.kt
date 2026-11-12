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
 * Platform-agnostic biometric authentication interface.
 *
 * Platform actuals wrap:
 * - Android: BiometricPrompt
 * - iOS: LAContext (LocalAuthentication)
 * - Desktop/JS: Returns [BiometricResult.Unavailable]
 */
expect class BiometricAuthenticator() {

    /** Check if biometric hardware is available and enrolled. */
    fun isAvailable(): Boolean

    /**
     * Prompt the user for biometric authentication.
     * Returns the result synchronously on the calling coroutine.
     */
    suspend fun authenticate(reason: String): BiometricResult
}

/**
 * The outcome of one biometric prompt.
 *
 * [Cancelled] and [Unavailable] are deliberately distinct from [Failure]: the user dismissing the
 * sheet and the device having no enrolled biometric are not authentication failures, and treating
 * them as such would count them toward a lockout the user cannot clear.
 */
sealed class BiometricResult {
    /** The user authenticated. */
    data object Success : BiometricResult()

    /**
     * Authentication was attempted and rejected; [message] explains why. Counts toward lockout.
     *
     * @property message why authentication failed. Deliberately coarse — a precise reason would reveal which factor
     *   is enrolled.
     */
    data class Failure(val message: String) : BiometricResult()

    /** The user dismissed the prompt. NOT a failure — it must not count toward lockout. */
    data object Cancelled : BiometricResult()

    /** No biometric is enrolled or the hardware is absent. A capability fact, not an attempt. */
    data object Unavailable : BiometricResult()
}
