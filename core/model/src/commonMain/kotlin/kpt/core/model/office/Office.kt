/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.office

import kotlinx.serialization.Serializable

@Serializable
data class Office(
    val id: Long,
    val name: String,
    val nameDecorated: String? = null,
    val externalId: String? = null,
    val openingDate: List<Int>? = null,
    val hierarchy: String? = null,
)
