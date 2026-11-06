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

@Serializable
data class RolePreferences(
    val id: String,
    val name: String,
    val description: String,
    val disabled: Boolean,
) {
    companion object {
        val DEFAULT = RolePreferences(
            id = "",
            name = "",
            description = "",
            disabled = false,
        )
    }
}
