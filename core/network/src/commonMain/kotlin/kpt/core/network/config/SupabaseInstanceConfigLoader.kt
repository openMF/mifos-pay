/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.model.instance.InstancesConfig
import kpt.core.network.SupabaseApiManager
import mifos_pay.core.network.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
/**
 * Instance config loader that fetches configuration from Supabase.
 * Uses SupabaseApiManager for API interactions.
 */
class SupabaseInstanceConfigLoader(
    private val supabaseApiManager: SupabaseApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : InstanceConfigLoader {

    override suspend fun fetchInstancesConfig(): InstancesConfig {
        if (!supabaseApiManager.isConfigured) {
            return loadFallbackConfig()
        }
        return try {
            supabaseApiManager.appConfigService.fetchInstancesConfig()
        } catch (e: Exception) {
            loadFallbackConfig()
        }
    }

    override fun observeInstancesConfig(): Flow<ScreenState<InstancesConfig>> = flow {
        emit(fetchInstancesConfig())
    }.asScreenStateFlow().flowOn(ioDispatcher)

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadFallbackConfig(): InstancesConfig {
        return try {
            val bytes = Res.readBytes("files/instances_config_default.json")
            val jsonString = bytes.decodeToString()
            json.decodeFromString<InstancesConfig>(jsonString)
        } catch (e: Exception) {
            DEFAULT_CONFIG
        }
    }

    companion object {
        private val DEFAULT_CONFIG = InstancesConfig(instances = emptyList())
    }
}
