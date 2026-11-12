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

import io.ktor.client.plugins.logging.LogLevel
import org.koin.core.scope.Scope

/**
 * How much of an HTTP exchange may be written to the device log. Ordered least to most revealing.
 *
 * A log line is personal data the moment a body carries a name, an account or a token, and device logs
 * leave the app's control (adb, bug-report bundles, crash breadcrumbs). So the release default is
 * [NONE], and [BODY] is refused for release builds outright — see [NetworkPolicy.logDetail].
 */
enum class HttpLogDetail(internal val level: LogLevel) {
    /** Nothing at all — the release default. */
    NONE(LogLevel.NONE),

    /** Method, URL (query credentials masked) and status only. */
    INFO(LogLevel.INFO),

    /** [INFO] plus headers (credential headers masked) — the most a release build may log. */
    HEADERS(LogLevel.HEADERS),

    /** Everything including request/response bodies — debug builds only. */
    BODY(LogLevel.ALL),
}

/**
 * How a WEB build reaches servers that do not answer CORS for the site's origin —
 * `app-profile/app.yaml#network.web_cors.mode`.
 */
enum class WebCorsMode {
    /**
     * Requests to an allowlisted host go through the relay ([NetworkPolicy.webCorsProxy]): the browser
     * talks to its own origin, so the server's CORS policy never applies. The default.
     */
    RELAY,

    /**
     * No rewriting: the browser calls each server itself. Nothing but the server sees the traffic, but
     * every server must allow the site's origin, or the browser blocks the call.
     */
    DIRECT,
}

/**
 * App-wide network privacy policy — GENERATED from `app-profile/app.yaml#network` (`logging`,
 * `web_cors`) onto `AppAccessPoints.policy` by `syncForkConfig`. Every default is the
 * secure choice, so a fork whose app.yaml predates these keys gets the safe behaviour on sync.
 *
 * @property releaseLogging what a release build may log; never more than [HttpLogDetail.HEADERS].
 * @property debugLogging what a debug build may log; [HttpLogDetail.BODY] is fine there, it never ships.
 * @property redactQueryParams query parameters whose values are masked in every logged URL (credentials
 *   such as FRED's `api_key` travel in the query string). Matched case-insensitively; the defaults are
 *   always applied and app.yaml adds to them.
 * @property webCorsMode [WebCorsMode.RELAY] or [WebCorsMode.DIRECT] (`web_cors.mode`).
 * @property webCorsProxy the CORS relay web builds route allowlisted hosts through (`web_cors.relay`): a
 *   same-origin path (`/cors`, served by cmp-web's `_worker.js`) or an https:// external relay. A relay
 *   sees every request it forwards, so a hand-built policy has none (`null`); the generated one sets it.
 * @property webCorsAllowlist host patterns routed through the relay — an exact host or `*.example.com`
 *   (subdomains only). Generated from every access point's host plus `web_cors.hosts`; the deployed
 *   worker relays the same list (`relay-allowlist.json`).
 */
data class NetworkPolicy(
    val releaseLogging: HttpLogDetail = HttpLogDetail.NONE,
    val debugLogging: HttpLogDetail = HttpLogDetail.BODY,
    val redactQueryParams: List<String> = emptyList(),
    val webCorsProxy: String? = null,
    val webCorsMode: WebCorsMode = WebCorsMode.RELAY,
    val webCorsAllowlist: List<String> = emptyList(),
) {
    /** The relay a web build uses, or null when none applies ([WebCorsMode.DIRECT], or none declared). */
    val activeWebCorsRelay: String?
        get() = webCorsProxy.takeIf { webCorsMode == WebCorsMode.RELAY }

    /**
     * The host patterns [ap]'s client relays: the allowlist plus the point's legacy `proxied_host`.
     * Empty in [WebCorsMode.DIRECT].
     */
    fun webCorsHostsFor(ap: AccessPoint): List<String> =
        if (activeWebCorsRelay == null) emptyList() else (webCorsAllowlist + listOfNotNull(ap.proxiedHost)).distinct()

    /** The detail to log at for this build — a release build is clamped to headers at most. */
    fun logDetail(debugBuild: Boolean): HttpLogDetail =
        if (debugBuild) debugLogging else minOf(releaseLogging, HttpLogDetail.HEADERS)

    /** Every query parameter to mask: the built-in credential names plus the app's own. */
    val allRedactedQueryParams: List<String>
        get() = (DEFAULT_REDACTED_QUERY_PARAMS + redactQueryParams).distinctBy { it.lowercase() }

    /** The built-in redaction list shared by every policy. */
    companion object {
        /** Query parameters that carry credentials often enough to mask everywhere. */
        val DEFAULT_REDACTED_QUERY_PARAMS: List<String> = listOf(
            "api_key", "apikey", "key", "token", "access_token", "refresh_token", "id_token",
            "password", "secret", "client_secret", "signature", "sig", "code",
        )
    }
}

/**
 * Mask the values of [params] in [message] — any `name=value` pair after `?` or `&`, so a logged URL
 * keeps its shape (useful for debugging) without the credential.
 */
fun maskQueryParams(message: String, params: List<String>): String {
    if (params.isEmpty()) return message
    val names = params.joinToString("|") { Regex.escape(it) }
    return Regex("([?&](?:$names)=)[^&#\\s\"]*", RegexOption.IGNORE_CASE).replace(message, "$1***")
}

/**
 * True only for a DEBUG build of the running app — never "a debugger is attached" (that is
 * `kpt.core.base.security.isReleaseBuild`, a tamper signal, and wrong for this: a debug build with no
 * debugger would read as release). Unknown = false, so an undetectable platform gets release logging.
 */
expect fun Scope.isDebugBuild(): Boolean
