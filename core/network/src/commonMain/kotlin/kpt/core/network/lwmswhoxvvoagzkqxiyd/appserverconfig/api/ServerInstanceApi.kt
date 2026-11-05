/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api

import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto

/**
 * The `app_server_config` table's API — one row per server instance. The CONTRACT, no Supabase types:
 * a consumer is tested with a plain fake; [impl.ServerInstanceApiImpl] is the only Supabase-aware part.
 */
interface ServerInstanceApi {
    /** True when a real project URL + publishable key are configured. */
    val isConfigured: Boolean

    /**
     * The enabled instances whose `supported_apps` contains [app], in `sort_order` — filtered BY THE
     * SERVER, so one app never downloads another's servers. Empty when unconfigured.
     */
    suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto>
}
