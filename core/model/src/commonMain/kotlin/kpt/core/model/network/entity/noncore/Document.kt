/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.noncore

import kotlinx.serialization.Serializable

/**
 * Metadata of a document attached to a Fineract entity.
 *
 * @property id Document id.
 * @property parentEntityType Type of the parent entity.
 * @property parentEntityId Id of the parent entity.
 * @property name Document name.
 * @property fileName File name.
 * @property size File size in bytes.
 * @property type Type.
 * @property description Document description.
 */
@Serializable
data class Document(
    val id: Int = 0,
    val parentEntityType: String? = null,
    val parentEntityId: Int = 0,
    val name: String? = null,
    val fileName: String? = null,
    val size: Long = 0,
    val type: String? = null,
    val description: String? = null,
)
