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
 * A Fineract user together with the roles selected for that user.
 *
 * @property id Unique identifier.
 * @property username Login username.
 * @property firstname First name.
 * @property lastname Last name.
 * @property email Email address.
 * @property selectedRoles Roles selected for the user.
 */
@Serializable
data class UserWithRole(
    val id: String? = null,
    val username: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val email: String? = null,
    val selectedRoles: List<Role>? = ArrayList(),
)
