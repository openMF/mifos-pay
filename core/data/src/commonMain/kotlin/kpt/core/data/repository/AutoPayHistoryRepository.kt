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
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.network.entity.Page
// TODO: Align repository with final API response/request schema once confirmed by backend

interface AutoPayHistoryRepository {
    /**
     * Get AutoPay history for a specific AutoPay schedule
     */
    fun getAutoPayHistory(autoPayId: Long): ScreenStateStream<List<AutoPayHistory>>

    /**
     * Get AutoPay history with pagination.
     * Keeps the [Page] wrapper inside [kpt.core.common.ScreenState.Content]
     * (mirrors the Batch A `SelfServiceRepository.getSelfClientDetails(): Page<Client>`
     * treatment); consumers read `.data.pageItems` when they need the list.
     */
    fun getAutoPayHistoryWithPagination(
        autoPayId: Long,
        limit: Int = 20,
        offset: Int = 0,
    ): ScreenStateStream<Page<AutoPayHistory>>

    /**
     * Get AutoPay history by ID (point-lookup). Returns the entry or throws
     * on failure / when the lookup is unsupported.
     */
    suspend fun getHistoryById(id: Long): AutoPayHistory

    /**
     * Get AutoPay history by status
     */
    fun getHistoryByStatus(status: String): ScreenStateStream<List<AutoPayHistory>>

    /**
     * Get AutoPay history by date range
     */
    fun getHistoryByDateRange(
        fromDate: String,
        toDate: String,
    ): ScreenStateStream<List<AutoPayHistory>>

    /**
     * Search AutoPay history
     */
    fun searchHistory(query: String): ScreenStateStream<List<AutoPayHistory>>

    /**
     * Get history statistics
     */
    fun getHistoryStatistics(autoPayId: Long): ScreenStateStream<AutoPayHistoryStatistics>
}

data class AutoPayHistoryStatistics(
    val totalTransactions: Int = 0,
    val successfulTransactions: Int = 0,
    val failedTransactions: Int = 0,
    val pendingTransactions: Int = 0,
    val totalAmount: Double = 0.0,
    val successfulAmount: Double = 0.0,
    val failedAmount: Double = 0.0,
    val currency: String = "USD",
)
