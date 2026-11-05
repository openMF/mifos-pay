/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.network.AccessPoint
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.HeaderSpec
import kpt.core.base.network.NetworkConfig
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.base.network.restClientConfig
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A runtime network-config change reaching requests — on the client configuration `ktorfitFor`
 * actually builds ([restClientConfig]), with the client created BEFORE the change, as in the app.
 */
class NetworkConfigRequestTest {

    private val main = AccessPoint(
        id = "main",
        kind = AccessPointKind.REST,
        baseUrl = "https://api.example.org/",
        basePath = "v1/",
        loggableHost = "api.example.org",
        isMain = true,
        headers = listOf(HeaderSpec(name = "X-Tenant-Id", value = "tenant-a")),
    )

    @Test
    fun editsMainSwitchesAndResetsReachAnExistingClient() = runTest {
        val configs = NetworkConfigRepository(AccessPointRegistry(listOf(main)))
        val seen = mutableListOf<Pair<String, String?>>()
        val client = HttpClient(
            MockEngine { r ->
                seen += r.url.toString() to r.headers["X-Tenant-Id"]
                respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
            },
            restClientConfig(main, networkConfigs = configs),
        )

        client.get("items")
        assertEquals("https://api.example.org/v1/items" to "tenant-a", seen.last(), "the declared config")

        configs.update(
            "main",
            baseUrl = "https://other.example.org:8443/api/",
            headers = mapOf("X-Tenant-Id" to "tenant-b"),
        )
        client.get("items")
        assertEquals("https://other.example.org:8443/api/items" to "tenant-b", seen.last(), "an edit")

        configs.add(
            NetworkConfig(
                id = "local",
                kind = AccessPointKind.REST,
                baseUrl = "http://10.0.2.2:8080/",
                headers = mapOf("X-Tenant-Id" to "dev"),
            ),
        )
        configs.setMain("local")
        client.get("items")
        assertEquals("http://10.0.2.2:8080/items" to "dev", seen.last(), "a runtime-added config made main")

        configs.resetAll()
        client.get("items")
        assertEquals("https://api.example.org/v1/items" to "tenant-a", seen.last(), "reset returns to app-profile")
    }
}
