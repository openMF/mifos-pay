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
 * A user role in Fineract.
 *
 * @property id Role id.
 * @property name Role name.
 * @property description Description.
 * @property disabled Whether the role is disabled.
 */
@Serializable
data class Role(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val disabled: Boolean,
)
