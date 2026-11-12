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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kotlin.io.encoding.Base64

/**
 * Points the three Fineract access points at the server the user picked.
 *
 * mifos-pay's servers are loaded at runtime (Supabase-hosted instances), each with its own host, path and
 * tenant. The template's [NetworkConfigRepository] is what every REST access point reads its URL and
 * header values from on each request, so writing the selected instance there is the whole switch — no
 * custom Ktor plugin. `main` (staff API) and `selfservice` (`…/self/`) follow the selected instance,
 * `interbank` the selected inter-bank server; all three carry the instance's tenant.
 *
 * [start] applies the current selection synchronously, before any screen can fetch, then follows changes.
 */
class InstanceNetworkConfigBridge(
    private val instances: InstanceConfigManager,
    private val networkConfigs: NetworkConfigRepository,
    private val scope: CoroutineScope,
) {
    /** Applies the current selection now, then re-applies it on every change. */
    fun start() {
        apply(instances.getCurrentInstance(), instances.getCurrentInterbankInstance())
        scope.launch {
            combine(instances.selectedInstance, instances.selectedInterbankInstance) { _, _ ->
                instances.getCurrentInstance() to instances.getCurrentInterbankInstance()
            }.collect { (instance, interbank) -> apply(instance, interbank) }
        }
    }

    private fun apply(instance: ServerInstance, interbank: InterbankServer) =
        applyServerInstance(networkConfigs, instance, interbank)
}

/**
 * Writes [instance] (and its chosen [interbank] server) into [networkConfigs]: the staff API and the
 * self-service API (`…/self/`) on the instance's host and path, the inter-bank point on its server, all
 * three carrying the instance's tenant, and the staff API its credentials.
 */
internal fun applyServerInstance(
    networkConfigs: NetworkConfigRepository,
    instance: ServerInstance,
    interbank: InterbankServer,
) {
    val tenant = mapOf(TENANT_HEADER to instance.platformTenantId)
    networkConfigs.update(MAIN, baseUrl = instance.fullUrl, headers = tenant + (AUTHORIZATION to STAFF_AUTH))
    networkConfigs.update(SELF_SERVICE, baseUrl = instance.fullUrl + "self/", headers = tenant)
    networkConfigs.update(INTERBANK, baseUrl = interbank.fullUrl, headers = tenant)
}

private const val MAIN = "main"
private const val SELF_SERVICE = "selfservice"
private const val INTERBANK = "interbank"
private const val TENANT_HEADER = "Fineract-Platform-TenantId"
private const val AUTHORIZATION = "Authorization"

// The staff API's credentials on the Mifos demo servers (unchanged from the previous client).
private val STAFF_AUTH = "Basic " + Base64.encode("mifos:password".encodeToByteArray())
