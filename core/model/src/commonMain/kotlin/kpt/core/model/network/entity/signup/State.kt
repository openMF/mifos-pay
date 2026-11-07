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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A state or province option used in the signup address form.
 *
 * @property id Unique id.
 * @property name Display name.
 * @property countryId Id of the country.
 */
@Serializable
data class State(
    val id: String,
    val name: String,
    @SerialName("country_id")
    val countryId: String,
)
