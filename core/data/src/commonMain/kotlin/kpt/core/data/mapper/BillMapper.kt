/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.mapper

import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillFormData
import kpt.core.model.autopay.BillStatus
import kpt.core.model.autopay.RecurrencePattern
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Utility class for mapping between different bill data models
 */
object BillMapper {

    /**
     * Converts BillFormData to Bill
     */
    @OptIn(ExperimentalTime::class)
    fun formDataToBill(formData: BillFormData): Bill {
        return Bill(
            id = null,
            name = formData.name.trim(),
            amount = formData.amount.toDoubleOrNull() ?: 0.0,
            currency = formData.currency,
            dueDate = formData.dueDate,
            recurrencePattern = formData.recurrencePattern,
            billerId = formData.billerId,
            billerName = formData.billerName,
            description = formData.description.trim().takeIf { it.isNotBlank() },
            isActive = true,
            status = BillStatus.ACTIVE,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Converts Bill to BillFormData
     */
    fun billToFormData(bill: Bill): BillFormData {
        return BillFormData(
            name = bill.name,
            amount = bill.amount.toString(),
            currency = bill.currency,
            dueDate = bill.dueDate,
            recurrencePattern = bill.recurrencePattern,
            billerId = bill.billerId,
            billerName = bill.billerName,
            description = bill.description ?: "",
        )
    }

    /**
     * Updates an existing Bill with BillFormData
     */
    @OptIn(ExperimentalTime::class)
    fun updateBillWithFormData(existingBill: Bill, formData: BillFormData): Bill {
        return existingBill.copy(
            name = formData.name.trim(),
            amount = formData.amount.toDoubleOrNull() ?: existingBill.amount,
            currency = formData.currency,
            dueDate = formData.dueDate,
            recurrencePattern = formData.recurrencePattern,
            billerId = formData.billerId,
            billerName = formData.billerName,
            description = formData.description.trim().takeIf { it.isNotBlank() },
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Creates a copy of Bill with updated status
     */
    @OptIn(ExperimentalTime::class)
    fun updateBillStatus(bill: Bill, newStatus: BillStatus): Bill {
        return bill.copy(
            status = newStatus,
            isActive = newStatus == BillStatus.ACTIVE,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Creates a copy of Bill with updated active state
     */
    @OptIn(ExperimentalTime::class)
    fun updateBillActiveState(bill: Bill, isActive: Boolean): Bill {
        return bill.copy(
            isActive = isActive,
            status = if (isActive) BillStatus.ACTIVE else BillStatus.PAUSED,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Creates a new Bill from existing Bill with next recurrence date
     */
    @OptIn(ExperimentalTime::class)
    fun createNextRecurrenceBill(bill: Bill): Bill? {
        if (bill.recurrencePattern == RecurrencePattern.NONE) {
            return null
        }

        val nextDueDate = calculateNextDueDate(bill)
        return bill.copy(
            id = null,
            dueDate = nextDueDate,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Calculates the next due date based on recurrence pattern
     */
    private fun calculateNextDueDate(bill: Bill): Long {
        val daysToAdd = when (bill.recurrencePattern) {
            RecurrencePattern.DAILY -> 1
            RecurrencePattern.WEEKLY -> 7
            RecurrencePattern.BIWEEKLY -> 14
            RecurrencePattern.MONTHLY -> 30
            RecurrencePattern.QUARTERLY -> 90
            RecurrencePattern.SEMI_ANNUALLY -> 180
            RecurrencePattern.ANNUALLY -> 365
            RecurrencePattern.NONE -> 0
        }

        return if (daysToAdd > 0) {
            bill.dueDate + (daysToAdd * 24 * 60 * 60 * 1000L)
        } else {
            bill.dueDate
        }
    }

    /**
     * Creates a summary string for a bill
     */
    fun createBillSummary(bill: Bill): String {
        return buildString {
            append(bill.name)
            if (bill.billerName != null) {
                append(" - ${bill.billerName}")
            }
            append(" (${bill.currency} ${bill.amount})")
        }
    }

    /**
     * Creates a display name for recurrence pattern
     */
    fun getRecurrenceDisplayName(pattern: RecurrencePattern): String {
        return when (pattern) {
            RecurrencePattern.NONE -> "No Recurrence"
            RecurrencePattern.DAILY -> "Daily"
            RecurrencePattern.WEEKLY -> "Weekly"
            RecurrencePattern.BIWEEKLY -> "Bi-weekly"
            RecurrencePattern.MONTHLY -> "Monthly"
            RecurrencePattern.QUARTERLY -> "Quarterly"
            RecurrencePattern.SEMI_ANNUALLY -> "Semi-annually"
            RecurrencePattern.ANNUALLY -> "Annually"
        }
    }

    /**
     * Creates a display name for bill status
     */
    fun getStatusDisplayName(status: BillStatus): String {
        return when (status) {
            BillStatus.ACTIVE -> "Active"
            BillStatus.PAUSED -> "Paused"
            BillStatus.CANCELLED -> "Cancelled"
            BillStatus.COMPLETED -> "Completed"
        }
    }
}
