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

/**
 * Payload for a Fineract data table entry; transient fields are local bookkeeping and are not serialized.
 *
 * @property id Unique id.
 * @property clientCreationTime Local creation time of the client (not serialized).
 * @property dataTableString Local serialized form of the table (not serialized).
 * @property registeredTableName Registered data table name.
 * @property applicationTableName Table the data table is attached to.
 * @property data Column values keyed by column name.
 */
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
