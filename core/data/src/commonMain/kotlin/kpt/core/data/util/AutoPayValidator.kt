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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kpt.core.model.autopay.AutoPayPayload
import kpt.core.model.autopay.AutoPayUpdatePayload
/** Client-side validation of auto-pay payloads before they are submitted. */
@OptIn(FormatStringsInDatetimeFormats::class)
object AutoPayValidator {

    private val dateFormat = LocalDateTime.Format {
        byUnicodePattern("dd MMMM yyyy")
    }

    /** Outcome of validating an auto-pay payload. */
    sealed class ValidationResult {
        /** Result with no violations. */
        data object Valid : ValidationResult()

        /**
         * Result carrying a validation failure.
         * @property errorMessage Human-readable description of the violation.
         */
        data class Invalid(val errorMessage: String) : ValidationResult()
    }

    /** Validates a new auto-pay payload and returns the first violation, if any. */
    fun validateAutoPayPayload(payload: AutoPayPayload): ValidationResult {
        return when {
            payload.name.isBlank() -> ValidationResult.Invalid("Schedule name is required")
            payload.name.length < 3 -> ValidationResult.Invalid("Schedule name must be at least 3 characters")
            payload.name.length > 100 -> ValidationResult.Invalid("Schedule name must be less than 100 characters")

            payload.amount.isBlank() -> ValidationResult.Invalid("Amount is required")
            !isValidAmount(payload.amount) -> ValidationResult.Invalid("Invalid amount format")
            payload.amount.toDoubleOrNull()?.let { it <= 0 } == true -> ValidationResult.Invalid("Amount must be greater than 0")

            payload.currency.isBlank() -> ValidationResult.Invalid("Currency is required")
            !isValidCurrency(payload.currency) -> ValidationResult.Invalid("Invalid currency code")

            payload.frequency.isBlank() -> ValidationResult.Invalid("Frequency is required")
            !isValidFrequency(payload.frequency) -> ValidationResult.Invalid("Invalid frequency")

            payload.recipientName.isBlank() -> ValidationResult.Invalid("Recipient name is required")
            payload.recipientName.length < 2 -> ValidationResult.Invalid("Recipient name must be at least 2 characters")
            payload.recipientName.length > 100 -> ValidationResult.Invalid("Recipient name must be less than 100 characters")

            payload.recipientAccountNumber.isBlank() -> ValidationResult.Invalid("Recipient account number is required")
            !isValidAccountNumber(payload.recipientAccountNumber) -> ValidationResult.Invalid("Invalid account number format")

            payload.sourceAccountId <= 0 -> ValidationResult.Invalid("Invalid source account")
            payload.clientId <= 0 -> ValidationResult.Invalid("Invalid client ID")

            payload.validFrom.isNotBlank() && !isValidDate(payload.validFrom) -> ValidationResult.Invalid("Invalid start date format")
            payload.validTill.isNotBlank() && !isValidDate(payload.validTill) -> ValidationResult.Invalid("Invalid end date format")

            payload.validFrom.isNotBlank() && payload.validTill.isNotBlank() -> {
                val startDate = parseDate(payload.validFrom)
                val endDate = parseDate(payload.validTill)
                if (startDate != null && endDate != null && startDate >= endDate) {
                    ValidationResult.Invalid("End date must be after start date")
                } else {
                    ValidationResult.Valid
                }
            }

            else -> ValidationResult.Valid
        }
    }

    /** Validates an auto-pay update payload and returns the first violation, if any. */
    fun validateAutoPayUpdatePayload(payload: AutoPayUpdatePayload): ValidationResult {
        return when {
            payload.name?.let { it.isBlank() } == true -> ValidationResult.Invalid("Schedule name cannot be empty")
            payload.name?.let { it.length < 3 } == true -> ValidationResult.Invalid("Schedule name must be at least 3 characters")
            payload.name?.let { it.length > 100 } == true -> ValidationResult.Invalid("Schedule name must be less than 100 characters")

            payload.amount?.let { it.isBlank() } == true -> ValidationResult.Invalid("Amount cannot be empty")
            payload.amount?.let { !isValidAmount(it) } == true -> ValidationResult.Invalid("Invalid amount format")
            payload.amount?.toDoubleOrNull()?.let { it <= 0 } == true -> ValidationResult.Invalid("Amount must be greater than 0")

            payload.currency?.let { it.isBlank() } == true -> ValidationResult.Invalid("Currency cannot be empty")
            payload.currency?.let { !isValidCurrency(it) } == true -> ValidationResult.Invalid("Invalid currency code")

            payload.frequency?.let { it.isBlank() } == true -> ValidationResult.Invalid("Frequency cannot be empty")
            payload.frequency?.let { !isValidFrequency(it) } == true -> ValidationResult.Invalid("Invalid frequency")

            payload.recipientName?.let { it.isBlank() } == true -> ValidationResult.Invalid("Recipient name cannot be empty")
            payload.recipientName?.let { it.length < 2 } == true -> ValidationResult.Invalid("Recipient name must be at least 2 characters")
            payload.recipientName?.let { it.length > 100 } == true -> ValidationResult.Invalid("Recipient name must be less than 100 characters")

            payload.recipientAccountNumber?.let { it.isBlank() } == true -> ValidationResult.Invalid("Recipient account number cannot be empty")
            payload.recipientAccountNumber?.let { !isValidAccountNumber(it) } == true -> ValidationResult.Invalid("Invalid account number format")

            payload.validFrom?.let { it.isNotBlank() && !isValidDate(it) } == true -> ValidationResult.Invalid("Invalid start date format")
            payload.validTill?.let { it.isNotBlank() && !isValidDate(it) } == true -> ValidationResult.Invalid("Invalid end date format")

            payload.validFrom?.let { it.isNotBlank() } == true && payload.validTill?.let { it.isNotBlank() } == true -> {
                val startDate = parseDate(payload.validFrom!!)
                val endDate = parseDate(payload.validTill!!)
                if (startDate != null && endDate != null && startDate >= endDate) {
                    ValidationResult.Invalid("End date must be after start date")
                } else {
                    ValidationResult.Valid
                }
            }

            else -> ValidationResult.Valid
        }
    }

    private fun isValidAmount(amount: String): Boolean {
        return try {
            amount.toDoubleOrNull() != null && amount.toDouble() > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun isValidCurrency(currency: String): Boolean {
        return currency.length == 3 && currency.all { it.isLetter() }
    }

    private fun isValidFrequency(frequency: String): Boolean {
        val validFrequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")
        return validFrequencies.contains(frequency.uppercase())
    }

    private fun isValidAccountNumber(accountNumber: String): Boolean {
        return accountNumber.length >= 8 && accountNumber.length <= 20 && accountNumber.all { it.isLetterOrDigit() }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseDate(date: String): LocalDate? {
        return try {
            dateFormat.parse(date).date
        } catch (e: Exception) {
            null
        }
    }
}
