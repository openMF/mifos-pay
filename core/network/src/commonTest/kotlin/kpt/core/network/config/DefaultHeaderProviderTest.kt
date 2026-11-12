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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.network.DefaultHeaderProvider
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The header seam reaches the WIRE.
 *
 * `setupDefaultHttpClient` accepted `defaultHeaders` long before anything supplied them: every REST
 * client is built by `ktorfitFor`, which passed only the base URL and hosts. So the parameter existed,
 * looked wired, and sent nothing. A test that only asserted `headersFor(...)` returns the right map
 * would have passed throughout that period — which is why these assert the request the engine saw.
 */
class DefaultHeaderProviderTest {

    private fun clientSending(headers: Map<String, String>, capture: MutableList<Pair<String, String?>>) =
        HttpClient(
            MockEngine { request ->
                capture += "X-Api-Key" to request.headers["X-Api-Key"]
                capture += "X-Client-Version" to request.headers["X-Client-Version"]
                respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) { setupDefaultHttpClient(baseUrl = "https://example.test", defaultHeaders = headers)() }

    @Test
    fun declared_headers_are_sent_on_every_request() = runTest {
        val seen = mutableListOf<Pair<String, String?>>()
        val client = clientSending(mapOf("X-Api-Key" to "abc123", "X-Client-Version" to "9.9.9"), seen)

        client.get("/one")
        client.get("/two")

        // BOTH calls: the map is applied per-request by `defaultRequest`, not once on the first.
        assertEquals(4, seen.size, "expected two headers captured on each of two requests")
        assertTrue(seen.filter { it.first == "X-Api-Key" }.all { it.second == "abc123" })
        assertTrue(seen.filter { it.first == "X-Client-Version" }.all { it.second == "9.9.9" })
    }

    @Test
    fun no_provider_sends_no_extra_headers() = runTest {
        val seen = mutableListOf<Pair<String, String?>>()
        clientSending(DefaultHeaderProvider.None.headersFor("main"), seen).get("/x")

        // The neutral default must be genuinely absent, not an empty string a server would echo back.
        assertTrue(seen.all { it.second == null }, "the None provider must add nothing: $seen")
    }

    @Test
    fun headers_are_scoped_per_access_point() {
        val provider = object : DefaultHeaderProvider {
            override fun headersFor(accessPointId: String): Map<String, String> =
                if (accessPointId == "main") mapOf("X-Api-Key" to "only-main") else emptyMap()
        }

        // The whole point of keying on the id: a credential for one endpoint must not reach another.
        assertEquals("only-main", provider.headersFor("main")["X-Api-Key"])
        assertNull(provider.headersFor("coingecko")["X-Api-Key"])
    }

    @Test
    fun the_template_ships_a_neutral_seam() {
        // A fork inherits nothing it did not ask for — same contract as ForkErrorMapper.
        assertTrue(KptNetworkHeaders.headersFor("main").isEmpty())
        assertTrue(KptNetworkHeaders.headersFor("project").isEmpty())
    }
}
