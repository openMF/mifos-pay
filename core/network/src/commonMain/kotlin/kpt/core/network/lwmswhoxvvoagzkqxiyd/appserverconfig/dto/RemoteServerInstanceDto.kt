/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

/**
 * One row of `public.app_server_config` — the Mifos server list, ONE ROW PER INSTANCE. It replaces the
 * deprecated `app_config` table (a single row holding an `instances` blob). The schema is owned by
 * mifos-x-field-officer-app (`supabase/migrations/20261025120000_app_server_config.sql`).
 *
 * @property label human-readable name shown in a server picker.
 * @property protocol scheme including separator, e.g. `https://`.
 * @property endpoint host, e.g. `sandbox.mifos.community`.
 * @property path API base path, e.g. `/fineract-provider/api/v1/`.
 * @property port explicit port, or null for the protocol default.
 * @property platformTenantId the Fineract tenant header value for this instance.
 * @property isDefault whether this is the instance selected when the user has chosen none.
 * @property supportedApps which apps list this instance — the server filters on it.
 * @property demoAccounts published demo sign-ins for a public demo instance.
 * @property interbankServers mifos-pay's interbank list; kept raw until an app needs it typed.
 */
@Serializable
data class RemoteServerInstanceDto(
    val label: String,
    val protocol: String,
    val endpoint: String,
    val path: String,
    val port: String? = null,
    @SerialName("platform_tenant_id") val platformTenantId: String,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("supported_apps") val supportedApps: List<String> = emptyList(),
    @SerialName("demo_accounts") val demoAccounts: List<RemoteDemoAccountDto> = emptyList(),
    @SerialName("interbank_servers") val interbankServers: JsonArray = JsonArray(emptyList()),
)

/**
 * A PUBLISHED demo sign-in of a public demo instance — public by design, not a credential to protect.
 *
 * @property role what the account demonstrates, e.g. `officer`.
 * @property username demo login name.
 * @property password demo password, published alongside the instance.
 */
@Serializable
data class RemoteDemoAccountDto(val role: String, val username: String, val password: String)
