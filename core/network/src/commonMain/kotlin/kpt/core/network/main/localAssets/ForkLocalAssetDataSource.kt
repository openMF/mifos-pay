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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import kpt.core.model.network.entity.signup.City
import kpt.core.model.network.entity.signup.Country
import kpt.core.model.network.entity.signup.State
/** Default [LocalAssetDataSource]; currently returns empty lists. */
class ForkLocalAssetDataSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
) : LocalAssetDataSource {

    override suspend fun getCountries(): List<Country> {
        return emptyList()
    }

    override suspend fun getStateList(): List<State> {
        return emptyList()
    }

    override suspend fun getBanks(): List<String> {
        return emptyList()
    }

    override suspend fun getCities(): List<City> {
        return emptyList()
    }

    /** Names of the bundled asset files. */
    @Suppress("UnusedPrivateProperty")
    companion object {
        private const val COUNTRIES_ASSET = "countries.json"
        private const val STATES_ASSET = "states.json"
        private const val CITIES_ASSET = "cities.json"
        private const val BANKS_ASSET = "banks.json"
        private const val COUNTRIES_TO_CITIES_ASSET = "countriesToCities.json"
    }
}
