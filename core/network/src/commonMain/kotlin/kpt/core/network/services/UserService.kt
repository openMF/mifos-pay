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
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import kotlinx.coroutines.flow.Flow
import kpt.core.model.network.CommonResponse
import kpt.core.model.network.GenericResponse
import kpt.core.model.network.entity.UserWithRole
import kpt.core.model.network.entity.user.NewUserEntity
import kpt.core.model.network.entity.user.UpdateUserEntityPassword
import kpt.core.model.network.entity.user.UpdateUserPasswordResponse
import kpt.core.network.utils.ApiEndPoints
interface UserService {
    @GET(ApiEndPoints.USER)
    suspend fun users(): Flow<List<UserWithRole>>

    @POST(ApiEndPoints.USER)
    suspend fun createUser(@Body user: NewUserEntity): CommonResponse

    @PUT(ApiEndPoints.USER + "/{userId}")
    suspend fun updateUser(
        @Body updateUserEntity: NewUserEntity,
    ): Flow<GenericResponse>

    @PUT(ApiEndPoints.USER_SELF)
    suspend fun updateUserPassword(
        @Body updateUserEntity: UpdateUserEntityPassword,
    ): UpdateUserPasswordResponse

    @DELETE(ApiEndPoints.USER + "/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: Int,
    ): CommonResponse

    @GET("self/userdetails")
    suspend fun getUser(): Flow<UserWithRole>

    @PUT(ApiEndPoints.USER + "/{userId}")
    suspend fun assignClientToUser(
        @Path("userId") userId: Int,
        @Body clients: Map<String, List<Int>>,
    )
}
