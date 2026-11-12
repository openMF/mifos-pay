/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.fork.utils

import kpt.core.common.utils.hasConsecutiveRepetitions
import kpt.core.common.utils.hasSpaces
import kotlin.math.log2
import kotlin.math.pow

/** Evaluates password strength and produces improvement feedback. */
object PasswordChecker {
    private const val MIN_PASSWORD_LENGTH = 12
    private const val STRONG_PASSWORD_LENGTH = 15
    private const val MIN_ENTROPY_BITS = 100.0
    private const val MAX_PASSWORD_LENGTH = 50

    /** Computes the [PasswordStrength] of [password] and wraps it in a [PasswordStrengthResult]. */
    fun getPasswordStrengthResult(password: String): PasswordStrengthResult {
        return PasswordStrengthResult.Success(getPasswordStrength(password))
    }

    private fun getPasswordStrength(password: String): PasswordStrength {
        val length = password.length
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasNumbers = password.any { it.isDigit() }
        val hasSymbols = password.any { !it.isLetterOrDigit() }

        val numTypesPresent =
            listOf(hasUpperCase, hasLowerCase, hasNumbers, hasSymbols).count { it }
        val entropyBits = calculateEntropy(password)

        return when {
            length < MIN_PASSWORD_LENGTH -> PasswordStrength.LEVEL_0
            numTypesPresent == 1 -> PasswordStrength.LEVEL_1
            numTypesPresent == 2 -> PasswordStrength.LEVEL_2
            numTypesPresent == 4 && length >= STRONG_PASSWORD_LENGTH &&
                entropyBits >= MIN_ENTROPY_BITS -> PasswordStrength.LEVEL_5

            numTypesPresent == 4 && length >= STRONG_PASSWORD_LENGTH -> PasswordStrength.LEVEL_4

            else -> PasswordStrength.LEVEL_3
        }
    }

    private fun calculateEntropy(password: String): Double {
        val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols
        return log2(charPool.toDouble().pow(password.length))
    }

    // TODO: Move password feedback messages to string.xml — currently not possible as SignUpState uses Parcelable
    //  and cannot hold List<StringResource>; revisit when SavedStateHandle usage is decoupled from state.
    /** Returns human-readable hints on how to make [password] stronger. */
    fun getPasswordFeedback(password: String): List<String> {
        val feedback = mutableListOf<String>()

        if (password.length < MIN_PASSWORD_LENGTH) {
            feedback.add("The password must be at least $MIN_PASSWORD_LENGTH characters long.")
        }
        if (password.length > MAX_PASSWORD_LENGTH) {
            feedback.add("The password must not exceed $MAX_PASSWORD_LENGTH characters.")
        }
        if (!password.any { it.isUpperCase() }) {
            feedback.add("Include at least one uppercase letter.")
        }
        if (!password.any { it.isLowerCase() }) {
            feedback.add("Include at least one lowercase letter.")
        }
        if (!password.any { it.isDigit() }) {
            feedback.add("Include at least one number.")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            feedback.add("Include at least one special character.")
        }
        if (password.hasConsecutiveRepetitions()) {
            feedback.add("Avoid using consecutive repeated characters.")
        }
        if (password.hasSpaces()) {
            feedback.add("Do not include spaces in the password.")
        }

        return feedback
    }
}

/** Result of a password strength evaluation. */
sealed class PasswordStrengthResult {
    /**
     * Evaluation succeeded.
     *
     * @property passwordStrength The computed strength level.
     */
    data class Success(val passwordStrength: PasswordStrength) : PasswordStrengthResult()

    /**
     * Evaluation failed.
     *
     * @property message Description of the failure.
     */
    data class Error(val message: String) : PasswordStrengthResult()
}
