/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.common.ScreenStateStream
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kpt.core.model.autopay.RecurrencePattern
/** Repository for bills and their statuses. */
interface BillRepository {
    /**
     * Get all bills for a client
     */
    fun getAllBills(clientId: Long): ScreenStateStream<List<Bill>>

    /**
     * Get bill by ID. Returns the bill or throws on failure.
     */
    suspend fun getBillById(id: String): Bill

    /**
     * Create a new bill. Returns the created bill or throws on failure.
     */
    suspend fun createBill(bill: Bill): Bill

    /**
     * Update an existing bill. Returns the updated bill or throws on failure.
     */
    suspend fun updateBill(bill: Bill): Bill

    /**
     * Delete a bill. Throws on failure.
     */
    suspend fun deleteBill(id: String)

    /**
     * Get bills by status
     */
    suspend fun getBillsByStatus(status: BillStatus): List<Bill>

    /**
     * Get bills by biller ID
     */
    suspend fun getBillsByBillerId(billerId: String): List<Bill>

    /**
     * Get bills by recurrence pattern
     */
    suspend fun getBillsByRecurrencePattern(pattern: RecurrencePattern): List<Bill>

    /**
     * Search bills by name
     */
    suspend fun searchBillsByName(query: String): List<Bill>

    /**
     * Get overdue bills
     */
    suspend fun getOverdueBills(): List<Bill>

    /**
     * Get upcoming bills within a date range
     */
    suspend fun getUpcomingBills(fromDate: Long, toDate: Long): List<Bill>

    /**
     * Update bill status. Returns the updated bill or throws on failure.
     */
    suspend fun updateBillStatus(billId: String, status: BillStatus): Bill

    /**
     * Validate bill data before submission. Throws on invalid input.
     */
    suspend fun validateBill(bill: Bill)

    /**
     * Get bill statistics for dashboard
     */
    fun getBillStatistics(clientId: Long): ScreenStateStream<BillStatistics>

    /**
     * Clear all bills. Throws on failure.
     */
    suspend fun clearAllBills()
}

/**
 * Aggregate figures for a client's bills.
 * @property totalBills Total number of bills.
 * @property activeBills Number of active bills.
 * @property overdueBills Number of overdue bills.
 * @property totalAmount Total amount across bills.
 * @property currency Currency code of the amounts.
 * @property upcomingPayments Number of upcoming payments.
 * @property totalAmountThisMonth Total amount due this month.
 */
data class BillStatistics(
    val totalBills: Int = 0,
    val activeBills: Int = 0,
    val overdueBills: Int = 0,
    val totalAmount: Double = 0.0,
    val currency: String = "USD",
    val upcomingPayments: Int = 0,
    val totalAmountThisMonth: Double = 0.0,
)
