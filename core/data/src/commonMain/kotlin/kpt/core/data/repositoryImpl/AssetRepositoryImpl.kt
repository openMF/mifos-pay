/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.generated.resources.Res
import kpt.core.data.repository.AssetRepository
import kpt.core.model.utils.Country
import org.jetbrains.compose.resources.ExperimentalResourceApi
/** Default [AssetRepository]. */
@RepositoryBinding(binds = AssetRepository::class)
class AssetRepositoryImpl : AssetRepository {
    @OptIn(ExperimentalResourceApi::class)
    override fun getCountriesWithStates(): ScreenStateStream<Map<String, List<String>>> {
        return flow {
            val json = Json { ignoreUnknownKeys = true }
            val bytes = Res.readBytes("files/countries.json")
            val jsonString = bytes.decodeToString()
            val countries = json.decodeFromString<List<Country>>(jsonString)
            emit(
                countries.associate { country ->
                    country.name to country.states.map { it.name }
                },
            )
        }.asScreenStateFlow(isEmpty = { it.isEmpty() })
    }
}
