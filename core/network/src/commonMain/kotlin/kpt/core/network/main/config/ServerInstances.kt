/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.config

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kpt.core.model.instance.InstancesConfig
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.network.generated.resources.Res
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** This app's id in `app_server_config.supported_apps`: the server returns only the rows tagged with it. */
const val SERVER_INSTANCES_APP_ID = "mifos-pay"

private val json = Json { ignoreUnknownKeys = true }

/** One `app_server_config` row as the wallet's [ServerInstance], its inter-bank list included. */
fun RemoteServerInstanceDto.toServerInstance(): ServerInstance = ServerInstance(
    endpoint = endpoint,
    protocol = protocol,
    path = path,
    platformTenantId = platformTenantId,
    label = label,
    isDefault = isDefault,
    interbankServers = json.decodeFromJsonElement(ListSerializer(InterbankServer.serializer()), interbankServers),
)

/**
 * The server list shipped in the app (`files/instances_config_default.json`): what the picker offers with
 * no Supabase key, or when the table holds no row for this app. Empty if the file cannot be read.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun bundledServerInstances(): List<ServerInstance> = runCatching {
    json.decodeFromString<InstancesConfig>(Res.readBytes("files/instances_config_default.json").decodeToString())
        .instances
}.getOrDefault(emptyList())
