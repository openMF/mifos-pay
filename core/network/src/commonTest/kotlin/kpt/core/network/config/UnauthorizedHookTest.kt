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
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.network.UnauthorizedHandler
import kpt.core.base.network.notifyUnauthorized
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The 401 hook (core-base `notifyUnauthorized`) on the client the app actually builds — default
 * client setup, `expectSuccess` on — so the hook is proven to fire even though the 401 is thrown.
 */
class UnauthorizedHookTest {

    private fun client(status: HttpStatusCode, seen: MutableList<String>) = HttpClient(
        MockEngine {
            respond(
                """{"httpStatusCode":"${status.value}"}""",
                status,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        },
    ) {
        setupDefaultHttpClient(baseUrl = "https://example.test/")()
        notifyUnauthorized("main", UnauthorizedHandler { seen += it })
    }

    private suspend fun HttpClient.call(withCredentials: Boolean) = runCatching {
        get("offices") { if (withCredentials) header(HttpHeaders.Authorization, "Basic dG9rZW4=") }
    }

    @Test
    fun aRejectedSessionIsReportedAndTheErrorStillPropagates() = runTest {
        val seen = mutableListOf<String>()
        val result = client(HttpStatusCode.Unauthorized, seen).call(withCredentials = true)
        assertEquals(listOf("main"), seen)
        assertEquals(true, result.isFailure, "the hook observes; the caller still gets its 401")
    }

    @Test
    fun aWrongPasswordAtSignInIsNotARejectedSession() = runTest {
        // The sign-in call sends credentials in its BODY, with no Authorization header.
        val seen = mutableListOf<String>()
        client(HttpStatusCode.Unauthorized, seen).call(withCredentials = false)
        assertEquals(emptyList(), seen)
    }

    @Test
    fun aForbiddenCallIsNotARejectedSession() = runTest {
        // 403: signed in, not permitted. Signing out would not help.
        val seen = mutableListOf<String>()
        client(HttpStatusCode.Forbidden, seen).call(withCredentials = true)
        assertEquals(emptyList(), seen)
    }
}
