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
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
/**
 * TODO: Update endpoint paths when backend APIs are finalized, also use
 * Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations
 */
interface BillerService {
    /** Fetches all billers. */
    @GET("billers")
    fun getAllBillers(): Flow<List<Biller>>

    /** Fetches a biller by id. */
    @GET("billers/{billerId}")
    suspend fun getBillerById(@Path("billerId") billerId: String): Flow<Biller>

    /** Creates a biller. */
    @POST("billers")
    suspend fun createBiller(@Body biller: Biller): Flow<Biller>

    /** Updates a biller. */
    @PUT("billers/{billerId}")
    suspend fun updateBiller(
        @Path("billerId") billerId: String,
        @Body biller: Biller,
    ): Flow<Biller>

    /** Deletes a biller. */
    @DELETE("billers/{billerId}")
    suspend fun deleteBiller(@Path("billerId") billerId: String): Flow<Unit>

    /** Fetches billers of the given category. */
    @GET("billers/category/{category}")
    suspend fun getBillersByCategory(
        @Path("category") category: BillerCategory,
    ): Flow<List<Biller>>

    /** Searches billers by name. */
    @GET("billers/search")
    suspend fun searchBillersByName(
        @Query("query") query: String,
    ): Flow<List<Biller>>

    /** Checks whether a biller exists. */
    @GET("billers/exists")
    suspend fun isBillerExists(
        @Query("name") name: String,
        @Query("accountNumber") accountNumber: String,
    ): Flow<Boolean>

    /** Deletes all billers. */
    @DELETE("billers")
    suspend fun clearAllBillers(): Flow<Unit>
}
