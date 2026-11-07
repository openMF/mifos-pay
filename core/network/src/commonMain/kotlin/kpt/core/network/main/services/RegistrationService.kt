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
import de.jensklingenberg.ktorfit.http.POST
import kpt.core.model.network.entity.register.RegisterPayload
import kpt.core.model.network.entity.register.UserVerify
import kpt.core.network.main.utils.ApiEndPoints
/** Ktorfit service for user registration. */
interface RegistrationService {
    /** Registers a new user. */
    @POST(ApiEndPoints.REGISTRATION)
    suspend fun registerUser(@Body registerPayload: RegisterPayload)

    /** Verifies a registered user. */
    @POST(ApiEndPoints.REGISTRATION + "/user")
    suspend fun verifyUser(@Body userVerify: UserVerify)
}
