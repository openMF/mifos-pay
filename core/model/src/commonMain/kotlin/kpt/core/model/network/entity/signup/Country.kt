/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.signup

import kotlinx.serialization.Serializable

/**
 * A country option used in the signup address form.
 *
 * @property id Unique id.
 * @property sortname Short country code.
 * @property name Display name.
 */
@Serializable
data class Country(
    val id: String,
    val sortname: String,
    val name: String,
)
