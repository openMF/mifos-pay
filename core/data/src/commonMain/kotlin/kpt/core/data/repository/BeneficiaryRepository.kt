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
/** Repository for the beneficiaries a client can transfer to. */
interface BeneficiaryRepository {
    // Phase-3 cutover — reads on ScreenState.
    /** Streams the client's saved beneficiaries. */
    suspend fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>

    /** Streams the template used to create or update beneficiaries. */
    suspend fun getBeneficiaryTemplate(): Flow<ScreenState<BeneficiaryTemplate>>

    // Writes execute the network call and throw on failure (no offline write
    // stores yet). Callers surface errors via their own try/catch.
    /** Creates a new beneficiary from the payload. */
    suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload)

    /** Updates an existing beneficiary. */
    suspend fun updateBeneficiary(
        beneficiaryId: Long,
        payload: BeneficiaryUpdatePayload,
    )

    /** Deletes the beneficiary with the given id. */
    suspend fun deleteBeneficiary(beneficiaryId: Long)
}
