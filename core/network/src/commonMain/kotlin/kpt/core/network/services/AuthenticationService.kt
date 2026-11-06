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
import de.jensklingenberg.ktorfit.http.POST
import kpt.core.model.network.entity.authentication.AuthenticationPayload
import kpt.core.model.network.entity.user.User
import kpt.core.network.utils.ApiEndPoints
interface AuthenticationService {
    @POST(ApiEndPoints.AUTHENTICATION)
    suspend fun authenticate(@Body authPayload: AuthenticationPayload): User
}
