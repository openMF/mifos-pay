/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.payload

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
data class DataTablePayload(
    @Transient
    val id: Int? = null,
    @Transient
    val clientCreationTime: Long? = null,
    @Transient
    val dataTableString: String? = null,
    val registeredTableName: String? = null,
    val applicationTableName: String? = null,
    val data: HashMap<String, JsonElement>? = null,
)
