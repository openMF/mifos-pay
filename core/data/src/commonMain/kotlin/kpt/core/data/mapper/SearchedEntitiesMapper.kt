/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.mapper

import kpt.core.model.network.entity.SearchedEntity
import kpt.core.model.search.SearchResult
/** Maps a searched entity to a domain [SearchResult]. */
fun SearchedEntity.toModel(): SearchResult {
    return SearchResult(
        entityId = entityId,
        entityAccountNo = entityAccountNo,
        entityName = entityName,
        entityType = entityType,
        parentId = parentId,
        parentName = parentName,
    )
}

/** Maps a list of searched entities to domain [SearchResult]s. */
fun List<SearchedEntity>.toSearchResult(): List<SearchResult> = map { it.toModel() }
