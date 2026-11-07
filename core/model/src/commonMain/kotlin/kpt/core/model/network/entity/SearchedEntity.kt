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
 * A result row from the Fineract search API, with its parent entity.
 *
 * @property entityId Id of the entity.
 * @property entityAccountNo Account number of the entity.
 * @property entityName Name of the entity.
 * @property entityType Type of the entity.
 * @property parentId Id of the parent entity.
 * @property parentName Name of the parent entity.
 */
@Serializable
data class SearchedEntity(
    val entityId: Int = 0,
    val entityAccountNo: String = "",
    val entityName: String = "",
    val entityType: String = "",
    val parentId: Int = 0,
    val parentName: String = "",
)
