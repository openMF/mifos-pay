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

import kotlinx.coroutines.flow.Flow
import kpt.core.common.ScreenState
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.network.entity.templates.beneficiary.BeneficiaryTemplate
interface BeneficiaryRepository {
    // Phase-3 cutover — reads on ScreenState.
    suspend fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>

    suspend fun getBeneficiaryTemplate(): Flow<ScreenState<BeneficiaryTemplate>>

    // Writes execute the network call and throw on failure (no offline write
    // stores yet). Callers surface errors via their own try/catch.
    suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload)

    suspend fun updateBeneficiary(
        beneficiaryId: Long,
        payload: BeneficiaryUpdatePayload,
    )

    suspend fun deleteBeneficiary(beneficiaryId: Long)
}
