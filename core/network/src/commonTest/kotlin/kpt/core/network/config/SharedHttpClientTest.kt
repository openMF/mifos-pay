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

import kpt.core.base.network.AccessPoint
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.ktorfitFor
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * ONE `HttpClient` per access point, however many APIs sit on it.
 *
 * ## Why this test exists
 *
 * `restApi(...)` registers one Koin `single` per API TYPE, and `ktorfitFor` used to build a fresh
 * client on every call. For a server exposing many resource APIs that multiplies into one client —
 * one connection pool, one TLS session cache — per API, with no reuse between them.
 *
 * Two forks hit it independently, which is why the fix belongs here rather than in either of them:
 *  - `mifos-x-field-officer-app` migrated onto `@ApiBinding` and ended up with 22 interfaces on one
 *    Fineract host and 23 HttpClients. Measured on device 2026-10-20: an authenticated read never
 *    settled — minutes of "Refreshing…" with no error and no retry.
 *  - `mifos-pay` sidestepped it by hand in its legacy stack (3 role-keyed clients, ~24 services
 *    each) and never fixed this factory, so the defect is latent on its template path.
 *
 * ## What it pins
 *
 * The sharing is INVISIBLE at every other layer: the app compiles, every gate passes, and each API
 * works in isolation. Only latency betrays it, and latency is exactly what a test suite does not
 * measure. So the invariant is asserted on object identity instead.
 *
 * Lives in `core/network` because `core-base/network` ships no `commonTest` source set; it tests
 * `core-base` behaviour through the public DSL.
 */
class SharedHttpClientTest {

    private fun point(id: String, host: String) = AccessPoint(
        id = id,
        kind = AccessPointKind.REST,
        baseUrl = "https://$host/",
        loggableHost = host,
    )

    private fun registry() = AccessPointRegistry(
        listOf(point("primary", "api.example"), point("other", "other.example")),
    )

    @Test
    fun every_api_on_one_access_point_shares_one_client() {
        val registry = registry()

        // Two independent resolutions — exactly what two `restApi("primary") { … }` registrations
        // do for two different API interfaces.
        val first = registry.ktorfitFor("primary")
        val second = registry.ktorfitFor("primary")

        // The Ktorfit WRAPPERS may differ; they are cheap and hold no connections. The CLIENT
        // must not.
        assertSame(
            first.httpClient,
            second.httpClient,
            "Each API on an access point built its own HttpClient — N connection pools against one " +
                "host. Invisible to every gate; only latency shows it.",
        )
    }

    @Test
    fun distinct_access_points_keep_distinct_clients() {
        val registry = registry()

        // The cache must key on the access point, not collapse to one client for the whole app:
        // a credential belongs to ONE endpoint, and `defaultHeaders` are per access point. Sharing
        // across hosts would leak it to servers that never needed it.
        assertNotSame(
            registry.ktorfitFor("primary").httpClient,
            registry.ktorfitFor("other").httpClient,
            "Two hosts shared one client — a per-endpoint credential would reach the wrong server.",
        )
    }

    @Test
    fun the_shared_client_survives_across_registry_lookups() {
        val registry = registry()
        val clients = List(5) { registry.ktorfitFor("primary").httpClient }

        // Five resolutions, one client — the property that matters once many APIs resolve.
        clients.forEach { assertSame(clients.first(), it) }
    }
}
