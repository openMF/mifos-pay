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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.BeneficiaryRepository
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.network.entity.templates.beneficiary.BeneficiaryTemplate
import kpt.core.network.main.services.BeneficiaryService
/** Default [BeneficiaryRepository] backed by the beneficiary API. */
@RepositoryBinding(binds = BeneficiaryRepository::class)
class BeneficiaryRepositoryImpl(
    private val beneficiaryService: BeneficiaryService,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : BeneficiaryRepository {
    override suspend fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>> {
        return beneficiaryService.beneficiaryList()
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun getBeneficiaryTemplate(): Flow<ScreenState<BeneficiaryTemplate>> {
        return beneficiaryService.beneficiaryTemplate()
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun createBeneficiary(
        beneficiaryPayload: BeneficiaryPayload,
    ) {
        withContext(ioDispatcher) {
            beneficiaryService.createBeneficiary(beneficiaryPayload)
        }
    }

    override suspend fun updateBeneficiary(
        beneficiaryId: Long,
        payload: BeneficiaryUpdatePayload,
    ) {
        withContext(ioDispatcher) {
            beneficiaryService.updateBeneficiary(beneficiaryId, payload)
        }
    }

    override suspend fun deleteBeneficiary(beneficiaryId: Long) {
        withContext(ioDispatcher) {
            beneficiaryService.deleteBeneficiary(beneficiaryId)
        }
    }
}
