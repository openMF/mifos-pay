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
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.model.network.entity.SearchedEntity
import kpt.core.network.main.utils.ApiEndPoints
/** Ktorfit service for searching resources. */
interface SearchService {
    /** Searches Fineract resources. */
    @GET(ApiEndPoints.SEARCH)
    suspend fun searchResources(
        @Query("query") query: String,
        @Query("resource") resources: String,
        @Query("exactMatch") exactMatch: Boolean,
    ): List<SearchedEntity>
}
