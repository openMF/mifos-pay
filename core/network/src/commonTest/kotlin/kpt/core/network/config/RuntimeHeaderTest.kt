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
import kpt.core.base.network.AccessPoint
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.HeaderSpec
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A header value set AFTER the client was built reaches the next request.
 *
 * This is the invariant the whole design exists for. The HTTP client is a Koin singleton created at
 * app start, long before anyone signs in — so a credential captured at construction is `null` for
 * the process lifetime. Resolving through [RuntimeHeaderStore] inside `defaultRequest` is what makes
 * "log in, and the next call carries it" true, and these tests fail if that ever regresses to a
 * value captured once.
 */
class RuntimeHeaderTest {

    private val specs = listOf(
        HeaderSpec(name = "Fineract-Platform-TenantId", value = "default"),
        HeaderSpec(name = HttpHeaders.Authorization, runtimeKey = "fineract.auth"),
    )

    private fun clientFor(store: RuntimeHeaderStore, seen: MutableList<Map<String, String?>>) =
        HttpClient(
            MockEngine { request ->
                seen += mapOf(
                    "tenant" to request.headers["Fineract-Platform-TenantId"],
                    "auth" to request.headers[HttpHeaders.Authorization],
                )
                respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) {
            setupDefaultHttpClient(
                baseUrl = "https://sandbox.mifos.community/fineract-provider/api/v1/",
                dynamicHeaders = { store.resolve(specs) },
            )()
        }

    @Test
    fun a_credential_set_after_the_client_exists_reaches_the_next_request() = runTest {
        val store = RuntimeHeaderStore()
        val seen = mutableListOf<Map<String, String?>>()
        val client = clientFor(store, seen) // built BEFORE login, as in production

        client.get("offices") // anonymous
        store["fineract.auth"] = "Basic abc123" // login lands here
        client.get("offices") // same client instance

        assertNull(seen[0]["auth"], "before login the header must be ABSENT, not empty")
        assertEquals("Basic abc123", seen[1]["auth"], "the value login wrote must reach the next call")
        // The static one is unaffected by any of this.
        assertTrue(seen.all { it["tenant"] == "default" })
    }

    @Test
    fun sign_out_stops_sending_the_credential() = runTest {
        val store = RuntimeHeaderStore()
        val seen = mutableListOf<Map<String, String?>>()
        val client = clientFor(store, seen)

        store["fineract.auth"] = "Basic abc123"
        client.get("offices")
        store.clear("fineract.auth")
        client.get("offices")

        // Not "empty string" — the previous user's credential must be gone, and an empty
        // Authorization is a malformed credential rather than an absent one.
        assertEquals("Basic abc123", seen[0]["auth"])
        assertNull(seen[1]["auth"], "after clear() the header must be omitted entirely")
    }

    @Test
    fun a_spec_must_be_exactly_one_of_static_or_runtime() {
        // Both, or neither, is a declaration that cannot mean anything — caught at construction
        // rather than by silently sending the wrong thing.
        assertFailsWith<IllegalArgumentException> { HeaderSpec(name = "X") }
        assertFailsWith<IllegalArgumentException> { HeaderSpec(name = "X", value = "a", runtimeKey = "b") }
    }

    @Test
    fun every_generated_rest_point_joins_to_a_slash_terminated_url() {
        // Over EVERY generated REST point, so it holds in a fork whatever its point ids are. The
        // trailing slash is load-bearing: Ktor resolves a relative request path against the effective
        // URL, so without it `offices` would REPLACE the last segment. Supabase points are excluded —
        // supabase-kt takes the project origin and builds its own paths. The demo point's declared
        // origin and path are pinned in kpt.core.network.fineract.FineractPointTest.
        AppAccessPoints.points.filter { it.kind == AccessPointKind.REST }.forEach { point ->
            assertTrue(point.effectiveUrl.endsWith("/"), "${point.id}: ${point.effectiveUrl}")
        }
    }

    @Test
    fun the_join_is_slash_safe_from_either_side() {
        fun ap(url: String, path: String) =
            AccessPoint(id = "x", kind = AccessPointKind.REST, baseUrl = url, basePath = path, loggableHost = "h")

        // Neither side has to be careful about its slashes — a double slash is a 404 on many servers.
        assertEquals("https://h/a/b/", ap("https://h/", "a/b/").effectiveUrl)
        assertEquals("https://h/a/b/", ap("https://h", "a/b").effectiveUrl)
        assertEquals("https://h/a/b/", ap("https://h/", "/a/b/").effectiveUrl)
        // No path: the origin is used as-is rather than gaining a stray slash.
        assertEquals("https://h/", ap("https://h/", "").effectiveUrl)
    }
}
