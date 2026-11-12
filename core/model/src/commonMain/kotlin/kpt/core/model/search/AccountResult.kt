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

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * An account found by a search, with its parent client details.
 *
 * @property entityId Identifier of the matched entity.
 * @property entityAccountNo Account number of the matched entity.
 * @property entityExternalId External identifier of the matched entity.
 * @property entityName Name of the matched entity.
 * @property entityType Type of the matched entity.
 * @property parentId Identifier of the parent entity.
 * @property parentName Name of the parent entity.
 * @property parentType Type of the parent entity.
 * @property subEntityType Sub-type of the matched entity.
 */
@Serializable
@Parcelize
data class AccountResult(
    val entityId: Long,
    val entityAccountNo: String,
    val entityExternalId: String?,
    val entityName: String,
    val entityType: String,
    val parentId: Long,
    val parentName: String,
    val parentType: String,
    val subEntityType: String,
) : Parcelable
