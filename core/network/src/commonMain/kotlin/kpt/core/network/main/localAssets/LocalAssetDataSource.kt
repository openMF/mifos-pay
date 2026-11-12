/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.localAssets

import kpt.core.model.network.entity.signup.City
import kpt.core.model.network.entity.signup.Country
import kpt.core.model.network.entity.signup.State
/** Source of static reference data (countries, states, banks, cities) bundled with the app. */
interface LocalAssetDataSource {

    /** Returns the available countries. */
    suspend fun getCountries(): List<Country>

    /** Returns the available states. */
    suspend fun getStateList(): List<State>

    /** Returns the available bank names. */
    suspend fun getBanks(): List<String>

    /** Returns the available cities. */
    suspend fun getCities(): List<City>
}
