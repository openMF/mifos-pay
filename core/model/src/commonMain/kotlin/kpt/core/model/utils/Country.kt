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

@Serializable
data class State(
    val code: String,
    val name: String,
)
