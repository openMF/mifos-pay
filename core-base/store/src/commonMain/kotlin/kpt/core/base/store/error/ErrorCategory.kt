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

/**
 * High-level classification of a [Throwable] for state-aware UI rendering.
 *
 * Pure logic — no platform calls, no side effects. Lives in `core-base/store` (state layer)
 * so both UI and non-UI consumers can branch on the same categorization.
 *
 * The classification is heuristic: it inspects the throwable's class name, message, and
 * cause chain. It is intentionally conservative — anything that doesn't match a specific
 * pattern falls through to [Generic].
 *
 * **Sealed-interface (Phase 07).** Some variants carry payload: [Server] reports the HTTP
 * code (`500` / `503`) so UI / telemetry / retry-policy can branch on the exact status,
 * and [ClientError] does the same for non-auth/non-rate-limit 4xx codes. Pattern-matching
 * call sites should use `is ErrorCategory.Server` (with optional `cat.httpCode`) rather
 * than the previous enum-constant form `ErrorCategory.Server`.
 */
sealed interface ErrorCategory {
    /** No connectivity, captive portal, or transport-level failure. Suggest retry + network UX. */
    data object Network : ErrorCategory

    /** HTTP 401/403 or auth-equivalent. Suggest session refresh / login UX. */
    data object Auth : ErrorCategory

    /** HTTP 429 — server is rate-limiting this client. Suggest backing off and retrying. */
    data object RateLimit : ErrorCategory

    /**
     * HTTP 4xx that is not specifically classified as [Auth] (401/403), [RateLimit] (429),
     * [Timeout.Connect] (408), or [QuotaExceeded] (402). The [httpCode] is the exact
     * status (e.g. `400`, `404`, `409`, `422`). Suggest surfacing the response body to
     * the user — these are usually input/validation errors, not infrastructure.
     *
     * @property httpCode the 4xx status. Non-null — a client error always arrives with a response.
     */
    data class ClientError(val httpCode: Int) : ErrorCategory

    /**
     * HTTP 5xx (other than [Timeout.Read] / 504) or upstream service error. The [httpCode]
     * is the exact status when available (e.g. `500`, `502`, `503`); null when categorized
     * heuristically without a parseable code. Suggest retry-with-backoff + status UX.
     *
     * @property httpCode the 5xx status, or null when the server failed without one (a dropped connection mid-
     *   response).
     */
    data class Server(val httpCode: Int? = null) : ErrorCategory

    /**
     * HTTP 402 (Payment Required) or 507 (Insufficient Storage), or any "out of quota"
     * billing-style error. UI should surface a quota/billing CTA, never retry blindly.
     */
    data object QuotaExceeded : ErrorCategory

    /** Timeouts split by which leg of the request timed out. */
    sealed interface Timeout : ErrorCategory {
        /** HTTP 408 — request never made it to the server (or server never responded to the headers). */
        data object Connect : Timeout

        /** HTTP 504 — server received the request but the upstream didn't reply in time. */
        data object Read : Timeout
    }

    /** Anything not specifically classified. UI shows the generic error treatment. */
    data object Generic : ErrorCategory
}

/**
 * Classifies [error] into an [ErrorCategory] by inspecting its class name, message, and
 * cause chain.
 *
 * Matching rules (first match wins):
 * - Network: class or cause name contains `IOException`, `Connect`, `Timeout`, `UnknownHost`,
 *   `SSLException`, `Offline`
 * - HTTP code parsing (from message): 401/403 → Auth · 408 → Timeout.Connect · 429 → RateLimit ·
 *   402/507 → QuotaExceeded · 504 → Timeout.Read · other 4xx → ClientError(code) ·
 *   other 5xx → Server(code)
 * - Auth (message-keyword fallback): contains `Unauthorized` / `Forbidden`
 * - RateLimit (message-keyword fallback): contains `Too Many Requests` / `Rate Limit`
 * - Generic: fallback
 *
 * Stable across invocations for the same throwable instance.
 */
@Suppress("CyclomaticComplexMethod", "ReturnCount")
fun categorize(error: Throwable): ErrorCategory {
    if (error.classNameChain().any { it.matchesNetworkClassName() }) {
        return ErrorCategory.Network
    }
    return error.messageChain()
        .firstNotNullOfOrNull { msg ->
            // Try the structured HTTP-code path first — it produces richer categories
            // (carrying the exact code) and disambiguates 4xx/5xx subclasses.
            msg.extractHttpCode()?.let { code -> categorizeHttpCode(code) }
                ?: when {
                    msg.matchesAuthKeyword() -> ErrorCategory.Auth
                    msg.matchesRateLimitKeyword() -> ErrorCategory.RateLimit
                    else -> null
                }
        }
        ?: ErrorCategory.Generic
}

private fun Throwable.classNameChain(): Sequence<String> = sequence {
    var t: Throwable? = this@classNameChain
    while (t != null) {
        t::class.simpleName?.let { yield(it) }
        t = t.cause
    }
}

private fun Throwable.messageChain(): Sequence<String> = sequence {
    var t: Throwable? = this@messageChain
    while (t != null) {
        t.message?.let { yield(it) }
        t = t.cause
    }
}

/**
 * Transport-failure class names, matched on the class-name chain.
 *
 * The list is NAME-based because this is commonMain — there is no shared supertype to test against
 * (`java.io.IOException` does not exist on iOS or Web). That makes coverage a question of evidence,
 * not intuition: an engine whose exception happens not to contain one of these substrings silently
 * categorizes Generic, and `DecisionEngine.isExpectedWhenOffline()` then refuses the offline-first
 * rule and shows a blocking NoNetwork on a screen that should have shown its own Empty.
 *
 * Two such holes were measured rather than guessed:
 *
 *  - `HttpRequestException` — Ktor's Darwin engine (iOS/macOS) raises `DarwinHttpRequestException`
 *    (and the deprecated `IosHttpRequestException`) for a transport failure, wrapping the NSError.
 *    Confirmed against the cached `ktor-client-darwin` 3.2.1 klib. The NSError is a PROPERTY, not a
 *    `cause`, so the chain never reaches its "offline" text. Matching the `HttpRequestException`
 *    suffix is deliberately narrow: Ktor reports HTTP STATUS failures as `ClientRequestException` /
 *    `ServerResponseException`, neither of which contains it, so a 500 stays real signal.
 *  - `Socket` — `java.net.SocketException` IS an IOException subclass, but a name match cannot see
 *    that, and "SocketException" contains none of the original six patterns. A dropped connection
 *    on JVM/Android categorized Generic while its sibling `SocketTimeoutException` matched via
 *    "Timeout" — an inconsistency nothing in the API surface hinted at.
 *  - `JsError` — the JS and wasmJs engines wrap EVERY JavaScript-side failure in
 *    `JsError(origin: dynamic) : Throwable("Error from javascript[...]")`, including a rejected
 *    `fetch` and a rejected `ReadableStreamDefaultReader.read()`. Verified against ktor `main`:
 *    `ktor-client-core/js/.../JsClientEngine.kt` and
 *    `ktor-client-core/wasmJs/.../browser/BrowserFetch.kt` — the engine moved OUT of
 *    `ktor-client-js` into `ktor-client-core`, which is why hunting for a cached `ktor-client-js`
 *    artifact turned up nothing. The name carries no transport word at all — not IOException, not
 *    Connect, not Socket — so until this entry, web was the last platform still falling through to
 *    a blocking NoNetwork on an empty offline cache: the same bug the Darwin entry fixed for iOS.
 *    The browser's own cause (`TypeError: Failed to fetch`) hangs off `origin`, not `cause`, so the
 *    chain never sees it either.
 *
 * Each entry above is pinned by a case in ErrorCategoryTest, including a negative one asserting
 * `ServerResponseException` does NOT become Network.
 */
private fun String.matchesNetworkClassName(): Boolean =
    contains("IOException", ignoreCase = true) ||
        contains("Connect", ignoreCase = true) ||
        contains("Timeout", ignoreCase = true) ||
        contains("UnknownHost", ignoreCase = true) ||
        // `SSL`, not `SSLException`: the JVM's TLS failures are `SSLHandshakeException` /
        // `SSLPeerUnverifiedException`, which do not contain "SSLException" — measured 2026-10-25,
        // a server whose TLS was down categorized Generic and sign-in blamed the password.
        contains("SSL", ignoreCase = true) ||
        // java.io.EOFException (an IOException the name match cannot see) — the cause under that
        // handshake failure: the peer closed the connection.
        contains("EOFException", ignoreCase = true) ||
        contains("Offline", ignoreCase = true) ||
        contains("HttpRequestException", ignoreCase = true) ||
        contains("Socket", ignoreCase = true) ||
        contains("JsError", ignoreCase = true)

/**
 * Matches any 3-digit HTTP status code in a message, optionally preceded by `HTTP `.
 * Captures the numeric value.
 */
private val HTTP_CODE_REGEX = Regex("""\b(?:HTTP\s+)?(\d{3})\b""")

private fun String.extractHttpCode(): Int? =
    HTTP_CODE_REGEX.find(this)?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it in 100..599 }

private fun categorizeHttpCode(code: Int): ErrorCategory = when (code) {
    // 401 only. A 403 is "signed in, but not permitted" — signing in again cannot fix it, and as
    // Auth it became ScreenState.Unauthenticated, which carries NO error, so the server's own reason
    // ("User has no authority to READ collectionsheets") was replaced by generic copy. As a
    // ClientError it renders as an error that keeps the server's message. Measured 2026-10-24.
    401 -> ErrorCategory.Auth
    408 -> ErrorCategory.Timeout.Connect
    429 -> ErrorCategory.RateLimit
    402, 507 -> ErrorCategory.QuotaExceeded
    504 -> ErrorCategory.Timeout.Read
    in 400..499 -> ErrorCategory.ClientError(code)
    in 500..599 -> ErrorCategory.Server(code)
    else -> ErrorCategory.Generic
}

// "Forbidden" is deliberately absent — see categorizeHttpCode: a 403 is not an authentication failure.
private fun String.matchesAuthKeyword(): Boolean =
    contains("Unauthorized", ignoreCase = true)

private fun String.matchesRateLimitKeyword(): Boolean =
    contains("Too Many Requests", ignoreCase = true) ||
        contains("Rate Limit", ignoreCase = true) ||
        contains("RateLimit", ignoreCase = true)
