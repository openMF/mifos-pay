/*
 * Copyright 2026 Mifos Initiative
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
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.model.network.entity.pocket.PocketCommandResponse
import kpt.core.model.network.entity.pocket.PocketDelinkRequest
import kpt.core.model.network.entity.pocket.PocketLinkRequest
import kpt.core.model.network.entity.pocket.PocketResponseDto
import kpt.core.network.utils.ApiEndPoints
interface PocketService {
    @GET(ApiEndPoints.POCKETS)
    suspend fun getPocketAccounts(): PocketResponseDto

    @POST(ApiEndPoints.POCKETS)
    suspend fun linkAccounts(
        @Query("command") command: String = "linkAccounts",
        @Body request: PocketLinkRequest,
    ): PocketCommandResponse

    @POST(ApiEndPoints.POCKETS)
    suspend fun delinkAccounts(
        @Query("command") command: String = "delinkAccounts",
        @Body request: PocketDelinkRequest,
    ): PocketCommandResponse
}
