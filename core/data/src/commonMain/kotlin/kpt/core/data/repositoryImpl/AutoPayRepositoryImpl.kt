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
import kotlinx.coroutines.withContext
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.AutoPayRepository
import kpt.core.data.repository.AutoPayStatistics
import kpt.core.data.util.AutoPayValidator
import kpt.core.model.autopay.AutoPay
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.AutoPayPayload
import kpt.core.model.autopay.AutoPayTemplate
import kpt.core.model.autopay.AutoPayUpdatePayload
import kpt.core.model.autopay.UpcomingPayment
import kpt.core.model.network.entity.Page
import kpt.core.network.FineractApiManager
class AutoPayRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : AutoPayRepository {

    override fun getAutoPayTemplate(
        clientId: Long,
        sourceAccountId: Long,
    ): ScreenStateStream<AutoPayTemplate> {
        return apiManager.autoPayApi
            .getAutoPayTemplate(clientId, sourceAccountId)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override fun getAllAutoPaySchedules(
        clientId: Long,
    ): ScreenStateStream<List<AutoPay>> {
        return apiManager.autoPayApi
            .getAllAutoPaySchedules(clientId)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getAutoPaySchedule(
        autoPayId: Long,
    ): ScreenStateStream<AutoPay> {
        return apiManager.autoPayApi
            .getAutoPaySchedule(autoPayId)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun createAutoPaySchedule(
        payload: AutoPayPayload,
    ) {
        withContext(ioDispatcher) {
            apiManager.autoPayApi.createAutoPaySchedule(payload)
        }
    }

    override suspend fun updateAutoPaySchedule(
        autoPayId: Long,
        payload: AutoPayUpdatePayload,
    ) {
        withContext(ioDispatcher) {
            apiManager.autoPayApi.updateAutoPaySchedule(
                autoPayId = autoPayId,
                payload = payload,
            )
        }
    }

    override suspend fun deleteAutoPaySchedule(
        autoPayId: Long,
    ) {
        withContext(ioDispatcher) {
            apiManager.autoPayApi.deleteAutoPaySchedule(autoPayId)
        }
    }

    override suspend fun pauseAutoPaySchedule(
        autoPayId: Long,
    ) {
        withContext(ioDispatcher) {
            apiManager.autoPayApi.pauseAutoPaySchedule(autoPayId)
        }
    }

    override suspend fun resumeAutoPaySchedule(
        autoPayId: Long,
    ) {
        withContext(ioDispatcher) {
            apiManager.autoPayApi.resumeAutoPaySchedule(autoPayId)
        }
    }

    override fun getAutoPayHistory(
        autoPayId: Long,
        limit: Int,
    ): ScreenStateStream<Page<AutoPayHistory>> {
        return apiManager.autoPayApi
            .getAutoPayHistory(autoPayId, limit)
            .asScreenStateFlow(isEmpty = { it.pageItems.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getUpcomingPayments(
        clientId: Long,
        limit: Int,
    ): ScreenStateStream<List<UpcomingPayment>> {
        return apiManager.autoPayApi
            .getUpcomingPayments(clientId, limit)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getAutoPayStatistics(
        clientId: Long,
    ): ScreenStateStream<AutoPayStatistics> {
        return apiManager.autoPayApi
            .getAutoPayStatistics(clientId)
            .map { response ->
                AutoPayStatistics(
                    totalActiveSchedules = response.totalActiveSchedules,
                    totalPausedSchedules = response.totalPausedSchedules,
                    totalCompletedSchedules = response.totalCompletedSchedules,
                    totalUpcomingPayments = response.totalUpcomingPayments,
                    totalAmountThisMonth = response.totalAmountThisMonth,
                    currency = response.currency,
                )
            }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun validateAutoPayPayload(
        payload: AutoPayPayload,
    ) {
        withContext(ioDispatcher) {
            when (val validationResult = AutoPayValidator.validateAutoPayPayload(payload)) {
                is AutoPayValidator.ValidationResult.Valid -> Unit
                is AutoPayValidator.ValidationResult.Invalid -> {
                    throw IllegalArgumentException(validationResult.errorMessage)
                }
            }
        }
    }
}
