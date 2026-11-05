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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The shared client names JSON for a typed body, so no access point has to declare it.
 *
 * What it locks, found on device 2026-10-01: `ContentNegotiation` serializes a body only when the
 * request already names a Content-Type it has a converter for, and a Ktorfit `@Body` parameter names
 * none. With nothing declared, Ktor threw before opening a socket ("Fail to prepare request body for
 * sending … with Content-Type: null"), which sign-in rendered as a wrong password.
 *
 * `JsonBodyContentType` (core-base) fills that in, and ONLY that: a GET stays bodyless and untyped, a
 * text body keeps text/plain, and a server that declares its own Content-Type keeps it.
 */
class JsonBodyContentTypeTest {

    @Serializable
    private data class Credentials(val username: String, val password: String)

    private class Seen {
        var contentType: String? = null
        var accept: String? = null
    }

    private fun client(seen: Seen, declared: Map<String, String> = emptyMap()) = HttpClient(
        MockEngine { request ->
            seen.contentType = request.body.contentType?.toString() ?: request.headers[HttpHeaders.ContentType]
            seen.accept = request.headers[HttpHeaders.Accept]
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        },
    ) {
        setupDefaultHttpClient(baseUrl = "https://server.test/", dynamicHeaders = { declared })()
    }

    @Test
    fun aTypedBodyIsSentAsJsonWithNothingDeclared() = runTest {
        val seen = Seen()
        // The call itself is half the assertion: without the default this throws inside Ktor.
        client(seen).post("authentication") { setBody(Credentials("fieldofficer", "unused")) }

        assertTrue(seen.contentType?.startsWith("application/json") == true, "got ${seen.contentType}")
        assertTrue(seen.accept?.contains("application/json") == true, "got Accept ${seen.accept}")
    }

    @Test
    fun aBodylessGetDeclaresNoContentType() = runTest {
        val seen = Seen()
        client(seen).get("clients")

        assertNull(seen.contentType)
    }

    @Test
    fun aTextBodyKeepsItsOwnType() = runTest {
        val seen = Seen()
        client(seen).post("note") { setBody("plain words") }

        assertTrue(seen.contentType?.startsWith("text/plain") == true, "got ${seen.contentType}")
    }

    @Test
    fun aServerThatDeclaresItsOwnTypeKeepsIt() = runTest {
        val seen = Seen()
        val custom = "application/vnd.api+json"
        // A declared type is honoured: the default steps aside instead of overwriting it. (Serializing a
        // typed body under it would also need that converter registered — see JsonBodyContentType.)
        client(seen, declared = mapOf(HttpHeaders.ContentType to custom)).get("clients")

        assertEquals(custom, seen.contentType)
    }
}
