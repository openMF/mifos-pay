/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.util

import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillFormData
import kpt.core.model.autopay.BillValidationResult
import kpt.core.model.autopay.RecurrencePattern
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Utility class for validating bill data before submission
 */
object BillValidator {

    /**
     * Validates a Bill object
     */
    fun validateBill(bill: Bill): BillValidationResult {
        val nameError = validateName(bill.name)
        val amountError = validateAmount(bill.amount)
        val dueDateError = validateDueDate(bill.dueDate)
        val recurrencePatternError = validateRecurrencePattern(bill.recurrencePattern)
        val billerError = validateBiller(bill.billerId, bill.billerName)

        val isValid = nameError == null &&
            amountError == null &&
            dueDateError == null &&
            recurrencePatternError == null &&
            billerError == null

        return BillValidationResult(
            isValid = isValid,
            nameError = nameError,
            amountError = amountError,
            dueDateError = dueDateError,
            recurrencePatternError = recurrencePatternError,
            billerError = billerError,
        )
    }

    /**
     * Validates BillFormData object
     */
    fun validateBillFormData(formData: BillFormData): BillValidationResult {
        val nameError = validateName(formData.name)
        val amountError = validateAmountString(formData.amount)
        val dueDateError = validateDueDate(formData.dueDate)
        val recurrencePatternError = validateRecurrencePattern(formData.recurrencePattern)
        val billerError = validateBiller(formData.billerId, formData.billerName)

        // AutoPay validation
        val autoPayPaymentMethodError = validateAutoPayPaymentMethod(formData.enableAutoPay, formData.autoPayPaymentMethod)
        val autoPaySourceAccountError = validateAutoPaySourceAccount(formData.enableAutoPay, formData.autoPaySourceAccount)
        val autoPayMaxAmountError = validateAutoPayMaxAmount(formData.autoPayMaxAmount)

        val isValid = nameError == null &&
            amountError == null &&
            dueDateError == null &&
            recurrencePatternError == null &&
            billerError == null &&
            autoPayPaymentMethodError == null &&
            autoPaySourceAccountError == null &&
            autoPayMaxAmountError == null

        return BillValidationResult(
            isValid = isValid,
            nameError = nameError,
            amountError = amountError,
            dueDateError = dueDateError,
            recurrencePatternError = recurrencePatternError,
            billerError = billerError,
            autoPayPaymentMethodError = autoPayPaymentMethodError,
            autoPaySourceAccountError = autoPaySourceAccountError,
            autoPayMaxAmountError = autoPayMaxAmountError,
        )
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Bill name is required"
            name.length < 2 -> "Bill name must be at least 2 characters long"
            name.length > 100 -> "Bill name must be less than 100 characters"
            !name.matches(Regex("^[a-zA-Z0-9\\s\\-_]+$")) -> "Bill name contains invalid characters"
            else -> null
        }
    }

    private fun validateAmount(amount: Double): String? {
        return when {
            amount <= 0 -> "Bill amount must be greater than 0"
            amount > 999999.99 -> "Bill amount cannot exceed 999,999.99"
            else -> null
        }
    }

    private fun validateAmountString(amountString: String): String? {
        return when {
            amountString.isBlank() -> "Bill amount is required"
            else -> {
                try {
                    val amount = amountString.toDouble()
                    validateAmount(amount)
                } catch (e: NumberFormatException) {
                    "Invalid amount format"
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun validateDueDate(dueDate: Long): String? {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return when {
            dueDate <= 0 -> "Due date is required"
            dueDate < currentTime -> "Due date cannot be in the past"
            dueDate > currentTime + (365 * 24 * 60 * 60 * 1000L) -> "Due date cannot be more than 1 year in the future"
            else -> null
        }
    }

    private fun validateRecurrencePattern(pattern: RecurrencePattern): String? {
        return when (pattern) {
            RecurrencePattern.NONE -> null
            RecurrencePattern.DAILY -> null
            RecurrencePattern.WEEKLY -> null
            RecurrencePattern.BIWEEKLY -> null
            RecurrencePattern.MONTHLY -> null
            RecurrencePattern.QUARTERLY -> null
            RecurrencePattern.SEMI_ANNUALLY -> null
            RecurrencePattern.ANNUALLY -> null
        }
    }

    private fun validateBiller(billerId: String?, billerName: String?): String? {
        return when {
            billerId.isNullOrBlank() && billerName.isNullOrBlank() -> null
            billerId.isNullOrBlank() && !billerName.isNullOrBlank() -> "Biller ID is required when biller name is provided"
            !billerId.isNullOrBlank() && billerName.isNullOrBlank() -> "Biller name is required when biller ID is provided"
            else -> null
        }
    }

    private fun validateAutoPayPaymentMethod(enableAutoPay: Boolean, paymentMethod: String): String? {
        return when {
            !enableAutoPay -> null
            paymentMethod.isBlank() -> "Payment method is required when AutoPay is enabled"
            !listOf("Bank Account", "Credit Card", "UPI").contains(paymentMethod) -> "Invalid payment method"
            else -> null
        }
    }

    private fun validateAutoPaySourceAccount(enableAutoPay: Boolean, sourceAccount: String): String? {
        return when {
            !enableAutoPay -> null
            sourceAccount.isBlank() -> "Source account is required when AutoPay is enabled"
            sourceAccount.length < 8 -> "Source account must be at least 8 characters"
            sourceAccount.length > 20 -> "Source account must be less than 20 characters"
            else -> null
        }
    }

    private fun validateAutoPayMaxAmount(maxAmount: String): String? {
        return when {
            maxAmount.isBlank() -> null
            else -> {
                try {
                    val amount = maxAmount.toDouble()
                    when {
                        amount <= 0 -> "Maximum amount must be greater than 0"
                        amount > 999999.99 -> "Maximum amount cannot exceed 999,999.99"
                        else -> null
                    }
                } catch (e: NumberFormatException) {
                    "Invalid maximum amount format"
                }
            }
        }
    }

    /**
     * Validates if a bill is overdue
     */
    @OptIn(ExperimentalTime::class)
    fun isBillOverdue(bill: Bill): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return bill.dueDate < currentTime && bill.status == kpt.core.model.autopay.BillStatus.ACTIVE
    }

    /**
     * Calculates the next payment date based on recurrence pattern
     */
    @OptIn(ExperimentalTime::class)
    fun calculateNextPaymentDate(bill: Bill): Long {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return when (bill.recurrencePattern) {
            RecurrencePattern.NONE -> bill.dueDate
            RecurrencePattern.DAILY -> bill.dueDate + (1 * 24 * 60 * 60 * 1000L)
            RecurrencePattern.WEEKLY -> bill.dueDate + (7 * 24 * 60 * 60 * 1000L)
            RecurrencePattern.BIWEEKLY -> bill.dueDate + (14 * 24 * 60 * 60 * 1000L)
            RecurrencePattern.MONTHLY -> bill.dueDate + (30 * 24 * 60 * 60 * 1000L)
            RecurrencePattern.QUARTERLY -> bill.dueDate + (90 * 24 * 60 * 60 * 1000L)
            RecurrencePattern.SEMI_ANNUALLY -> bill.dueDate + (180 * 24 * 60 * 60 * 1000L)
            RecurrencePattern.ANNUALLY -> bill.dueDate + (365 * 24 * 60 * 60 * 1000L)
        }
    }

    /**
     * Checks if a bill is due within the specified number of days
     */
    @OptIn(ExperimentalTime::class)
    fun isBillDueWithinDays(bill: Bill, days: Int): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val daysInMillis = days * 24 * 60 * 60 * 1000L
        return bill.dueDate <= currentTime + daysInMillis &&
            bill.dueDate > currentTime &&
            bill.status == kpt.core.model.autopay.BillStatus.ACTIVE
    }
}
