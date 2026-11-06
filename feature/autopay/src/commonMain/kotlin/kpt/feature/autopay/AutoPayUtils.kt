/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Converts a Bill with AutoPay enabled to an AutoPaySchedule
 */
fun Bill.toAutoPaySchedule(): AutoPaySchedule {
    return AutoPaySchedule(
        id = this.id ?: "",
        name = this.name,
        amount = this.amount,
        currency = this.currency,
        frequency = this.recurrencePattern.displayName,
        nextPaymentDate = formatDateForDisplay(this.dueDate),
        status = mapBillStatusToAutoPayStatus(this.status),
        recipientName = this.billerName ?: "",
        accountNumber = this.autoPaySourceAccount ?: "",
    )
}

/**
 * Converts a Bill with AutoPay enabled to an UpcomingPayment
 */
fun Bill.toUpcomingPayment(): UpcomingPayment {
    return UpcomingPayment(
        id = this.id ?: "",
        scheduleName = this.name,
        amount = this.amount,
        currency = this.currency,
        dueDate = formatDateForDisplay(this.dueDate),
        status = mapBillStatusToPaymentStatus(this.status),
        recipientName = this.billerName ?: "",
    )
}

/**
 * Maps BillStatus to AutoPayStatus
 */
fun mapBillStatusToAutoPayStatus(billStatus: BillStatus): AutoPayStatus {
    return when (billStatus) {
        BillStatus.ACTIVE -> AutoPayStatus.ACTIVE
        BillStatus.PAUSED -> AutoPayStatus.PAUSED
        BillStatus.CANCELLED -> AutoPayStatus.CANCELLED
        BillStatus.COMPLETED -> AutoPayStatus.COMPLETED
    }
}

/**
 * Maps BillStatus to PaymentStatus
 */
fun mapBillStatusToPaymentStatus(billStatus: BillStatus): PaymentStatus {
    return when (billStatus) {
        BillStatus.ACTIVE -> PaymentStatus.UPCOMING
        BillStatus.PAUSED -> PaymentStatus.UPCOMING
        BillStatus.CANCELLED -> PaymentStatus.CANCELLED
        BillStatus.COMPLETED -> PaymentStatus.COMPLETED
    }
}

/**
 * Calculates the next payment date based on the bill's due date and recurrence pattern
 */
@OptIn(ExperimentalTime::class)
fun calculateNextPaymentDate(bill: Bill): Long {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val dueDate = bill.dueDate

    return when {
        dueDate > currentTime -> dueDate
        else -> {
            // Calculate next occurrence based on recurrence pattern
            val interval = bill.recurrencePattern.interval * 24 * 60 * 60 * 1000L // Convert days to milliseconds
            val occurrencesSinceDue = ((currentTime - dueDate) / interval) + 1
            dueDate + (occurrencesSinceDue * interval)
        }
    }
}

/**
 * Formats a timestamp to a readable date string
 */
@OptIn(ExperimentalTime::class)
fun formatDateForDisplay(timestamp: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    } catch (e: Exception) {
        "Invalid Date"
    }
}

/**
 * Checks if a bill is overdue
 */
@OptIn(ExperimentalTime::class)
fun isBillOverdue(bill: Bill): Boolean {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    return bill.dueDate < currentTime && bill.status == BillStatus.ACTIVE
}

/**
 * Gets bills with AutoPay enabled from a list of bills
 */
fun List<Bill>.getBillsWithAutoPay(): List<Bill> {
    return this.filter { it.autoPayEnabled }
}

/**
 * Gets active bills with AutoPay enabled
 */
fun List<Bill>.getActiveBillsWithAutoPay(): List<Bill> {
    return this.filter { it.autoPayEnabled && it.status == BillStatus.ACTIVE }
}

/**
 * Calculates upcoming payments for bills with AutoPay enabled
 */
fun List<Bill>.calculateUpcomingPayments(): List<UpcomingPayment> {
    return this.getActiveBillsWithAutoPay()
        .map { it.toUpcomingPayment() }
        .sortedBy { it.dueDate }
}
