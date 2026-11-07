/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import org.mifos.lib.loan.network.model.LoanAccountApplicationPayloadDto
import org.mifos.lib.loan.network.model.LoanTemplateResponseDto

/** Ktorfit API for the Fineract loans endpoints. */
interface LoansService {
    /** Fetches the individual loan template for [clientId]. */
    @GET(ApiEndPoints.LOANS + "/template?templateType=individual")
    fun getLoanTemplate(@Query("clientId") clientId: Long?): Flow<LoanTemplateResponseDto>

    /** Fetches the individual loan template for [clientId] and [productId]. */
    @GET(ApiEndPoints.LOANS + "/template?templateType=individual")
    fun getLoanTemplateByProduct(
        @Query("clientId") clientId: Long?,
        @Query("productId") productId: Long?,
    ): Flow<LoanTemplateResponseDto>

    /** Submits a new loan application with [payload]. */
    @POST(ApiEndPoints.LOANS)
    suspend fun createLoansAccount(@Body payload: LoanAccountApplicationPayloadDto)
}
