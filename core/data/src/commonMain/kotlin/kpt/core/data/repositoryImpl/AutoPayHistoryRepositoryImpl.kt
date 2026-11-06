/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.AutoPayHistoryRepository
import kpt.core.data.repository.AutoPayHistoryStatistics
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.PaymentStatus
import kpt.core.model.network.entity.Page
import kpt.core.network.FineractApiManager
// TODO: Align repository with final API response/request schema once confirmed by backend

class AutoPayHistoryRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : AutoPayHistoryRepository {

    override fun getAutoPayHistory(autoPayId: Long): ScreenStateStream<List<AutoPayHistory>> {
        return apiManager.autoPayApi.getAutoPayHistory(autoPayId, 100)
            .map { page -> page.pageItems }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getAutoPayHistoryWithPagination(
        autoPayId: Long,
        limit: Int,
        offset: Int,
    ): ScreenStateStream<Page<AutoPayHistory>> {
        return apiManager.autoPayApi.getAutoPayHistory(autoPayId, limit)
            .asScreenStateFlow(isEmpty = { it.pageItems.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun getHistoryById(id: Long): AutoPayHistory {
        throw UnsupportedOperationException("Individual history lookup not supported in read-only mode")
    }

    override fun getHistoryByStatus(status: String): ScreenStateStream<List<AutoPayHistory>> {
        return apiManager.autoPayApi.getAutoPayHistory(0, 100) // Get all and filter client-side
            .map { page ->
                page.pageItems.filter { it.status?.name == status }
            }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getHistoryByDateRange(
        fromDate: String,
        toDate: String,
    ): ScreenStateStream<List<AutoPayHistory>> {
        return apiManager.autoPayApi.getAutoPayHistory(0, 100) // Get all and filter client-side
            .map { page ->
                page.pageItems.filter { history ->
                    val transactionDate = history.transactionDate
                    transactionDate != null && transactionDate >= fromDate && transactionDate <= toDate
                }
            }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun searchHistory(query: String): ScreenStateStream<List<AutoPayHistory>> {
        return apiManager.autoPayApi.getAutoPayHistory(0, 100) // Get all and filter client-side
            .map { page ->
                page.pageItems.filter { history ->
                    history.recipientName?.contains(query, ignoreCase = true) == true ||
                        history.referenceNumber?.contains(query, ignoreCase = true) == true
                }
            }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getHistoryStatistics(autoPayId: Long): ScreenStateStream<AutoPayHistoryStatistics> {
        return apiManager.autoPayApi.getAutoPayHistory(autoPayId, 100)
            .map { page ->
                val historyList = page.pageItems

                val totalCount = historyList.size
                val successfulCount = historyList.count { it.status == PaymentStatus.COMPLETED }
                val failedCount = historyList.count { it.status == PaymentStatus.FAILED }
                val pendingCount = historyList.count { it.status == PaymentStatus.PENDING }

                AutoPayHistoryStatistics(
                    totalTransactions = totalCount,
                    successfulTransactions = successfulCount,
                    failedTransactions = failedCount,
                    pendingTransactions = pendingCount,
                )
            }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }
}
