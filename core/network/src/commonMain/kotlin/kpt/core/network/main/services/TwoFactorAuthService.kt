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
/** Ktorfit service for two-factor authentication. */
interface TwoFactorAuthService {
    /** Fetches the available OTP delivery methods. */
    @GET(ApiEndPoints.TWOFACTOR)
    suspend fun deliveryMethods(): Flow<List<DeliveryMethod>>

    /** Requests an OTP through the given delivery method. */
    @POST(ApiEndPoints.TWOFACTOR)
    suspend fun requestOTP(@Query("deliveryMethod") deliveryMethod: String): Flow<String>

    /** Validates an OTP token. */
    @POST(ApiEndPoints.TWOFACTOR + "/validate")
    suspend fun validateToken(@Query("token") token: String): Flow<AccessToken>
}
