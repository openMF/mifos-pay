/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.search

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * A single hit returned by the server search.
 *
 * @property entityId Identifier of the matched entity.
 * @property entityAccountNo Account number of the matched entity.
 * @property entityName Name of the matched entity.
 * @property entityType Type of the matched entity.
 * @property parentId Identifier of the parent entity.
 * @property parentName Name of the parent entity.
 */
@Parcelize
data class SearchResult(
    val entityId: Int,
    val entityAccountNo: String,
    val entityName: String,
    val entityType: String,
    val parentId: Int,
    val parentName: String,
) : Parcelable
