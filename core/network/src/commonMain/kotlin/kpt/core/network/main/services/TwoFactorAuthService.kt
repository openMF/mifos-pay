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

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.network.twofactor.AccessToken
import kpt.core.model.network.twofactor.DeliveryMethod
import kpt.core.network.main.utils.ApiEndPoints
interface TwoFactorAuthService {
    @GET(ApiEndPoints.TWOFACTOR)
    suspend fun deliveryMethods(): Flow<List<DeliveryMethod>>

    @POST(ApiEndPoints.TWOFACTOR)
    suspend fun requestOTP(@Query("deliveryMethod") deliveryMethod: String): Flow<String>

    @POST(ApiEndPoints.TWOFACTOR + "/validate")
    suspend fun validateToken(@Query("token") token: String): Flow<AccessToken>
}
