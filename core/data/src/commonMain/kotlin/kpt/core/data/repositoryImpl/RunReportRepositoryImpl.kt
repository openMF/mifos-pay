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
import kpt.core.data.mapper.toModel
import kpt.core.data.repository.RunReportRepository
import kpt.core.model.savingsaccount.Transaction
import kpt.core.network.FineractApiManager
class RunReportRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : RunReportRepository {
    override suspend fun getTransactionReceipt(
        outputType: String,
        transactionId: String,
    ): ScreenStateStream<Transaction> {
        return apiManager.runReportApi
            .getTransactionReceipt(outputType, transactionId)
            .map { it.toModel() }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }
}
