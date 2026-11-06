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
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.autopay.AutoPay
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.AutoPayPayload
import kpt.core.model.autopay.AutoPayTemplate
import kpt.core.model.autopay.AutoPayUpdatePayload
import kpt.core.model.autopay.UpcomingPayment
import kpt.core.model.network.entity.Page
import kpt.core.network.main.utils.ApiEndPoints
/**
* TODO: Sync with backend team and update service layer according to finalized API contract,
* also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations
*/

interface AutoPayService {

    /**
     * Get AutoPay template for creating new AutoPay schedules
     */
    @GET("${ApiEndPoints.AUTO_PAY}/template")
    fun getAutoPayTemplate(
        @Query("clientId") clientId: Long,
        @Query("sourceAccountId") sourceAccountId: Long,
    ): Flow<AutoPayTemplate>

    /**
     * Get all AutoPay schedules for a client
     */
    @GET(ApiEndPoints.AUTO_PAY)
    fun getAllAutoPaySchedules(
        @Query("clientId") clientId: Long,
    ): Flow<List<AutoPay>>

    /**
     * Get AutoPay schedule by ID
     */
    @GET("${ApiEndPoints.AUTO_PAY}/{autoPayId}")
    fun getAutoPaySchedule(
        @Path("autoPayId") autoPayId: Long,
    ): Flow<AutoPay>

    /**
     * Create a new AutoPay schedule
     */
    @POST(ApiEndPoints.AUTO_PAY)
    suspend fun createAutoPaySchedule(
        @Body payload: AutoPayPayload,
    )

    /**
     * Update an existing AutoPay schedule
     */
    @PUT("${ApiEndPoints.AUTO_PAY}/{autoPayId}")
    suspend fun updateAutoPaySchedule(
        @Path("autoPayId") autoPayId: Long,
        @Body payload: AutoPayUpdatePayload,
        @Query("command") command: String = "update",
    )

    /**
     * Delete an AutoPay schedule
     */
    @PUT("${ApiEndPoints.AUTO_PAY}/{autoPayId}")
    suspend fun deleteAutoPaySchedule(
        @Path("autoPayId") autoPayId: Long,
        @Query("command") command: String = "delete",
    )

    /**
     * Pause an AutoPay schedule
     */
    @PUT("${ApiEndPoints.AUTO_PAY}/{autoPayId}")
    suspend fun pauseAutoPaySchedule(
        @Path("autoPayId") autoPayId: Long,
        @Query("command") command: String = "pause",
    )

    /**
     * Resume a paused AutoPay schedule
     */
    @PUT("${ApiEndPoints.AUTO_PAY}/{autoPayId}")
    suspend fun resumeAutoPaySchedule(
        @Path("autoPayId") autoPayId: Long,
        @Query("command") command: String = "resume",
    )

    /**
     * Get AutoPay payment history
     */
    @GET("${ApiEndPoints.AUTO_PAY}/{autoPayId}/history")
    fun getAutoPayHistory(
        @Path("autoPayId") autoPayId: Long,
        @Query("limit") limit: Int = 20,
    ): Flow<Page<AutoPayHistory>>

    /**
     * Get upcoming payments for all AutoPay schedules
     */
    @GET("${ApiEndPoints.AUTO_PAY}/upcoming-payments")
    fun getUpcomingPayments(
        @Query("clientId") clientId: Long,
        @Query("limit") limit: Int = 10,
    ): Flow<List<UpcomingPayment>>

    /**
     * Get AutoPay statistics for dashboard
     */
    @GET("${ApiEndPoints.AUTO_PAY}/statistics")
    fun getAutoPayStatistics(
        @Query("clientId") clientId: Long,
    ): Flow<AutoPayStatisticsResponse>
}

data class AutoPayStatisticsResponse(
    val totalActiveSchedules: Int = 0,
    val totalPausedSchedules: Int = 0,
    val totalCompletedSchedules: Int = 0,
    val totalUpcomingPayments: Int = 0,
    val totalAmountThisMonth: Double = 0.0,
    val currency: String = "USD",
)
