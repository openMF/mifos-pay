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
import kpt.core.data.repository.KycLevelRepository
import kpt.core.model.kyc.KYCLevel1Details
import kpt.core.network.FineractApiManager
class KycLevelRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : KycLevelRepository {
    override fun fetchKYCLevel1Details(
        clientId: Long,
    ): ScreenStateStream<KYCLevel1Details?> {
        return apiManager.kycLevel1Api
            .fetchKYCLevel1Details(clientId)
            .map { it.firstOrNull() }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun addKYCLevel1Details(
        clientId: Long,
        kycLevel1Details: KYCLevel1Details,
    ) {
        withContext(ioDispatcher) {
            apiManager.kycLevel1Api.addKYCLevel1Details(clientId, kycLevel1Details)
        }
    }

    override suspend fun updateKYCLevel1Details(
        clientId: Long,
        kycLevel1Details: KYCLevel1Details,
    ) {
        withContext(ioDispatcher) {
            apiManager.kycLevel1Api.updateKYCLevel1Details(clientId, kycLevel1Details)
        }
    }
}
