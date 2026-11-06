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
import kpt.core.model.savedcards.CardPayload
import kpt.core.model.savedcards.SavedCard
import kpt.core.network.utils.ApiEndPoints
interface SavedCardService {

    @GET(ApiEndPoints.DATATABLES + "/saved_cards/{clientId}")
    fun getSavedCards(@Path("clientId") clientId: Long): Flow<List<SavedCard>>

    @GET(ApiEndPoints.DATATABLES + "/saved_cards/{clientId}/{cardId}")
    fun getSavedCard(
        @Path("clientId") clientId: Long,
        @Path("cardId") cardId: Long,
    ): Flow<List<SavedCard>>

    @POST(ApiEndPoints.DATATABLES + "/saved_cards/{clientId}")
    suspend fun addSavedCard(
        @Path("clientId") clientId: Long,
        @Body card: CardPayload,
    ): Unit

    @PUT(ApiEndPoints.DATATABLES + "/saved_cards/{clientId}/{cardId}")
    suspend fun updateCard(
        @Path("clientId") clientId: Long,
        @Path("cardId") cardId: Long,
        @Body card: CardPayload,
    ): Unit

    @DELETE(ApiEndPoints.DATATABLES + "/saved_cards/{clientId}/{cardId}")
    suspend fun deleteCard(
        @Path("clientId") clientId: Long,
        @Path("cardId") cardId: Long,
    ): Unit
}
