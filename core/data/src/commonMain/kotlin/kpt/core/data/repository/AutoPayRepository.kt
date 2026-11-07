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
import kpt.core.model.autopay.AutoPay
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.AutoPayPayload
import kpt.core.model.autopay.AutoPayTemplate
import kpt.core.model.autopay.AutoPayUpdatePayload
import kpt.core.model.autopay.UpcomingPayment
import kpt.core.model.network.entity.Page
/** Repository for managing auto-pay (scheduled recurring payment) schedules. */
interface AutoPayRepository {
    // Phase-3 cutover — Flow-shaped reads on ScreenState.

    /**
     * Get AutoPay template for creating new AutoPay schedules
     */
    fun getAutoPayTemplate(
        clientId: Long,
        sourceAccountId: Long,
    ): ScreenStateStream<AutoPayTemplate>

    /**
     * Get all AutoPay schedules for a client
     */
    fun getAllAutoPaySchedules(
        clientId: Long,
    ): ScreenStateStream<List<AutoPay>>

    /**
     * Get AutoPay schedule by ID
     */
    fun getAutoPaySchedule(autoPayId: Long): ScreenStateStream<AutoPay>

    // Writes throw on failure and return Unit on success.

    /**
     * Create a new AutoPay schedule. Throws on failure.
     */
    suspend fun createAutoPaySchedule(
        payload: AutoPayPayload,
    )

    /**
     * Update an existing AutoPay schedule. Throws on failure.
     */
    suspend fun updateAutoPaySchedule(
        autoPayId: Long,
        payload: AutoPayUpdatePayload,
    )

    /**
     * Delete an AutoPay schedule. Throws on failure.
     */
    suspend fun deleteAutoPaySchedule(autoPayId: Long)

    /**
     * Pause an AutoPay schedule. Throws on failure.
     */
    suspend fun pauseAutoPaySchedule(autoPayId: Long)

    /**
     * Resume a paused AutoPay schedule. Throws on failure.
     */
    suspend fun resumeAutoPaySchedule(autoPayId: Long)

    /**
     * Get AutoPay payment history. Keeps the [Page] wrapper inside Content
     * (mirrors `AutoPayHistoryRepository.getAutoPayHistoryWithPagination`
     * treatment).
     */
    fun getAutoPayHistory(
        autoPayId: Long,
        limit: Int = 20,
    ): ScreenStateStream<Page<AutoPayHistory>>

    /**
     * Get upcoming payments for all AutoPay schedules
     */
    fun getUpcomingPayments(
        clientId: Long,
        limit: Int = 10,
    ): ScreenStateStream<List<UpcomingPayment>>

    /**
     * Get AutoPay statistics for dashboard
     */
    fun getAutoPayStatistics(
        clientId: Long,
    ): ScreenStateStream<AutoPayStatistics>

    /**
     * Validate AutoPay payload before submission (pure-local validation,
     * no I/O). Throws on invalid input.
     */
    suspend fun validateAutoPayPayload(payload: AutoPayPayload)
}

/**
 * Aggregate figures for a client's auto-pay schedules.
 * @property totalActiveSchedules Number of active schedules.
 * @property totalPausedSchedules Number of paused schedules.
 * @property totalCompletedSchedules Number of completed schedules.
 * @property totalUpcomingPayments Number of upcoming payments.
 * @property totalAmountThisMonth Total amount scheduled for this month.
 * @property currency Currency code of the amounts.
 */
data class AutoPayStatistics(
    val totalActiveSchedules: Int = 0,
    val totalPausedSchedules: Int = 0,
    val totalCompletedSchedules: Int = 0,
    val totalUpcomingPayments: Int = 0,
    val totalAmountThisMonth: Double = 0.0,
    val currency: String = "USD",
)
