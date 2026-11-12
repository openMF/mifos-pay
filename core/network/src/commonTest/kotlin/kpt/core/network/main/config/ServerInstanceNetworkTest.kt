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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.network.config.AppAccessPoints
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerInstanceNetworkTest {

    private val bank1 = ServerInstance(
        endpoint = "mifos-bank-1.mifos.community",
        protocol = "https://",
        path = "/fineract-provider/api/v1/",
        platformTenantId = "mifos-bank-1",
        label = "Bank 1",
    )
    private val flexcore = InterbankServer(
        endpoint = "apis.flexcore.mx",
        protocol = "https://",
        path = "/v1.0/vnext2/",
        label = "IB",
    )

    private fun configs() = NetworkConfigRepository(AccessPointRegistry(AppAccessPoints.points, AppAccessPoints.policy))

    @Test
    fun pickingAServerRepointsAllThreeFineractAccessPoints() {
        val configs = configs()
        applyServerInstance(configs, bank1, flexcore)
        val point = { id: String -> AppAccessPoints.points.single { it.id == id } }

        assertEquals("https://mifos-bank-1.mifos.community/fineract-provider/api/v1/", configs.urlFor(point("main")))
        assertEquals(
            "https://mifos-bank-1.mifos.community/fineract-provider/api/v1/self/",
            configs.urlFor(point("selfservice")),
        )
        assertEquals("https://apis.flexcore.mx/v1.0/vnext2/", configs.urlFor(point("interbank")))
        listOf("main", "selfservice", "interbank").forEach { id ->
            val headers = configs.headersFor(point(id), emptyMap())
            assertEquals("mifos-bank-1", headers["Fineract-Platform-TenantId"], "tenant on $id")
        }
        assertTrue(configs.headersFor(point("main"), emptyMap())["Authorization"].orEmpty().startsWith("Basic "))
        // The self-service point's Authorization is the signed-in user's, supplied by the auth bridge.
        assertEquals(null, configs.headersFor(point("selfservice"), emptyMap())["Authorization"])
    }

    @Test
    fun aRemoteRowKeepsItsInterbankServers() {
        val row = RemoteServerInstanceDto(
            label = "Bank 2",
            protocol = "https://",
            endpoint = "mifos-bank-2.mifos.community",
            path = "/fineract-provider/api/v1/",
            platformTenantId = "mifos-bank-2",
            isDefault = true,
            supportedApps = listOf(SERVER_INSTANCES_APP_ID),
            interbankServers = Json.parseToJsonElement(
                """[{"endpoint":"apis.flexcore.mx","protocol":"https://","path":"/v1.0/vnext2/",""" +
                    """"label":"IB","isDefault":true}]""",
            ) as JsonArray,
        )
        val instance = row.toServerInstance()
        assertEquals("mifos-bank-2", instance.platformTenantId)
        assertTrue(instance.isDefault)
        assertEquals(listOf(flexcore.copy(isDefault = true)), instance.interbankServers)
    }
}
