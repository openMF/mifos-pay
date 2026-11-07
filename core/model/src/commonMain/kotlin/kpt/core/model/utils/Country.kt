/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.utils

import kotlinx.serialization.Serializable

/**
 * A country with its codes, region and states.
 *
 * @property code2 ISO 3166-1 alpha-2 country code.
 * @property code3 ISO 3166-1 alpha-3 country code.
 * @property name Country name.
 * @property capital Capital city.
 * @property region World region.
 * @property subregion Sub-region.
 * @property states States or provinces of the country.
 */
@Serializable
data class Country(
    val code2: String,
    val code3: String,
    val name: String,
    val capital: String,
    val region: String,
    val subregion: String,
    val states: List<State>,
)

/**
 * A state or province of a country.
 *
 * @property code State code.
 * @property name State name.
 */
@Serializable
data class State(
    val code: String,
    val name: String,
)
