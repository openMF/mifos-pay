/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class ErrorCategoryTest {

    @Test
    fun categorize_genericRuntimeException_returnsGeneric() {
        assertEquals(
            ErrorCategory.Generic,
            categorize(RuntimeException("oops")),
        )
    }

    @Test
    fun categorize_ioExceptionByName_returnsNetwork() {
        // Use a custom Throwable whose simpleName contains 'IOException' — works on KMP without
        // depending on java.io.IOException (which isn't on iOS/Web targets).
        class IOException(message: String) : RuntimeException(message)
        assertEquals(
            ErrorCategory.Network,
            categorize(IOException("disconnect")),
        )
    }

    // ─── REAL engine exception names ────────────────────────────────────────
    // The cases above use invented names that obviously contain the matched substrings. These use
    // the names the transport layers ACTUALLY throw, which is the only thing that decides whether a
    // real offline device gets the offline-first Empty or a blocking NoNetwork.

    @Test
    fun categorize_ktorDarwinTransportFailure_returnsNetwork() {
        // Ktor's Darwin engine (iOS/macOS — `ktor-client-darwin`, wired in libs.versions.toml) raises
        // `DarwinHttpRequestException` for a TRANSPORT failure, wrapping the NSError. Verified
        // against the cached klib for ktor-client-darwin 3.2.1, which declares exactly
        // `DarwinHttpRequestException` and the deprecated `IosHttpRequestException`.
        //
        // The NSError is a PROPERTY of that exception, not its `cause`, so classNameChain() stops at
        // the Darwin name and never sees "offline". None of IOException / Connect / Timeout /
        // UnknownHost / SSLException / Offline appears in it — so before this case it categorized
        // Generic, `isExpectedWhenOffline()` rejected it, and DecisionEngine fell through to a
        // blocking NoNetwork. That is the exact user-facing bug the offline-first rule exists to
        // prevent, reachable on every iOS cold start with an empty cache.
        class DarwinHttpRequestException : RuntimeException()
        assertEquals(
            ErrorCategory.Network,
            categorize(DarwinHttpRequestException()),
        )
    }

    @Test
    fun categorize_ktorDarwinLegacyName_returnsNetwork() {
        class IosHttpRequestException : RuntimeException()
        assertEquals(
            ErrorCategory.Network,
            categorize(IosHttpRequestException()),
        )
    }

    @Test
    fun categorize_socketException_returnsNetwork() {
        // java.net.SocketException IS an IOException subclass, but categorize() matches on the NAME,
        // not the type — and "SocketException" contains none of the six patterns. A dropped
        // connection on JVM/Android therefore categorized Generic while its sibling
        // SocketTimeoutException matched (via "Timeout"), which is an inconsistency no caller could
        // have predicted.
        class SocketException : RuntimeException()
        assertEquals(
            ErrorCategory.Network,
            categorize(SocketException()),
        )
    }

    @Test
    fun categorize_tlsHandshakeFailure_returnsNetwork() {
        // javax.net.ssl.SSLHandshakeException does not contain "SSLException"; its cause, an
        // EOFException, contains no transport pattern either. Measured on device 2026-10-25.
        class EOFException : RuntimeException()
        class SSLHandshakeException(cause: Throwable) : RuntimeException("connection closed", cause)
        assertEquals(ErrorCategory.Network, categorize(SSLHandshakeException(EOFException())))
        assertEquals(ErrorCategory.Network, categorize(EOFException()))
    }

    @Test
    fun categorize_ktorJsEngineFailure_returnsNetwork() {
        // Ktor's JS and wasmJs engines wrap EVERY JavaScript-side failure in
        //   class JsError(origin: dynamic) : Throwable("Error from javascript[...]")
        // including a rejected fetch and a rejected ReadableStreamDefaultReader.read(). Verified
        // against ktor main: ktor-client-core/js/.../JsClientEngine.kt and
        // ktor-client-core/wasmJs/.../browser/BrowserFetch.kt. (The engine moved out of
        // ktor-client-js into ktor-client-core, which is why looking for a cached ktor-client-js
        // artifact found nothing.)
        //
        // The name carries NO transport word — not IOException, not Connect, not Socket — so before
        // this case web was the last platform still falling through to a blocking NoNetwork on an
        // empty offline cache, the same bug the Darwin case fixed for iOS. The browser's own cause
        // ("TypeError: Failed to fetch") hangs off `origin`, not `cause`, so the chain never sees it.
        class JsError : RuntimeException()
        assertEquals(
            ErrorCategory.Network,
            categorize(JsError()),
        )
    }

    @Test
    fun categorize_serverResponseException_staysNonNetwork() {
        // Guard on the widening: Ktor reports HTTP STATUS failures as ResponseException subclasses,
        // which must NOT become Network — a 500 is real signal from a reachable server, and calling
        // it Network would let `isExpectedWhenOffline()` swallow it on an offline screen.
        class ServerResponseException : RuntimeException()
        assertNotEquals(
            ErrorCategory.Network,
            categorize(ServerResponseException()),
        )
    }

    @Test
    fun categorize_connectExceptionByName_returnsNetwork() {
        class ConnectTimeoutException : RuntimeException()
        assertEquals(
            ErrorCategory.Network,
            categorize(ConnectTimeoutException()),
        )
    }

    @Test
    fun categorize_timeoutByName_returnsNetwork() {
        class SocketTimeoutException : RuntimeException()
        assertEquals(
            ErrorCategory.Network,
            categorize(SocketTimeoutException()),
        )
    }

    @Test
    fun categorize_authErrorMessage_returnsAuth() {
        // Match by message pattern when the throwable type isn't an HTTP exception.
        // ErrorCategory.Auth is detected by either message keyword or HTTP-style status patterns.
        assertEquals(
            ErrorCategory.Auth,
            categorize(RuntimeException("HTTP 401 Unauthorized")),
        )
        // 403 is NOT Auth: signed in, not permitted. It is a client error that keeps its reason.
        assertEquals(
            ErrorCategory.ClientError(403),
            categorize(RuntimeException("403 Forbidden")),
        )
    }

    @Test
    fun categorize_serverError_returnsServerWithHttpCode() {
        // Phase 07: Server is now a data class carrying the exact 5xx code.
        assertEquals(
            ErrorCategory.Server(httpCode = 500),
            categorize(RuntimeException("HTTP 500 Internal Server Error")),
        )
        assertEquals(
            ErrorCategory.Server(httpCode = 503),
            categorize(RuntimeException("503 Service Unavailable")),
        )
    }

    @Test
    fun categorize_5xx_carriesExactCode() {
        // Spot check that 502/505 etc. each propagate their exact code.
        val cat502 = categorize(RuntimeException("HTTP 502 Bad Gateway"))
        val server502 = assertIs<ErrorCategory.Server>(cat502)
        assertEquals(502, server502.httpCode)

        val cat505 = categorize(RuntimeException("505 HTTP Version Not Supported"))
        val server505 = assertIs<ErrorCategory.Server>(cat505)
        assertEquals(505, server505.httpCode)
    }

    @Test
    fun categorize_causeChainInspected() {
        class IOException : RuntimeException("net")
        val wrapper = RuntimeException("wrapped", IOException())
        assertEquals(
            ErrorCategory.Network,
            categorize(wrapper),
            "categorize should inspect cause chain, not just the top throwable.",
        )
    }

    @Test
    fun categorize_isStable_acrossInvocations() {
        val err = RuntimeException("HTTP 500")
        assertEquals(categorize(err), categorize(err))
    }

    @Test
    fun categorize_offlineException_returnsNetwork() {
        // Contract that PagingScreenStream's offline guard relies on: throwing
        // an OfflineException must route the UI through the no-network treatment.
        // Class name contains "Offline" → matchesNetworkClassName() heuristic catches it.
        assertEquals(
            ErrorCategory.Network,
            categorize(OfflineException()),
            "OfflineException must categorize as Network so framework UI shows the offline treatment.",
        )
    }

    @Test
    fun categorize_http429InMessage_returnsRateLimit() {
        // Regression guard for the double-escape bug: RATE_LIMIT_HTTP_REGEX must match
        // "HTTP 429" as a word boundary, not as the literal string "\\b...429\\b".
        assertEquals(
            ErrorCategory.RateLimit,
            categorize(RuntimeException("HTTP 429 Too Many Requests")),
        )
    }

    @Test
    fun categorize_429Alone_returnsRateLimit() {
        assertEquals(
            ErrorCategory.RateLimit,
            categorize(RuntimeException("Status: 429")),
        )
    }

    // ─── Phase 07: new sealed-interface variants ─────────────────────────────

    @Test
    fun categorize_http402_returnsQuotaExceeded() {
        // 402 Payment Required → billing/quota CTA, never retry.
        assertEquals(
            ErrorCategory.QuotaExceeded,
            categorize(RuntimeException("HTTP 402 Payment Required")),
        )
    }

    @Test
    fun categorize_http507_returnsQuotaExceeded() {
        // 507 Insufficient Storage — server-side quota, treat as billing.
        assertEquals(
            ErrorCategory.QuotaExceeded,
            categorize(RuntimeException("HTTP 507 Insufficient Storage")),
        )
    }

    @Test
    fun categorize_http408_returnsTimeoutConnect() {
        // 408 Request Timeout — never reached the server's body parser.
        assertEquals(
            ErrorCategory.Timeout.Connect,
            categorize(RuntimeException("HTTP 408 Request Timeout")),
        )
    }

    @Test
    fun categorize_http504_returnsTimeoutRead() {
        // 504 Gateway Timeout — upstream didn't respond in time.
        assertEquals(
            ErrorCategory.Timeout.Read,
            categorize(RuntimeException("HTTP 504 Gateway Timeout")),
        )
    }

    @Test
    fun categorize_http400_returnsClientErrorWithCode() {
        val cat = categorize(RuntimeException("HTTP 400 Bad Request"))
        val client = assertIs<ErrorCategory.ClientError>(cat)
        assertEquals(400, client.httpCode)
    }

    @Test
    fun categorize_http404_returnsClientErrorWithCode() {
        val cat = categorize(RuntimeException("HTTP 404 Not Found"))
        val client = assertIs<ErrorCategory.ClientError>(cat)
        assertEquals(404, client.httpCode)
    }

    @Test
    fun categorize_http422_returnsClientErrorWithCode() {
        // Validation errors — UI should surface the body, not "session expired".
        val cat = categorize(RuntimeException("HTTP 422 Unprocessable Entity"))
        val client = assertIs<ErrorCategory.ClientError>(cat)
        assertEquals(422, client.httpCode)
    }

    @Test
    fun categorize_http401_takesPrecedenceOverClientError() {
        // 401 must still be Auth, not the generic ClientError bucket.
        assertEquals(
            ErrorCategory.Auth,
            categorize(RuntimeException("HTTP 401")),
        )
    }

    @Test
    fun categorize_http429_takesPrecedenceOverClientError() {
        // 429 must still be RateLimit, not ClientError.
        assertEquals(
            ErrorCategory.RateLimit,
            categorize(RuntimeException("HTTP 429")),
        )
    }

    @Test
    fun categorize_server500_carriesCode() {
        val cat = categorize(RuntimeException("HTTP 500"))
        val server = assertIs<ErrorCategory.Server>(cat)
        assertNotNull(server.httpCode)
        assertEquals(500, server.httpCode)
    }
}
