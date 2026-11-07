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

// TODO(phase-4): Migrate to ScreenState via `createOfflineStore` (Bill
// autopay-bills tier). Deferred from Phase-3 Batch B cutover because bills
// need a real Store5 offline store (SourceOfTruth = Room, fetcher = Fineract
// bills endpoint, bookkeeper for scheduled payment reconciliation) before
// their reads can adopt the fork-wide `ScreenStateStream<T>` envelope.

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.fork.util.BillErrorHandler
import kpt.core.data.fork.util.BillValidator
import kpt.core.data.repository.BillRepository
import kpt.core.data.repository.BillStatistics
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kpt.core.model.autopay.RecurrencePattern
import kpt.core.network.FineractApiManager
import kpt.core.network.main.services.BillStatisticsResponse
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Network-based implementation of BillRepository.
 *
 * TODO: This implementation uses placeholder API endpoints. When the backend APIs
 * for bill management are finalized, update the endpoints and request/response
 * models according to the actual API contract.
 */
class BillRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : BillRepository {

    override fun getAllBills(clientId: Long): ScreenStateStream<List<Bill>> {
        return apiManager.billApi
            .getAllBills(clientId)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun getBillById(id: String): Bill {
        return withContext(ioDispatcher) {
            apiManager.billApi.getBillById(id).first()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun createBill(bill: Bill): Bill {
        val billWithId = bill.copy(
            id = bill.id ?: generateBillId(),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        return withContext(ioDispatcher) {
            apiManager.billApi.createBill(billWithId).first()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateBill(bill: Bill): Bill {
        val billId = requireNotNull(bill.id) { "Bill ID is required for update" }
        val updatedBill = bill.copy(
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        return withContext(ioDispatcher) {
            apiManager.billApi.updateBill(billId, updatedBill).first()
        }
    }

    override suspend fun deleteBill(id: String) {
        withContext(ioDispatcher) {
            apiManager.billApi.deleteBill(id).first()
        }
    }

    override suspend fun getBillsByStatus(status: BillStatus): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                apiManager.billApi.getBillsByStatus(status).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBillsByBillerId(billerId: String): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                apiManager.billApi.getBillsByBillerId(billerId).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBillsByRecurrencePattern(pattern: RecurrencePattern): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                apiManager.billApi.getBillsByRecurrencePattern(pattern).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchBillsByName(query: String): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                apiManager.billApi.searchBillsByName(query).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getOverdueBills(): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                apiManager.billApi.getOverdueBills().first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUpcomingBills(fromDate: Long, toDate: Long): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                apiManager.billApi.getUpcomingBills(fromDate, toDate).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateBillStatus(billId: String, status: BillStatus): Bill {
        return withContext(ioDispatcher) {
            apiManager.billApi.updateBillStatus(billId, status).first()
        }
    }

    override suspend fun validateBill(bill: Bill) {
        val validationResult = BillValidator.validateBill(bill)
        if (!validationResult.isValid) {
            val errorMessage = BillErrorHandler.handleValidationError(validationResult)
            throw IllegalArgumentException(errorMessage)
        }
    }

    override fun getBillStatistics(clientId: Long): ScreenStateStream<BillStatistics> {
        return apiManager.billApi
            .getBillStatistics(clientId)
            .map { response: BillStatisticsResponse ->
                BillStatistics(
                    totalBills = response.totalBills,
                    activeBills = response.activeBills,
                    overdueBills = response.overdueBills,
                    totalAmount = response.totalAmount,
                    currency = response.currency,
                    upcomingPayments = response.upcomingPayments,
                    totalAmountThisMonth = response.totalAmountThisMonth,
                )
            }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun clearAllBills() {
        withContext(ioDispatcher) {
            apiManager.billApi.clearAllBills().first()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun generateBillId(): String {
        return "bill_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
}
