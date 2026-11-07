/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A network failure, named so the store's class-name categorizer reads it as one. */
private class FakeIOException(message: String) : Exception(message)

/** The web CORS relay: which hosts it takes, the URL it produces, and that it sees the FINAL host. */
class WebCorsRelayTest {

    /** Runs one GET [url] on a client configured by [setup] and returns the URL the engine received. */
    private suspend fun sentUrl(url: String, setup: HttpClientConfig<*>.() -> Unit): String {
        var seen = ""
        val client = HttpClient(
            MockEngine { request ->
                seen = request.url.toString()
                respondOk()
            },
        ) { setup() }
        client.get(url)
        return seen
    }

    private fun fixedUrl(base: String) = object : DynamicUrlConfigProvider {
        override fun getBaseUrl(): String = base
        override fun getLoggableHosts(): List<String> = emptyList()
    }

    @Test
    fun exactHostMatchesCaseInsensitively() {
        assertTrue(corsRelayHostMatches("api.example.com", "api.example.com"))
        assertTrue(corsRelayHostMatches("API.Example.com", "api.example.COM"))
        assertFalse(corsRelayHostMatches("evil-api.example.com", "api.example.com"))
        assertFalse(corsRelayHostMatches("api.example.com.evil.io", "api.example.com"))
    }

    @Test
    fun wildcardMatchesSubdomainsOnly() {
        assertTrue(corsRelayHostMatches("bank.mifos.community", "*.mifos.community"))
        assertTrue(corsRelayHostMatches("a.b.mifos.community", "*.mifos.community"))
        assertFalse(corsRelayHostMatches("mifos.community", "*.mifos.community"))
        assertFalse(corsRelayHostMatches("evilmifos.community", "*.mifos.community"))
    }

    @Test
    fun pathRelayResolvesAgainstThePageOrigin() {
        assertEquals("https://app.pages.dev/cors", resolveCorsRelayBase("/cors", "https://app.pages.dev"))
        val external = "https://relay.example.com/x"
        assertEquals(external, resolveCorsRelayBase(external, "https://app.pages.dev"))
    }

    @Test
    fun allowlistedRequestIsRewrittenThroughAPathRelay() = runTest {
        val sent = sentUrl("https://bank.mifos.community/fineract-provider/api/v1/clients?limit=5&offset=0") {
            installCorsRelay(listOf("*.mifos.community"), resolveCorsRelayBase("/cors", "http://localhost:8788"))
        }
        val target = "https://bank.mifos.community/fineract-provider/api/v1/clients?limit=5&offset=0"
        assertEquals("http://localhost:8788/cors/?$target", sent)
    }

    @Test
    fun allowlistedRequestIsRewrittenThroughAnHttpsRelay() = runTest {
        val sent = sentUrl("https://api.stlouisfed.org/fred/series?series_id=X") {
            installCorsRelay(listOf("api.stlouisfed.org"), "https://relay.example.com/cors")
        }
        assertEquals("https://relay.example.com/cors/?https://api.stlouisfed.org/fred/series?series_id=X", sent)
    }

    @Test
    fun aHostOffTheAllowlistGoesDirect() = runTest {
        val sent = sentUrl("https://api.worldbank.org/v2/country") {
            installCorsRelay(listOf("api.stlouisfed.org"), "https://relay.example.com/cors")
        }
        assertEquals("https://api.worldbank.org/v2/country", sent)
    }

    @Test
    fun directModeRelaysNothing() {
        val ap = AccessPoint(
            id = "p",
            kind = AccessPointKind.REST,
            baseUrl = "https://a.test/",
            loggableHost = "a.test",
            proxiedHost = "a.test",
        )
        val direct = NetworkPolicy(
            webCorsProxy = "/cors",
            webCorsMode = WebCorsMode.DIRECT,
            webCorsAllowlist = listOf("a.test"),
        )
        assertNull(direct.activeWebCorsRelay)
        assertEquals(emptyList(), direct.webCorsHostsFor(ap))
        val relay = direct.copy(webCorsMode = WebCorsMode.RELAY, webCorsAllowlist = listOf("*.b.test"))
        assertEquals("/cors", relay.activeWebCorsRelay)
        assertEquals(listOf("*.b.test", "a.test"), relay.webCorsHostsFor(ap))
    }

    @Test
    fun relaySeesTheHostAfterARuntimeBaseUrlChange() = runTest {
        // Installed BEFORE the dynamic plugin on purpose: the order must come from the pipeline phase,
        // not from install order. The static host is off the allowlist; the runtime one is on it.
        val sent = sentUrl("clients") {
            // setupDefaultHttpClient's order: defaultRequest, then (here, deliberately early) the relay.
            defaultRequest { url("https://static.example.org/fineract-provider/api/v1/") }
            installCorsRelay(listOf("*.mifos.community"), "https://relay.example.com/cors")
            install(DynamicBaseUrlPlugin) {
                configProvider = fixedUrl("https://bank.mifos.community/fineract-provider/api/v1/")
                staticBaseUrl = "https://static.example.org/fineract-provider/api/v1/"
            }
        }
        val target = "https://bank.mifos.community/fineract-provider/api/v1/clients"
        assertEquals("https://relay.example.com/cors/?$target", sent)
    }

    @Test
    fun aRuntimeSwitchOffTheAllowlistGoesDirect() = runTest {
        val sent = sentUrl("clients") {
            defaultRequest { url("https://static.example.org/") }
            installCorsRelay(listOf("static.example.org"), "https://relay.example.com/cors")
            install(DynamicBaseUrlPlugin) {
                configProvider = fixedUrl("https://other.example.net/")
                staticBaseUrl = "https://static.example.org/"
            }
        }
        assertEquals("https://other.example.net/clients", sent)
    }

    /** One exchange the engine saw: the URL and the body it carried. */
    private data class Sent(val url: String, val body: String)

    /**
     * A client with the same-origin fallback on, whose engine answers the relay with [relayAnswer] and
     * every direct call with 200. Each test uses its own [relayBase], because the absent-relay memo is
     * session-wide.
     */
    private fun fallbackClient(
        relayBase: String,
        sent: MutableList<Sent>,
        expectSuccess: Boolean = false,
        relayAnswer: suspend MockRequestHandleScope.() -> HttpResponseData,
    ) = HttpClient(
        MockEngine { request ->
            sent += Sent(request.url.toString(), request.body.toByteArray().decodeToString())
            if (request.url.toString().startsWith(relayBase)) relayAnswer() else respondOk("direct")
        },
    ) {
        this.expectSuccess = expectSuccess
        installCorsRelay(listOf("api.bank.test"), relayBase, fallbackToDirect = true)
    }

    @Test
    fun aMissingRelayIsReplayedDirectlyWithTheSameBodyAndRemembered() = runTest {
        val relay = "https://site-a.test/cors"
        val sent = mutableListOf<Sent>()
        val client = fallbackClient(relay, sent) {
            respond("<html>404</html>", HttpStatusCode.NotFound) // a static host: no worker, no tag
        }
        val first: HttpResponse = client.post("https://api.bank.test/auth") { setBody("""{"u":"x"}""") }
        assertEquals(HttpStatusCode.OK, first.status)
        assertEquals(
            listOf(
                Sent("$relay/?https://api.bank.test/auth", """{"u":"x"}"""),
                Sent("https://api.bank.test/auth", """{"u":"x"}"""),
            ),
            sent,
        )
        sent.clear()
        client.get("https://api.bank.test/clients")
        val urls = sent.map { it.url }
        assertEquals(listOf("https://api.bank.test/clients"), urls, "absent relay must not be tried again")
    }

    @Test
    fun aMissingRelayIsDetectedUnderExpectSuccessToo() = runTest {
        val sent = mutableListOf<Sent>()
        val client = fallbackClient("https://site-b.test/cors", sent, expectSuccess = true) {
            respond("Method Not Allowed", HttpStatusCode.MethodNotAllowed)
        }
        assertEquals(HttpStatusCode.OK, client.post("https://api.bank.test/auth") { setBody("b") }.status)
        assertEquals(2, sent.size)
    }

    @Test
    fun theWorkersOwnRefusalIsFinal() = runTest {
        val sent = mutableListOf<Sent>()
        val client = fallbackClient("https://site-c.test/cors", sent) {
            respond("Host not allowed", HttpStatusCode.Forbidden, headersOf(CORS_RELAY_HEADER, "1"))
        }
        assertEquals(HttpStatusCode.Forbidden, client.get("https://api.bank.test/x").status)
        assertEquals(1, sent.size, "a tagged 403 came from the worker — never retried directly")
        client.get("https://api.bank.test/y")
        assertEquals(2, sent.size)
        assertTrue(sent.all { it.url.startsWith("https://site-c.test/cors") }, "relay still in use: $sent")
    }

    @Test
    fun anUpstreamErrorThroughTheRelayIsFinalEvenUnderExpectSuccess() = runTest {
        val sent = mutableListOf<Sent>()
        val client = fallbackClient("https://site-d.test/cors", sent, expectSuccess = true) {
            respond("boom", HttpStatusCode.InternalServerError, headersOf(CORS_RELAY_HEADER, "1"))
        }
        assertFailsWith<ServerResponseException> { client.get("https://api.bank.test/x") }
        assertEquals(1, sent.size)
    }

    @Test
    fun aNetworkFailureRetriesAnIdempotentCallDirectly() = runTest {
        val sent = mutableListOf<Sent>()
        val client = fallbackClient("https://site-e.test/cors", sent) {
            throw FakeIOException("connection refused")
        }
        assertEquals(HttpStatusCode.OK, client.get("https://api.bank.test/x").status)
        val urls = sent.map { it.url }
        assertEquals(listOf("https://site-e.test/cors/?https://api.bank.test/x", "https://api.bank.test/x"), urls)
        sent.clear()
        client.get("https://api.bank.test/y")
        val after = sent.map { it.url }
        assertEquals(listOf("https://api.bank.test/y"), after, "relay unreachable but direct worked: remembered")
    }

    @Test
    fun aNetworkFailureNeverRepeatsAPost() = runTest {
        val sent = mutableListOf<Sent>()
        val client = fallbackClient("https://site-f.test/cors", sent) {
            throw FakeIOException("reset after send")
        }
        assertFailsWith<FakeIOException> { client.post("https://api.bank.test/pay") { setBody("1") } }
        assertEquals(1, sent.size, "the relay may already have forwarded it")
    }

    @Test
    fun withoutFallbackAMissingRelayIsNotSecondGuessed() = runTest {
        val sent = mutableListOf<Sent>()
        val client = HttpClient(
            MockEngine { request ->
                sent += Sent(request.url.toString(), "")
                respond("no tag", HttpStatusCode.NotFound)
            },
        ) { installCorsRelay(listOf("api.bank.test"), "https://external-relay.test") }
        assertEquals(HttpStatusCode.NotFound, client.get("https://api.bank.test/x").status)
        assertEquals(1, sent.size)
    }
}
