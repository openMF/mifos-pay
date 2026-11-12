/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.utils

import kotlin.math.log2
import kotlin.math.pow

/**
 * Scores a password against the app's rules and explains what is missing.
 *
 * Pure and platform-free, so the same verdict drives the indicator and any validation.
 */
object PasswordChecker {
    private const val MIN_PASSWORD_LENGTH = 8
    private const val STRONG_PASSWORD_LENGTH = 12
    private const val MIN_ENTROPY_BITS = 60.0
    private const val MAX_PASSWORD_LENGTH = 128

    /**
     * Validates then scores a password.
     *
     * Returns an error for empty or over-length input rather than a band, because neither has a meaningful strength.
     *
     * @param password the candidate.
     */
    @Suppress("ReturnCount")
    fun getPasswordStrengthResult(password: String): PasswordStrengthResult {
        when {
            password.isEmpty() -> return PasswordStrengthResult.Error("Password cannot be empty.")
            password.length > MAX_PASSWORD_LENGTH -> {
                return PasswordStrengthResult.Error(
                    "Password is too long. Maximum length is $MAX_PASSWORD_LENGTH characters.",
                )
            }
        }

        val result = getPasswordStrength(password)

        return PasswordStrengthResult.Success(result)
    }

    /**
     * Scores a password into a band from character-class variety, length and entropy.
     *
     * @param password the candidate.
     */
    fun getPasswordStrength(password: String): PasswordStrength {
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
            numTypesPresent == 3 && length >= STRONG_PASSWORD_LENGTH -> PasswordStrength.LEVEL_4
            numTypesPresent == 4 && length >= STRONG_PASSWORD_LENGTH &&
                entropyBits >= MIN_ENTROPY_BITS -> PasswordStrength.LEVEL_5

            else -> PasswordStrength.LEVEL_3
        }
    }

    private fun calculateEntropy(password: String): Double {
        val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols
        return log2(charPool.toDouble().pow(password.length))
    }

    /**
     * The unmet requirements, as user-facing sentences — what to fix, not just that it is weak. Empty when the
     * password satisfies every rule.
     *
     * @param password the candidate.
     */
    fun getPasswordFeedback(password: String): List<String> {
        val feedback = mutableListOf<String>()

        if (password.length < MIN_PASSWORD_LENGTH) {
            feedback.add("Password should be at least $MIN_PASSWORD_LENGTH characters long.")
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
        if (password.length < STRONG_PASSWORD_LENGTH) {
            feedback.add("For a stronger password, use at least $STRONG_PASSWORD_LENGTH characters.")
        }

        return feedback
    }
}

/**
 * The verdict: a strength band plus the unmet requirements, so the UI can say WHAT to fix rather
 * than only that it is weak.
 */
sealed class PasswordStrengthResult {
    /**
     * The password scored.
     *
     * @property passwordStrength the band it scored.
     */
    data class Success(val passwordStrength: PasswordStrength) : PasswordStrengthResult()

    /**
     * The password could not be scored — empty or over the length limit.
     *
     * @property message why it could not be scored.
     */
    data class Error(val message: String) : PasswordStrengthResult()
}
