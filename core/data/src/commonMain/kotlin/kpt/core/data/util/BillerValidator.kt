/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import co.touchlab.kermit.Logger
import kpt.core.model.autopay.BillerFormData
import kpt.core.model.autopay.BillerValidationResult
/**
 * Validator for biller form data with comprehensive validation rules.
 *
 * Provides validation for all biller fields including:
 * - Name validation (required, length, format)
 * - Account number validation (required, format, length)
 * - Contact number validation (required, format, length)
 * - Email validation (optional, format)
 * - Category validation (required)
 * - Address validation (optional, length)
 */
object BillerValidator {

    private val logger = Logger.withTag("BILLER_VALIDATOR")

    // Validation constants
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 100
    private const val MIN_ACCOUNT_NUMBER_LENGTH = 8
    private const val MAX_ACCOUNT_NUMBER_LENGTH = 20
    private const val MIN_CONTACT_NUMBER_LENGTH = 10
    private const val MAX_CONTACT_NUMBER_LENGTH = 15
    private const val MAX_EMAIL_LENGTH = 254
    private const val MAX_ADDRESS_LENGTH = 500

    // Regex patterns
    private val NAME_PATTERN = Regex("^[a-zA-Z0-9\\s\\-_.]+$")
    private val ACCOUNT_NUMBER_PATTERN = Regex("^[0-9]+$")
    private val CONTACT_NUMBER_PATTERN = Regex("^[+]?[0-9\\s\\-()]+$")
    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * Validates the complete biller form data and returns validation result.
     *
     * @param formData The biller form data to validate
     * @return BillerValidationResult containing validation status and error messages
     */
    fun validateBillerForm(formData: BillerFormData): BillerValidationResult {
        logger.d("BILLER_VALIDATOR Starting validation for biller: ${formData.name}")

        val nameError = validateName(formData.name)
        val accountNumberError = validateAccountNumber(formData.accountNumber)
        val contactNumberError = validateContactNumber(formData.contactNumber)
        val emailError = validateEmail(formData.email)
        val categoryError = validateCategory(formData.category)
        val addressError = validateAddress(formData.address)

        val isValid = nameError == null &&
            accountNumberError == null &&
            contactNumberError == null &&
            emailError == null &&
            categoryError == null &&
            addressError == null

        val validationResult = BillerValidationResult(
            isValid = isValid,
            nameError = nameError,
            accountNumberError = accountNumberError,
            contactNumberError = contactNumberError,
            emailError = emailError,
            categoryError = categoryError,
        )

        logger.d("BILLER_VALIDATOR Validation completed. Valid: $isValid")
        return validationResult
    }

    /**
     * Validates biller name with length and format requirements.
     *
     * @param name The biller name to validate
     * @return Error message if invalid, null if valid
     */
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Biller name is required"
            name.length < MIN_NAME_LENGTH -> "Biller name must be at least $MIN_NAME_LENGTH characters"
            name.length > MAX_NAME_LENGTH -> "Biller name must be less than $MAX_NAME_LENGTH characters"
            !NAME_PATTERN.matches(name.trim()) -> "Biller name contains invalid characters"
            name.trim().isEmpty() -> "Biller name cannot be only whitespace"
            else -> null
        }
    }

    /**
     * Validates account number with format and length requirements.
     *
     * @param accountNumber The account number to validate
     * @return Error message if invalid, null if valid
     */
    private fun validateAccountNumber(accountNumber: String): String? {
        return when {
            accountNumber.isBlank() -> "Account number is required"
            accountNumber.length < MIN_ACCOUNT_NUMBER_LENGTH -> "Account number must be at least $MIN_ACCOUNT_NUMBER_LENGTH digits"
            accountNumber.length > MAX_ACCOUNT_NUMBER_LENGTH -> "Account number must be less than $MAX_ACCOUNT_NUMBER_LENGTH digits"
            !ACCOUNT_NUMBER_PATTERN.matches(accountNumber.trim()) -> "Account number must contain only digits"
            accountNumber.trim().isEmpty() -> "Account number cannot be only whitespace"
            else -> null
        }
    }

    /**
     * Validates contact number with format and length requirements.
     *
     * @param contactNumber The contact number to validate
     * @return Error message if invalid, null if valid
     */
    private fun validateContactNumber(contactNumber: String): String? {
        return when {
            contactNumber.isBlank() -> "Contact number is required"
            contactNumber.length < MIN_CONTACT_NUMBER_LENGTH -> "Contact number must be at least $MIN_CONTACT_NUMBER_LENGTH digits"
            contactNumber.length > MAX_CONTACT_NUMBER_LENGTH -> "Contact number must be less than $MAX_CONTACT_NUMBER_LENGTH digits"
            !CONTACT_NUMBER_PATTERN.matches(contactNumber.trim()) -> "Contact number contains invalid characters"
            contactNumber.trim().isEmpty() -> "Contact number cannot be only whitespace"
            else -> null
        }
    }

    /**
     * Validates email address format (optional field).
     *
     * @param email The email address to validate
     * @return Error message if invalid, null if valid
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> null // Email is optional
            email.length > MAX_EMAIL_LENGTH -> "Email address is too long (max $MAX_EMAIL_LENGTH characters)"
            !EMAIL_PATTERN.matches(email.trim()) -> "Please enter a valid email address"
            email.trim().isEmpty() -> "Email cannot be only whitespace"
            else -> null
        }
    }

    /**
     * Validates that a category has been selected.
     *
     * @param category The selected category to validate
     * @return Error message if invalid, null if valid
     */
    private fun validateCategory(category: kpt.core.model.autopay.BillerCategory?): String? {
        return when {
            category == null -> "Please select a biller category"
            else -> null
        }
    }

    /**
     * Validates address length (optional field).
     *
     * @param address The address to validate
     * @return Error message if invalid, null if valid
     */
    private fun validateAddress(address: String): String? {
        return when {
            address.isBlank() -> null // Address is optional
            address.length > MAX_ADDRESS_LENGTH -> "Address is too long (max $MAX_ADDRESS_LENGTH characters)"
            address.trim().isEmpty() -> "Address cannot be only whitespace"
            else -> null
        }
    }

    /**
     * Validates individual name field for real-time validation.
     *
     * @param name The name to validate
     * @return Error message if invalid, null if valid
     */
    fun validateNameField(name: String): String? = validateName(name)

    /**
     * Validates individual account number field for real-time validation.
     *
     * @param accountNumber The account number to validate
     * @return Error message if invalid, null if valid
     */
    fun validateAccountNumberField(accountNumber: String): String? = validateAccountNumber(accountNumber)

    /**
     * Validates individual contact number field for real-time validation.
     *
     * @param contactNumber The contact number to validate
     * @return Error message if invalid, null if valid
     */
    fun validateContactNumberField(contactNumber: String): String? = validateContactNumber(contactNumber)

    /**
     * Validates individual email field for real-time validation.
     *
     * @param email The email to validate
     * @return Error message if invalid, null if valid
     */
    fun validateEmailField(email: String): String? = validateEmail(email)
}
