/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network

import kpt.core.network.main.services.AppConfigService
import org.mifos.corebase.network.SupabaseConfigClient
/**
 * API manager for Supabase services.
 * Similar to FineractApiManager but for Supabase-based APIs.
 */
class SupabaseApiManager(
    private val supabaseClient: SupabaseConfigClient,
) {
    val appConfigService by lazy {
        AppConfigService(supabaseClient.postgrest)
    }

    val isConfigured: Boolean get() = supabaseClient.isConfigured
}
