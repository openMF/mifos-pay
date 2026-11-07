/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity

import kotlinx.serialization.Serializable

/**
 * Generic paged Fineract response wrapper.
 *
 * @property totalFilteredRecords Total records matching the filter.
 * @property pageItems Items on this page.
 */
@Serializable
data class Page<T>(
    val totalFilteredRecords: Int = 0,
    val pageItems: List<T> = emptyList(),
)
