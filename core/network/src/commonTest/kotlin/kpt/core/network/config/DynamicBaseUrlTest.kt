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
import kpt.core.base.network.DynamicUrlConfigProvider
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runtime server switching (core-base `DynamicBaseUrlPlugin`) on the client the app builds.
 *
 * Measured 2026-10-25: with a static base of `…/1.0/field/v1/` and a dynamic one of
 * `…/fineract-provider/api/v1/`, a request for `clients` went to
 * `https://other.example/fineract-provider/api/v1/1.0/field/v1/clients` — the static base PATH was kept
 * and the dynamic one prepended. Any fork whose server has an API path sent every request to a URL that
 * does not exist the moment the officer changed server.
 */
class DynamicBaseUrlTest {

    private fun client(dynamicBase: String?, seen: MutableList<String>) = HttpClient(
        MockEngine { r ->
            seen += r.url.toString()
            respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        },
    ) {
        setupDefaultHttpClient(
            baseUrl = "https://api.example.org/1.0/field/v1/",
            dynamicUrlProvider = dynamicBase?.let { base ->
                object : DynamicUrlConfigProvider {
                    override fun getBaseUrl() = base
                    override fun getLoggableHosts() = listOf("other.example")
                }
            },
        )()
    }

    @Test
    fun aSwitchedServerReplacesTheBasePathInsteadOfStackingOnIt() = runTest {
        val seen = mutableListOf<String>()
        client("https://other.example/fineract-provider/api/v1/", seen).get("clients?x=1")
        assertEquals("https://other.example/fineract-provider/api/v1/clients?x=1", seen.single())
    }

    @Test
    fun aServerWithAPortAndNoPathIsHonoured() = runTest {
        val seen = mutableListOf<String>()
        client("http://10.0.2.2:8443/", seen).get("offices")
        assertEquals("http://10.0.2.2:8443/offices", seen.single())
    }

    @Test
    fun withoutAProviderTheStaticBaseIsUnchanged() = runTest {
        val seen = mutableListOf<String>()
        client(null, seen).get("clients")
        assertEquals("https://api.example.org/1.0/field/v1/clients", seen.single())
    }
}
