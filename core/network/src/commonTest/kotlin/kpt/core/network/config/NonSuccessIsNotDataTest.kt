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
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kpt.core.base.network.setupDefaultHttpClient
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * A non-2xx must reach the caller as a FAILURE, never as data.
 *
 * ## Why this test exists
 *
 * The shared client did not set `expectSuccess`, so Ktor handed the error BODY to
 * ContentNegotiation and it was deserialized onto the expected success type. Nothing threw, so
 * every layer above treated the result as real:
 *
 *  - a list read got an error OBJECT where it wanted an ARRAY — either a confusing
 *    `JsonConvertException` or, when the shapes were compatible, an EMPTY LIST;
 *  - a write parsed the error onto a DTO whose fields are all nullable-with-defaults, so
 *    `MutationGateway.command` returned `MutationResult.Applied` — a failed write reported as a
 *    success.
 *
 * Measured on device 2026-10-20 against live Fineract: a 401 on `POST /clients/{id}/notes` returned
 * `Applied`, the screen navigated back as though the note were saved, and the note did not exist on
 * the server. The paired list read rendered "No Item Found" instead of "signed out". The user is
 * told the opposite of the truth in both directions.
 *
 * ## What it pins
 *
 * That the failure is RAISED. It deliberately does not assert which `ScreenState` or
 * `MutationResult` results — those are the store's and the gateway's contracts, tested there. The
 * only thing this file owns is: the HTTP layer does not launder a 4xx/5xx into a value.
 */
class NonSuccessIsNotDataTest {

    @Serializable
    private data class Payload(val name: String? = null, val id: Long? = null)

    /** The shape Fineract actually returns on a rejected call. */
    private val errorBody =
        """{"timestamp":"2026-10-20T08:38:37.192Z","status":401,"error":"Unauthorized","path":"/clients/3/notes"}"""

    private fun clientReturning(status: HttpStatusCode, body: String) = HttpClient(
        MockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) },
    ) {
        setupDefaultHttpClient(baseUrl = "https://example.test/")()
    }

    @Test
    fun a401DoesNotDeserializeOntoTheSuccessType() = runTest {
        val client = clientReturning(HttpStatusCode.Unauthorized, errorBody)

        // THE REGRESSION. Before `expectSuccess`, this returned a Payload with every field null —
        // indistinguishable from a genuine empty response, which is how a 401 became `Applied`.
        assertFailsWith<ClientRequestException> {
            client.get("anything").body<Payload>()
        }
    }

    @Test
    fun a500DoesNotDeserializeOntoTheSuccessType() = runTest {
        val client = clientReturning(HttpStatusCode.InternalServerError, errorBody)
        assertFailsWith<ServerResponseException> {
            client.get("anything").body<Payload>()
        }
    }

    @Test
    fun anErrorObjectWhereAListIsExpectedStillFailsLoudly() = runTest {
        val client = clientReturning(HttpStatusCode.Unauthorized, errorBody)

        // The list-read shape. This one DID throw before the fix, but as a JsonConvertException
        // about JSON tokens — which reads as a parsing bug in the client rather than as "the server
        // rejected you", and sent more than one debugging session down the wrong path.
        assertFailsWith<ClientRequestException> {
            client.get("anything").body<List<Payload>>()
        }
    }

    @Test
    fun a2xxStillDeserializesNormally() = runTest {
        val client = clientReturning(HttpStatusCode.OK, """{"name":"ok","id":7}""")

        // The other half of the contract: expectSuccess must not disturb the success path.
        val body = client.get("anything").body<Payload>()
        kotlin.test.assertEquals("ok", body.name)
        kotlin.test.assertEquals(7L, body.id)
    }
}
