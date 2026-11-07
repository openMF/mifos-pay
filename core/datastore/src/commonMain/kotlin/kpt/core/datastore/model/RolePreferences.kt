/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.model

import kotlinx.serialization.Serializable

/**
 * Persisted snapshot of a user role.
 * @property id Role id.
 * @property name Role name.
 * @property description Role description.
 * @property disabled Whether the role is disabled.
 */
@Serializable
data class RolePreferences(
    val id: String,
    val name: String,
    val description: String,
    val disabled: Boolean,
) {
    /** Holds the default (empty) value. */
    companion object {
        /** Empty role. */
        val DEFAULT = RolePreferences(
            id = "",
            name = "",
            description = "",
            disabled = false,
        )
    }
}
