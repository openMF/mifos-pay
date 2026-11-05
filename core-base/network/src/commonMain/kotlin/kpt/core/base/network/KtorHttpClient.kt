/*
 * Copyright 2025 Mifos Initiative
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
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.DigestAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.auth.providers.digest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kpt.core.base.security.CertificatePinConfig
import co.touchlab.kermit.Logger.Companion as KermitLogger

/**
 * Creates a Ktor [HttpClient] on the target's own engine (OkHttp, Darwin, JS fetch, CIO).
 *
 * [config] is applied on top of the engine defaults, so shared setup — JSON, logging, auth,
 * certificate pinning — is declared once in common code and the engine choice stays per platform.
 */
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient

/**
 * Platform hook: installs a CORS proxy plugin on web targets; no-op on native/Android/Desktop.
 *
 * Called automatically by [setupDefaultHttpClient] when [proxiedHosts] is non-empty.
 * On web, requests to any host in [proxiedHosts] are transparently rewritten through
 * [corsProxyBaseUrl] (format: "https://proxy-host/path?" — original URL is appended as-is).
 */
internal expect fun HttpClientConfig<*>.installProxyPlugin(
    proxiedHosts: List<String>,
    corsProxyBaseUrl: String,
)

/**
 * The auth-credential providers for [setupDefaultHttpClient], grouped so the client builder stays under
 * the parameter-count limit. All null (the default) installs no `Auth` plugin. When more than one is
 * supplied, Bearer takes precedence, then Basic, then Digest (mirrors the builder's `when` order).
 *
 * @param basic Provider for Basic authentication credentials.
 * @param digest Provider for Digest authentication credentials.
 * @param bearer Provider for Bearer token authentication.
 * @param bearerRefresh Optional refresh logic for Bearer tokens (only used if [bearer] is configured).
 */
data class AuthProviders(
    val basic: (() -> BasicAuthCredentials)? = null,
    val digest: (() -> DigestAuthCredentials)? = null,
    val bearer: (() -> BearerTokens)? = null,
    val bearerRefresh: (() -> BearerTokens)? = null,
)

/**
 * The three Ktor timeouts for [setupDefaultHttpClient], grouped for the same reason
 * [AuthProviders] is: the builder is capped at 20 parameters by detekt's `LongParameterList`, and
 * adding `connectTimeout` as a 21st flat parameter crossed it. Grouping is the fix the limit is
 * asking for — raising the threshold would hide the next one.
 *
 * All three are milliseconds, and all three matter independently:
 *
 * @param request Whole-request-lifecycle budget. Covers connect + send + server think-time +
 *   receive, so it must be the largest of the three or it masks the other two.
 * @param socket Idle gap between bytes once a connection is established. Catches a server that
 *   accepted the connection and then stopped sending.
 * @param connect Budget to ESTABLISH the connection. Without a bound here a stalled TCP/TLS
 *   handshake surfaces as a long spinner rather than an error the offline-first layer can retry —
 *   the default is deliberately well under [request] so a dead host fails fast.
 */
data class HttpTimeouts(
    val request: Long = 60_000L,
    val socket: Long = 60_000L,
    val connect: Long = 20_000L,
)

/**
 * Provides a default [HttpClientConfig] setup for use with a Ktor-based HTTP client.
 *
 * This function simplifies client configuration by handling common concerns such as:
 * - Authentication (Bearer, Basic, Digest)
 * - Default headers
 * - Timeouts
 * - Logging
 * - JSON serialization
 *
 * It can be passed directly into a Ktor client builder via the `config` lambda:
 * ```kotlin
 * val client = httpClient(setupDefaultHttpClient(baseUrl = "https://api.example.com"))
 * ```
 *
 * @param baseUrl The base URL to be applied to all requests unless explicitly overridden.
 * @param isReleaseBuild When true (the default — a caller must opt INTO verbose logging), nothing is logged
 *   and JSON is not pretty-printed. [restClientConfig] sets it from the build type via [NetworkPolicy].
 * @param authRequiredUrl A list of hostnames that require authentication.
 * @param defaultHeaders STATIC headers applied to every request — captured once, at client build.
 * @param dynamicHeaders Headers resolved on EVERY request. This is the hook a runtime credential
 *        needs: the client is a singleton built at Koin start, long before anyone signs in, so a
 *        value captured in [defaultHeaders] can never become a post-login token. The lambda is
 *        invoked inside `defaultRequest`, which runs per call.
 * @param timeouts Request / socket / connect budgets, grouped (see [HttpTimeouts]). Defaults bound
 *   connect well under request so a stalled handshake fails fast instead of hanging a spinner.
 * @param httpLogger A logger used for HTTP logging (defaults to `Logger.DEFAULT`).
 * @param httpLogLevel Level of HTTP logging (e.g. `LogLevel.ALL`).
 * @param loggableHosts A list of hostnames for which HTTP logging is enabled.
 * @param sensitiveHeaders Headers masked in logs — credentials and session cookies by default.
 * @param redactQueryParams Query parameters whose values are masked in logged URLs (see [maskQueryParams]).
 * @param jsonConfig Custom [Json] configuration used by `ContentNegotiation`.
 * @param authProviders The Basic/Digest/Bearer credential providers (see [AuthProviders]). Default
 *   installs no auth plugin.
 * @param certificatePinConfig TLS certificate pin configuration. Pins are applied by platform
 *   engines that support it (OkHttp on Android/Desktop). Other platforms ignore this parameter.
 * @param proxiedHosts Hostnames whose requests should be transparently routed through
 *   [corsProxyBaseUrl] on browser targets (web only). Ignored on Android/Desktop/iOS.
 *   Example: `listOf("api.stlouisfed.org")` routes FRED API calls via the CORS proxy.
 * @param corsProxyBaseUrl Base URL of the CORS relay (format: `https://proxy/?<original-url>`), or null
 *   for none — the default. A relay sees every request it forwards, so it is a third-party processor
 *   and must be DECLARED (`app-profile/app.yaml#network.web_cors_proxy`), never implied.
 * @param multiUrlProvider Optional [MultiUrlConfigProvider] for apps that expose more than one
 *   named endpoint. When supplied, [DynamicBaseUrlPlugin] resolves the base URL per [urlType] at
 *   call time. Takes precedence over [dynamicUrlProvider]. Default null preserves the plain
 *   static-URL path (no regression).
 * @param urlType The named endpoint to resolve from [multiUrlProvider]. Ignored unless
 *   [multiUrlProvider] is set. Defaults to [UrlType.MAIN].
 *
 * @return A configuration lambda to be passed into the Ktor [HttpClient].
 */
@Suppress("UnusedParameter")
fun setupDefaultHttpClient(
    baseUrl: String,
    isReleaseBuild: Boolean = true,
    authRequiredUrl: List<String> = emptyList(),
    defaultHeaders: Map<String, String> = emptyMap(),
    dynamicHeaders: () -> Map<String, String> = { emptyMap() },
    timeouts: HttpTimeouts = HttpTimeouts(),
    httpLogger: Logger = Logger.DEFAULT,
    httpLogLevel: LogLevel = if (isReleaseBuild) LogLevel.NONE else LogLevel.ALL,
    loggableHosts: List<String> = emptyList(),
    sensitiveHeaders: List<String> = DEFAULT_SENSITIVE_HEADERS,
    redactQueryParams: List<String> = NetworkPolicy.DEFAULT_REDACTED_QUERY_PARAMS,
    jsonConfig: Json = Json {
        prettyPrint = !isReleaseBuild
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        // Cross-version tolerance: an unrecognized enum value (e.g. a server enum the client
        // doesn't model yet) OR an explicit JSON null on a non-null field with a default coerces
        // to that default instead of throwing a SerializationException. Many DTOs already document
        // a `*.UNKNOWN`/default fallback that only works with this flag enabled.
        coerceInputValues = true
    },
    authProviders: AuthProviders = AuthProviders(),
    certificatePinConfig: CertificatePinConfig = CertificatePinConfig.default(),
    proxiedHosts: List<String> = emptyList(),
    corsProxyBaseUrl: String? = null,
    multiUrlProvider: MultiUrlConfigProvider? = null,
    urlType: UrlType = UrlType.MAIN,
    dynamicUrlProvider: DynamicUrlConfigProvider? = null,
): HttpClientConfig<*>.() -> Unit = {
    // A NON-2XX MUST NOT REACH A CALLER AS DATA.
    //
    // Without this, Ktor hands the error BODY to ContentNegotiation, which deserializes it onto the
    // expected success type. Nothing throws, so every layer above treats the result as real:
    //
    //   list read  -> the error object does not match `List<T>`, so it either throws a confusing
    //                 JsonConvertException ("Expected start of the array '[', but had '{'") or,
    //                 when the shapes happen to be compatible, yields an EMPTY LIST.
    //   write      -> a DTO whose fields are nullable-with-defaults parses "fine", so
    //                 `MutationGateway.command` returns MutationResult.Applied -- a FAILED WRITE
    //                 REPORTED AS SUCCESS.
    //
    // MEASURED on device 2026-10-20 (mifos-x-field-officer-app, live Fineract): a 401 on
    // POST /clients/3/notes returned `Applied`; the screen navigated back as though the note were
    // saved, and the note did not exist on the server. The paired list read rendered "No Item Found"
    // rather than "signed out". The user is told the opposite of the truth in both directions, which
    // is the worst failure mode available to a write.
    //
    // With expectSuccess the 4xx/5xx raises ClientRequestException / ServerResponseException, and
    // both paths already have correct destinations: `DefaultMutationGateway.command` catches
    // Throwable into MutationResult.Failed, and the read path surfaces ScreenState.Error -- or
    // Unauthenticated, which `ScreenState` already models and which could never be reached before.
    //
    // Opt-OUT per client, not a global policy: an endpoint that genuinely encodes outcomes in
    // non-2xx bodies sets `expectSuccess = false` on its own client and handles them explicitly.
    expectSuccess = true

    val refreshMutex = Mutex()
    val basicCredentialsProvider = authProviders.basic
    val digestCredentialsProvider = authProviders.digest
    val bearerTokensProvider = authProviders.bearer
    val bearerRefreshProvider = authProviders.bearerRefresh

    when {
        bearerTokensProvider != null -> {
            install(Auth) {
                bearer {
                    loadTokens { bearerTokensProvider() }
                    if (bearerRefreshProvider != null) {
                        refreshTokens {
                            refreshMutex.withLock {
                                val currentTokens = bearerTokensProvider()
                                if (currentTokens != oldTokens) {
                                    currentTokens
                                } else {
                                    bearerRefreshProvider()
                                }
                            }
                        }
                    }
                    sendWithoutRequest { request ->
                        request.url.host in authRequiredUrl
                    }
                }
            }
        }

        basicCredentialsProvider != null -> {
            install(Auth) {
                basic {
                    credentials {
                        basicCredentialsProvider()
                    }
                    sendWithoutRequest { request ->
                        request.url.host in authRequiredUrl
                    }
                }
            }
        }

        digestCredentialsProvider != null -> {
            install(Auth) {
                digest {
                    credentials {
                        digestCredentialsProvider()
                    }
                }
            }
        }
    }

    defaultRequest {
        url(baseUrl)
        defaultHeaders.forEach { (key, value) ->
            headers.append(key, value)
        }
        // Evaluated HERE, per request — that is the whole point. A login that lands after this client
        // was constructed still reaches the next call.
        dynamicHeaders().forEach { (key, value) ->
            headers.append(key, value)
        }
    }

    // A CONNECT bound, not only request + socket.
    //
    // Without one, a call that never completes its TCP/TLS handshake is governed only by the 60s
    // request timeout — which a user reads as an indefinite spinner rather than a failure, and which
    // an offline-first layer cannot turn into a retry.
    //
    // `mifos-pay` hit this first and worked around it per-client, with the diagnosis written out in
    // its own NetworkModule: "The corebase default sets request+socket to 60s but no connectTimeout;
    // a shorter connect bound + request timeout surfaces a stalled call as an Error/Retry the
    // offline-first layer handles, instead of a long spinner." That workaround covered only its three
    // hand-wired clients; every client built through this factory still missed it, which is how
    // `mifos-x-field-officer-app` met the same stall on device in 2026-10.
    //
    // 20s is the value that workaround settled on. Overridable per client for a slow endpoint.
    install(HttpTimeout) {
        requestTimeoutMillis = timeouts.request
        socketTimeoutMillis = timeouts.socket
        connectTimeoutMillis = timeouts.connect
    }

    install(Logging) {
        logger = httpLogger
        level = httpLogLevel
        filter { request ->
            loggableHosts.any { host ->
                request.url.host.contains(host)
            }
        }
        sanitizeHeader { header ->
            sensitiveHeaders.any { it.equals(header, ignoreCase = true) }
        }
        logger = object : Logger {
            override fun log(message: String) {
                // URLs carry credentials too (an api_key query param): mask them before the line exists.
                KermitLogger.d(tag = "KtorClient", messageString = maskQueryParams(message, redactQueryParams))
            }
        }
    }

    install(ContentNegotiation) {
        json(jsonConfig)
    }
    // A typed body is JSON unless the call says otherwise — so no access point has to declare it.
    install(JsonBodyContentType)

    // Runtime base-URL switching (opt-in). The [DynamicBaseUrlPlugin] rewrites each request's
    // host/scheme at call time — the [baseUrl] above becomes a static fallback. A
    // [MultiUrlConfigProvider] + [urlType] resolves the base URL per named endpoint (multi-server
    // apps); a plain [DynamicUrlConfigProvider] keeps the single-URL behaviour. Consumers wire a
    // provider in their DI (see core/network NetworkModule); both default null, so the plain
    // static-URL path is unchanged (no regression).
    when {
        multiUrlProvider != null -> install(DynamicBaseUrlPlugin) {
            multiConfigProvider = multiUrlProvider
            this.urlType = urlType
            staticBaseUrl = baseUrl
        }

        dynamicUrlProvider != null -> install(DynamicBaseUrlPlugin) {
            configProvider = dynamicUrlProvider
            staticBaseUrl = baseUrl
        }
    }

    // No relay unless one is declared: routing traffic through a third party is never a default.
    corsProxyBaseUrl?.let { installProxyPlugin(proxiedHosts, it) }
}

/** Headers masked in HTTP logs by default — every credential or session carrier the template sends. */
val DEFAULT_SENSITIVE_HEADERS: List<String> = listOf(
    HttpHeaders.Authorization,
    HttpHeaders.ProxyAuthorization,
    HttpHeaders.Cookie,
    HttpHeaders.SetCookie,
    "apikey",
    "X-Api-Key",
)
