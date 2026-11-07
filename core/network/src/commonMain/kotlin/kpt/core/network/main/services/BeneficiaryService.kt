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
import kotlinx.coroutines.flow.Flow
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.network.entity.templates.beneficiary.BeneficiaryTemplate
import kpt.core.network.main.utils.ApiEndPoints
/** Ktorfit service for beneficiaries. */
interface BeneficiaryService {
    /** Fetches the list of beneficiaries. */
    @GET(ApiEndPoints.BENEFICIARIES + "/tpt")
    fun beneficiaryList(): Flow<List<Beneficiary>>

    /** Fetches the beneficiary template. */
    @GET(ApiEndPoints.BENEFICIARIES + "/tpt/template")
    suspend fun beneficiaryTemplate(): Flow<BeneficiaryTemplate>

    /** Creates a beneficiary. */
    @POST(ApiEndPoints.BENEFICIARIES + "/tpt")
    suspend fun createBeneficiary(@Body beneficiaryPayload: BeneficiaryPayload)

    /** Updates a beneficiary. */
    @PUT(ApiEndPoints.BENEFICIARIES + "/tpt/{beneficiaryId}")
    suspend fun updateBeneficiary(
        @Path("beneficiaryId") beneficiaryId: Long,
        @Body payload: BeneficiaryUpdatePayload,
    )

    /** Deletes a beneficiary. */
    @DELETE(ApiEndPoints.BENEFICIARIES + "/tpt/{beneficiaryId}")
    suspend fun deleteBeneficiary(@Path("beneficiaryId") beneficiaryId: Long): Unit
}
