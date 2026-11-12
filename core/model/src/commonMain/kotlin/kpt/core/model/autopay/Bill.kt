/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.autopay

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * A bill the user tracks, optionally recurring and optionally paid automatically through auto-pay.
 *
 * @property id Unique id of the bill.
 * @property name Display name of the bill.
 * @property amount Amount due.
 * @property currency Currency code.
 * @property dueDate Due date as epoch milliseconds.
 * @property recurrencePattern How often the bill repeats.
 * @property billerId Id of the biller the bill is paid to.
 * @property billerName Name of the biller.
 * @property description Optional description.
 * @property isActive Whether the bill is active.
 * @property status Lifecycle status of the bill.
 * @property autoPayEnabled Whether the bill is paid automatically.
 * @property autoPayPaymentMethod Payment method used for auto-pay.
 * @property autoPaySourceAccount Source account used for auto-pay.
 * @property autoPayMaxAmount Maximum amount auto-pay may pay.
 * @property createdAt Creation time as epoch milliseconds.
 * @property updatedAt Last update time as epoch milliseconds.
 */
@Serializable
@Parcelize
data class Bill
@OptIn(ExperimentalTime::class)
constructor(
    /**
     * Unique id of the bill.
     */
    val id: String? = null,
    /**
     * Display name of the bill.
     */
    val name: String,
    /**
     * Amount due.
     */
    val amount: Double,
    /**
     * Currency code.
     */
    val currency: String = "USD",
    /**
     * Due date as epoch milliseconds.
     */
    val dueDate: Long,
    /**
     * How often the bill repeats.
     */
    val recurrencePattern: RecurrencePattern,
    /**
     * Id of the biller the bill is paid to.
     */
    val billerId: String? = null,
    /**
     * Name of the biller.
     */
    val billerName: String? = null,
    /**
     * Optional description.
     */
    val description: String? = null,
    /**
     * Whether the bill is active.
     */
    val isActive: Boolean = true,
    /**
     * Lifecycle status of the bill.
     */
    val status: BillStatus = BillStatus.ACTIVE,
    // AutoPay configuration
    /**
     * Whether the bill is paid automatically.
     */
    val autoPayEnabled: Boolean = false,
    /**
     * Payment method used for auto-pay.
     */
    val autoPayPaymentMethod: String? = null,
    /**
     * Source account used for auto-pay.
     */
    val autoPaySourceAccount: String? = null,
    /**
     * Maximum amount auto-pay may pay.
     */
    val autoPayMaxAmount: Double? = null,
    /**
     * Creation time as epoch milliseconds.
     */
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    /**
     * Last update time as epoch milliseconds.
     */
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) : Parcelable

/**
 * How often a bill repeats, with its display label and approximate interval in days.
 *
 * @property displayName Human-readable label.
 * @property interval Approximate number of days between occurrences.
 */
@Serializable
enum class RecurrencePattern(val displayName: String, val interval: Int) {
    /**
     * The bill does not repeat.
     */
    NONE("No Recurrence", 0),

    /**
     * Repeats every day.
     */
    DAILY("Daily", 1),

    /**
     * Repeats every 7 days.
     */
    WEEKLY("Weekly", 7),

    /**
     * Repeats every 14 days.
     */
    BIWEEKLY("Bi-weekly", 14),

    /**
     * Repeats every 30 days.
     */
    MONTHLY("Monthly", 30),

    /**
     * Repeats every 90 days.
     */
    QUARTERLY("Quarterly", 90),

    /**
     * Repeats every 180 days.
     */
    SEMI_ANNUALLY("Semi-annually", 180),

    /**
     * Repeats every 365 days.
     */
    ANNUALLY("Annually", 365),
    ;

    /**
     * Lookup helpers for RecurrencePattern.
     */
    companion object {
        /**
         * Returns the pattern whose display name matches, or null if none does.
         */
        fun fromDisplayName(displayName: String): RecurrencePattern? {
            return entries.find { it.displayName == displayName }
        }
    }
}

/**
 * Lifecycle status of a bill.
 */
@Serializable
enum class BillStatus {
    /**
     * Bill is active.
     */
    ACTIVE,

    /**
     * Bill is temporarily suspended.
     */
    PAUSED,

    /**
     * Bill was cancelled.
     */
    CANCELLED,

    /**
     * Bill has been fully paid or has ended.
     */
    COMPLETED,
}

/**
 * Editable form state for creating or editing a bill; amounts are kept as strings for text input.
 *
 * @property name Entered bill name.
 * @property amount Entered amount.
 * @property currency Selected currency code.
 * @property dueDate Selected due date as epoch milliseconds.
 * @property recurrencePattern Selected recurrence.
 * @property billerId Selected biller id.
 * @property billerName Selected biller name.
 * @property description Entered description.
 * @property enableAutoPay Whether auto-pay is switched on.
 * @property autoPayPaymentMethod Entered auto-pay payment method.
 * @property autoPaySourceAccount Entered auto-pay source account.
 * @property autoPayMaxAmount Entered auto-pay maximum amount.
 */
@Serializable
data class BillFormData(
    val name: String = "",
    val amount: String = "",
    val currency: String = "USD",
    val dueDate: Long = 0L,
    val recurrencePattern: RecurrencePattern = RecurrencePattern.NONE,
    val billerId: String? = null,
    val billerName: String? = null,
    val description: String = "",
    // AutoPay configuration
    val enableAutoPay: Boolean = false,
    val autoPayPaymentMethod: String = "",
    val autoPaySourceAccount: String = "",
    val autoPayMaxAmount: String = "",
)

/**
 * Outcome of validating a BillFormData, with an error message per invalid field.
 *
 * @property isValid True when every field passed validation.
 * @property nameError Error for the name field.
 * @property amountError Error for the amount field.
 * @property dueDateError Error for the due date field.
 * @property recurrencePatternError Error for the recurrence field.
 * @property billerError Error for the biller field.
 * @property autoPayPaymentMethodError Error for the auto-pay payment method.
 * @property autoPaySourceAccountError Error for the auto-pay source account.
 * @property autoPayMaxAmountError Error for the auto-pay maximum amount.
 */
@Serializable
data class BillValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val amountError: String? = null,
    val dueDateError: String? = null,
    val recurrencePatternError: String? = null,
    val billerError: String? = null,
    // AutoPay validation
    val autoPayPaymentMethodError: String? = null,
    val autoPaySourceAccountError: String? = null,
    val autoPayMaxAmountError: String? = null,
)

/**
 * Computed next payment date for a bill.
 *
 * @property date Date as epoch milliseconds.
 * @property formattedDate Date formatted for display.
 * @property isOverdue Whether the date is already in the past.
 */
@Serializable
data class NextPaymentDate(
    val date: Long,
    val formattedDate: String,
    val isOverdue: Boolean = false,
)
