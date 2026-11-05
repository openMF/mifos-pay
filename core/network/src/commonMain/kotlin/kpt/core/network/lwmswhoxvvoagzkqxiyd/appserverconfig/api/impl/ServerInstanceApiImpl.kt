/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.impl

import io.github.jan.supabase.postgrest.query.Order
import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.ServerInstanceApi
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto

/**
 * Supabase implementation of [ServerInstanceApi]. Row-level security limits the publishable key (the
 * `anon` role) to enabled rows, read-only; the `supported_apps` containment filter runs in Postgres (GIN-indexed).
 * Inert without a key: returns empty, never builds a client on an empty URL.
 */
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class ServerInstanceApiImpl(
    private val supabase: SupabaseConfigClient,
) : ServerInstanceApi {

    override val isConfigured: Boolean get() = supabase.isConfigured

    override suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto> {
        if (!isConfigured) return emptyList()
        return supabase.data.from(TABLE).select {
            filter { contains("supported_apps", listOf(app)) }
            order("sort_order", Order.ASCENDING)
        }.decodeList<RemoteServerInstanceDto>()
    }

    private companion object {
        const val TABLE = "app_server_config"
    }
}
