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
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.network.ClientResponseEntity
import kpt.core.model.network.entity.Page
import kpt.core.model.network.entity.client.ClientAccountsEntity
import kpt.core.model.network.entity.client.ClientEntity
import kpt.core.model.network.entity.client.NewClientEntity
import kpt.core.model.network.entity.client.UpdateClientEntity
import kpt.core.network.utils.ApiEndPoints
interface ClientService {
    @GET(ApiEndPoints.CLIENTS)
    suspend fun clients(): Flow<Page<ClientEntity>>

    @GET(ApiEndPoints.CLIENTS + "/{clientId}")
    suspend fun getClientForId(@Path("clientId") clientId: Long): ClientEntity

    @GET(ApiEndPoints.CLIENTS + "/{clientId}")
    fun getClient(@Path("clientId") clientId: Long): Flow<ClientEntity>

    @PUT(ApiEndPoints.CLIENTS + "/{clientId}")
    suspend fun updateClient(
        @Path("clientId") clientId: Long,
        @Body payload: UpdateClientEntity,
    ): Unit

    @GET(ApiEndPoints.CLIENTS + "/{clientId}/images")
    @Headers("Accept: text/plain")
    fun getClientImage(@Path("clientId") clientId: Long): Flow<String>

    @PUT(ApiEndPoints.CLIENTS + "/{clientId}/images")
    suspend fun updateClientImage(
        @Path("clientId") clientId: Long,
        @Body typedFile: String,
    ): Unit

    @GET(ApiEndPoints.CLIENTS + "/{clientId}/accounts")
    suspend fun getClientAccounts(@Path("clientId") clientId: Long): ClientAccountsEntity

    @GET(ApiEndPoints.CLIENTS + "/{clientId}/accounts")
    fun getAccounts(
        @Path("clientId") clientId: Long,
        @Query("fields") accountType: String,
    ): Flow<ClientAccountsEntity>

    @POST(ApiEndPoints.CLIENTS)
    suspend fun createClient(@Body newClient: NewClientEntity): ClientResponseEntity

    @DELETE(ApiEndPoints.CLIENTS + "/{clientId}")
    suspend fun deleteClient(@Path("clientId") clientId: Int): ClientResponseEntity
}
