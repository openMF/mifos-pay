/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.services

import io.github.jan.supabase.postgrest.Postgrest
import kpt.core.model.instance.InstancesConfig
/**
 * Service for interacting with app_config table in Supabase.
 */
class AppConfigService(
    private val postgrest: Postgrest,
) {
    /** Fetches the server instances configuration from the Supabase app_config table. */
    suspend fun fetchInstancesConfig(): InstancesConfig {
        return postgrest
            .from(TABLE_APP_CONFIG)
            .select()
            .decodeSingle<InstancesConfig>()
    }

    /** Name of the Supabase table holding the app configuration. */
    companion object {
        private const val TABLE_APP_CONFIG = "app_config"
    }
}
