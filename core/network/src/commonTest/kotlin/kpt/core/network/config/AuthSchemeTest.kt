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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kpt.core.base.network.AccessPoint
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.AuthHeaderBridge
import kpt.core.base.network.AuthScheme
import kpt.core.base.network.AuthTokenSource
import kpt.core.base.network.RuntimeHeaderStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * `Authorization` is wired from the DECLARED scheme, with no call site formatting a credential.
 *
 * The behaviour that matters is the transition: sign in and the header appears, sign out and it is
 * gone — on a client that already existed. A test asserting only `format()` would pass even if the
 * bridge never ran.
 */
class AuthSchemeTest {

    private fun point(id: String, scheme: AuthScheme) = AccessPoint(
        id = id,
        kind = AccessPointKind.REST,
        baseUrl = "https://h/",
        loggableHost = "h",
        auth = scheme,
    )

    @Test
    fun each_scheme_formats_its_own_wire_value() {
        assertEquals("Basic abc", AuthScheme.BASIC.format("abc"))
        assertEquals("Bearer abc", AuthScheme.BEARER.format("abc"))
        // OAuth IS Bearer on the wire — the difference is where the token comes from, not the header.
        assertEquals("Bearer abc", AuthScheme.OAUTH.format("abc"))
        assertNull(AuthScheme.NONE.format("abc"), "a public endpoint is never handed a credential")
        // Blank must be OMITTED, not sent as `Basic ` — a malformed credential is worse than none.
        assertNull(AuthScheme.BASIC.format(null))
        assertNull(AuthScheme.BASIC.format("   "))
    }

    @Test
    fun sign_in_and_sign_out_move_the_header_automatically() = runTest {
        val token = MutableStateFlow<String?>(null)
        val headers = RuntimeHeaderStore()
        val key = AuthScheme.runtimeKeyFor("fineract")

        AuthHeaderBridge(
            points = listOf(point("fineract", AuthScheme.BASIC)),
            tokenSource = AuthTokenSource { token },
            headers = headers,
        ).start(TestScope(testScheduler))
        testScheduler.advanceUntilIdle()

        assertNull(headers[key], "signed out: no credential")

        token.value = "dXNlcjpwYXNz"
        testScheduler.advanceUntilIdle()
        assertEquals("Basic dXNlcjpwYXNz", headers[key], "sign-in must format and store it")

        token.value = null
        testScheduler.advanceUntilIdle()
        assertNull(headers[key], "sign-out must CLEAR it, not leave the previous user's credential")
    }

    @Test
    fun a_point_without_auth_is_never_bridged() = runTest {
        val headers = RuntimeHeaderStore()
        AuthHeaderBridge(
            points = listOf(point("coingecko", AuthScheme.NONE)),
            tokenSource = AuthTokenSource { MutableStateFlow("leaked-token") },
            headers = headers,
        ).start(TestScope(testScheduler))
        testScheduler.advanceUntilIdle()

        // The credential must not reach a public endpoint just because one exists.
        assertNull(headers[AuthScheme.runtimeKeyFor("coingecko")])
    }

    @Test
    fun every_auth_bearing_point_gets_a_derived_runtime_authorization_header() {
        // Over EVERY generated point, so it holds in a fork whatever its point ids are. The demo
        // point's declared values are pinned in kpt.core.network.fineract.FineractPointTest.
        AppAccessPoints.points.filter { it.auth != AuthScheme.NONE }.forEach { point ->
            // The Authorization spec is DERIVED from `auth:` — nobody wrote that row by hand.
            val auth = point.headers.first { it.name == "Authorization" }
            assertEquals(AuthScheme.runtimeKeyFor(point.id), auth.runtimeKey, point.id)
            assertNull(auth.value, "${point.id}: no credential may be baked into the binary")
        }
    }

    @Test
    fun parsing_is_forgiving_but_defaults_to_none() {
        assertEquals(AuthScheme.BASIC, AuthScheme.from("basic"))
        assertEquals(AuthScheme.OAUTH, AuthScheme.from("OAuth"))
        // An unknown or absent value must NOT silently become a credential-bearing scheme.
        assertEquals(AuthScheme.NONE, AuthScheme.from("mtls"))
        assertEquals(AuthScheme.NONE, AuthScheme.from(null))
    }
}
