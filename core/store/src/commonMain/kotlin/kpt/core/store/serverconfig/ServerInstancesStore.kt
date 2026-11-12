/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.serverconfig

import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.serverconfig.dao.ServerInstanceDao
import kpt.core.database.serverconfig.entity.ServerInstanceEntity
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.ServerInstanceApi
import kpt.core.network.main.config.SERVER_INSTANCES_APP_ID
import kpt.core.network.main.config.bundledServerInstances
import kpt.core.network.main.config.toServerInstance
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

private val interbankJson = Json { ignoreUnknownKeys = true }
private val interbankList = ListSerializer(InterbankServer.serializer())

/**
 * The server picker's instance list — network -> database -> store.
 *
 * Fetcher: Supabase `app_server_config` through the template's `ServerInstanceApi`, only this app's rows
 * (the server filters on `supported_apps`). Source of truth: the `server_instances` table, so the list
 * survives offline and a relaunch.
 *
 * With NO Supabase key the app has no remote list by design, so the fetcher answers with the list shipped
 * in the app; the same when the table holds no row for this app. A transport failure is NOT masked — it is
 * a fetch error, so the screen shows the cached list (or the error and a retry).
 */
// logout-exempt: public configuration, and the login screen needs it right after a sign-out
@StoreProvider(id = "serverInstances", ttl = "12h", logout = false)
@CacheKey(name = "KEY", key = "serverInstances")
fun provideServerInstancesStore(api: ServerInstanceApi, dao: ServerInstanceDao): Store<Unit, List<ServerInstance>> =
    StoreFactory.createStore(
        fetcher = Fetcher.of { _: Unit ->
            if (!api.isConfigured) return@of bundledServerInstances()
            api.fetchInstances(SERVER_INSTANCES_APP_ID).map { it.toServerInstance() }
                .ifEmpty { bundledServerInstances() }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit ->
                // Empty = never fetched: null makes Store5 fetch instead of serving "no servers".
                dao.observeAll().map { rows -> rows.takeIf { it.isNotEmpty() }?.map { it.toModel() } }
            },
            writer = { _: Unit, instances: List<ServerInstance> ->
                val now = Clock.System.now().toEpochMilliseconds()
                dao.replaceAll(instances.mapIndexed { position, instance -> instance.toEntity(position, now) })
            },
        ),
    )

private fun ServerInstanceEntity.toModel() = ServerInstance(
    endpoint = endpoint,
    protocol = protocol,
    path = path,
    platformTenantId = platformTenantId,
    label = label,
    isDefault = isDefault,
    interbankServers = interbankJson.decodeFromString(interbankList, interbankServers),
)

private fun ServerInstance.toEntity(position: Int, fetchedAtMs: Long) = ServerInstanceEntity(
    id = "$endpoint/$platformTenantId",
    label = label,
    protocol = protocol,
    endpoint = endpoint,
    path = path,
    platformTenantId = platformTenantId,
    isDefault = isDefault,
    interbankServers = interbankJson.encodeToString(interbankList, interbankServers),
    position = position,
    fetchedAtMs = fetchedAtMs,
)
