/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.services

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kpt.core.model.autopay.RecurrencePattern
/**
 * TODO: Update endpoint paths when backend APIs are finalized, also use
 * Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations
 */
interface BillService {
    /** Fetches all bills of a client. */
    @GET("bills")
    fun getAllBills(@Query("clientId") clientId: Long): Flow<List<Bill>>

    /** Fetches a bill by id. */
    @GET("bills/{billId}")
    suspend fun getBillById(@Path("billId") billId: String): Flow<Bill>

    /** Creates a bill. */
    @POST("bills")
    suspend fun createBill(@Body bill: Bill): Flow<Bill>

    /** Updates a bill. */
    @PUT("bills/{billId}")
    suspend fun updateBill(
        @Path("billId") billId: String,
        @Body bill: Bill,
    ): Flow<Bill>

    /** Deletes a bill. */
    @DELETE("bills/{billId}")
    suspend fun deleteBill(@Path("billId") billId: String): Flow<Unit>

    /** Fetches bills with the given status. */
    @GET("bills/status/{status}")
    suspend fun getBillsByStatus(@Path("status") status: BillStatus): Flow<List<Bill>>

    /** Fetches bills of the given biller. */
    @GET("bills/biller/{billerId}")
    suspend fun getBillsByBillerId(@Path("billerId") billerId: String): Flow<List<Bill>>

    /** Fetches bills with the given recurrence pattern. */
    @GET("bills/recurrence/{pattern}")
    suspend fun getBillsByRecurrencePattern(@Path("pattern") pattern: RecurrencePattern): Flow<List<Bill>>

    /** Searches bills by name. */
    @GET("bills/search")
    suspend fun searchBillsByName(@Query("query") query: String): Flow<List<Bill>>

    /** Fetches overdue bills. */
    @GET("bills/overdue")
    suspend fun getOverdueBills(): Flow<List<Bill>>

    /** Fetches upcoming bills. */
    @GET("bills/upcoming")
    suspend fun getUpcomingBills(
        @Query("fromDate") fromDate: Long,
        @Query("toDate") toDate: Long,
    ): Flow<List<Bill>>

    /** Updates the status of a bill. */
    @PUT("bills/{billId}/status")
    suspend fun updateBillStatus(
        @Path("billId") billId: String,
        @Query("status") status: BillStatus,
    ): Flow<Bill>

    /** Fetches aggregate bill statistics for a client. */
    @GET("bills/statistics")
    fun getBillStatistics(@Query("clientId") clientId: Long): Flow<BillStatisticsResponse>

    /** Deletes all bills. */
    @DELETE("bills")
    suspend fun clearAllBills(): Flow<Unit>
}

/**
 * Network response with aggregate bill figures.
 * @property totalBills Total number of bills.
 * @property activeBills Number of active bills.
 * @property overdueBills Number of overdue bills.
 * @property totalAmount Total amount across bills.
 * @property currency Currency code of the amounts.
 * @property upcomingPayments Number of upcoming payments.
 * @property totalAmountThisMonth Total amount due this month.
 */
data class BillStatisticsResponse(
    val totalBills: Int = 0,
    val activeBills: Int = 0,
    val overdueBills: Int = 0,
    val totalAmount: Double = 0.0,
    val currency: String = "USD",
    val upcomingPayments: Int = 0,
    val totalAmountThisMonth: Double = 0.0,
)
