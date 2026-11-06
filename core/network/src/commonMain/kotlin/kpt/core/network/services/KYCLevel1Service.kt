/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.services

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import kotlinx.coroutines.flow.Flow
import kpt.core.model.kyc.KYCLevel1Details
import kpt.core.network.utils.ApiEndPoints
interface KYCLevel1Service {

    @GET(ApiEndPoints.DATATABLES + "/kyc_level1_details/{clientId}")
    fun fetchKYCLevel1Details(@Path("clientId") clientId: Long): Flow<List<KYCLevel1Details>>

    @POST(ApiEndPoints.DATATABLES + "/kyc_level1_details/{clientId}")
    suspend fun addKYCLevel1Details(
        @Path("clientId") clientId: Long,
        @Body kycLevel1Details: KYCLevel1Details,
    ): Unit

    @PUT(ApiEndPoints.DATATABLES + "/kyc_level1_details/{clientId}/")
    suspend fun updateKYCLevel1Details(
        @Path("clientId") clientId: Long,
        @Body kycLevel1Details: KYCLevel1Details,
    ): Unit
}
