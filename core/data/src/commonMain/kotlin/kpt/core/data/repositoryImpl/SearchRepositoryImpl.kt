/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.data.mapper.toSearchResult
import kpt.core.data.repository.SearchRepository
import kpt.core.model.search.SearchResult
import kpt.core.network.FineractApiManager
/** Default [SearchRepository] backed by the search API. */
@RepositoryBinding(binds = SearchRepository::class)
class SearchRepositoryImpl(
    private val apiManager: FineractApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : SearchRepository {
    override suspend fun searchResources(
        query: String,
        resources: String,
        exactMatch: Boolean,
    ): List<SearchResult> {
        return withContext(ioDispatcher) {
            apiManager.searchApi.searchResources(query, resources, exactMatch).toSearchResult()
        }
    }
}
