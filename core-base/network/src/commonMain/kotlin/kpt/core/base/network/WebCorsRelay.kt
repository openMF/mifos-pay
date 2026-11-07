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

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlin.coroutines.cancellation.CancellationException

/**
 * True when [host] matches allowlist [pattern]: an exact host (case-insensitive), or `*.example.com`,
 * which matches any subdomain of `example.com` but not `example.com` itself.
 */
fun corsRelayHostMatches(host: String, pattern: String): Boolean {
    val h = host.lowercase()
    val p = pattern.trim().lowercase()
    return if (p.startsWith("*.")) h.endsWith(p.substring(1)) else h == p
}

/**
 * A relay given as a path (`/cors`) is served by the site itself — resolved against [origin], the page's
 * origin. An absolute URL (`https://relay.example.com/cors`) is used as-is.
 */
fun resolveCorsRelayBase(relay: String, origin: String): String = if (relay.startsWith("/")) origin + relay else relay

/** Response header the relay worker (`_worker.js`) sets on EVERY answer it gives, errors included. */
const val CORS_RELAY_HEADER: String = "X-Kpt-Relay"

/**
 * Same-origin relays known to be ABSENT this session (the host serves no worker — GitHub Pages answers
 * `/cors` with its own 404 page). Shared by every client, so only the first call pays the round trip.
 */
// ponytail: plain set, no lock — web targets are single-threaded; guard it if a JVM caller ever relays.
internal object CorsRelayAvailability {
    private val absent = mutableSetOf<String>()
    fun isAbsent(relayBase: String): Boolean = relayBase in absent
    fun markAbsent(relayBase: String) {
        absent += relayBase
    }
}

internal class WebCorsRelayConfig {
    var relayBase: String = ""
    val hostPatterns = mutableListOf<String>()

    /** Fall back to a direct call when the relay is missing — only for a same-origin relay (see plugin). */
    var fallbackToDirect: Boolean = false
}

// Methods a direct retry may repeat after a NETWORK failure, when the relay might already have forwarded
// the request. POST/PATCH are excluded: repeating one could submit it twice.
private val IDEMPOTENT = setOf("GET", "HEAD", "OPTIONS", "PUT", "DELETE")

/**
 * Rewrites a request whose host matches the allowlist to `<relay>/?<full url>` — the target embedded
 * verbatim after `?`, the format cmp-web's `_worker.js` relay (and corsproxy.io-style relays) read.
 *
 * Runs in the `Send` hook — after the whole request pipeline, so after `defaultRequest` and
 * [DynamicBaseUrlPlugin] whatever the install order: the host matched here is the FINAL one, including a
 * runtime network-config switch.
 *
 * **Fallback (same-origin relay only).** The worker tags every answer with [CORS_RELAY_HEADER]. A response
 * WITHOUT it means no worker answered (a static host's 404/405 page): the request is replayed directly —
 * safe for any method, since nothing was forwarded — and the relay is remembered absent for the session
 * ([CorsRelayAvailability]), so later calls go direct at once. A response WITH it (the worker's own 403,
 * an upstream 4xx/5xx) is final and never retried. A network failure is retried directly only for an
 * idempotent method, and the relay is marked absent only if that direct call succeeds — so being offline
 * never disables the relay. An external https relay is never second-guessed: it cannot be told apart
 * from a missing worker (the header is not exposed cross-origin).
 *
 * The target is embedded as one raw path segment (`"?$originalUrl"`) with parameters cleared, so Ktor
 * neither re-encodes it nor appends a stray `=` to the last query parameter.
 */
internal val WebCorsRelayPlugin = createClientPlugin("WebCorsRelay", ::WebCorsRelayConfig) {
    val relayBase = pluginConfig.relayBase
    val patterns = pluginConfig.hostPatterns.toList()
    val fallback = pluginConfig.fallbackToDirect

    on(Send) { request ->
        val host = request.url.host
        if (patterns.none { corsRelayHostMatches(host, it) }) return@on proceed(request)
        if (fallback && CorsRelayAvailability.isAbsent(relayBase)) return@on proceed(request)

        val original = request.url.build()
        val relay = URLBuilder(relayBase)
        request.url.apply {
            protocol = relay.protocol
            this.host = relay.host
            port = relay.port
            parameters.clear()
            encodedParameters.clear()
            fragment = ""
            val relayPath = relay.encodedPathSegments.filter { it.isNotEmpty() }
            encodedPathSegments = listOf("") + relayPath + "?$original"
        }
        if (!fallback) return@on proceed(request)

        // Request bodies are OutgoingContent (ByteArray/Text for serialized bodies), so the same builder
        // can be sent twice.
        val relayed = try {
            proceed(request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            // expectSuccess turned a non-2xx into an exception: a tagged one is the worker's answer.
            if (e.response.headers[CORS_RELAY_HEADER] != null) throw e
            CorsRelayAvailability.markAbsent(relayBase)
            request.url.takeFrom(original)
            return@on proceed(request)
        } catch (e: Throwable) {
            if (request.method.value !in IDEMPOTENT) throw e
            request.url.takeFrom(original)
            val call = proceed(request)
            CorsRelayAvailability.markAbsent(relayBase)
            return@on call
        }
        if (relayed.response.headers[CORS_RELAY_HEADER] != null) return@on relayed
        CorsRelayAvailability.markAbsent(relayBase)
        request.url.takeFrom(original)
        proceed(request)
    }
}

/**
 * Installs [WebCorsRelayPlugin] for [hostPatterns] through the already-resolved [relayBase]; with
 * [fallbackToDirect] a missing relay degrades to direct calls.
 */
internal fun HttpClientConfig<*>.installCorsRelay(
    hostPatterns: List<String>,
    relayBase: String,
    fallbackToDirect: Boolean = false,
) {
    if (hostPatterns.isEmpty()) return
    install(WebCorsRelayPlugin) {
        this.relayBase = relayBase
        this.hostPatterns += hostPatterns
        this.fallbackToDirect = fallbackToDirect
    }
}
