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

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClientConfig
import org.koin.core.module.Module

/**
 * core-base/network transport DSL — the streamlined, template-owned way to wire a REST API interface.
 *
 * A consumer's `ForkNetworkModule` declares an API in ONE line by referencing the id of an access
 * point in its [AccessPointRegistry] (SoT: `app-profile/app.yaml#network.access_points`). `core-base`
 * auto-builds the Ktor client + Ktorfit from the access point's base URL + loggable host, so the consumer
 * writes ONLY the API interface + its generated `createXApi()` factory — no per-API Ktorfit boilerplate.
 */

/**
 * Build a Ktorfit for the REST access point declared under [accessPointId]. Base URL + loggable host +
 * proxied host come from this [AccessPointRegistry]; throws if the id isn't declared.
 *
 * [headerProvider] supplies headers sent on EVERY request to this endpoint — see
 * [DefaultHeaderProvider] for what belongs there.
 *
 * [runtimeHeaders] resolves the point's DECLARED `headers[]` (app-profile) at request time: static
 * values are baked in, `runtimeKey` ones are read from the store on every call, so a credential
 * written at login reaches requests made by a client that already existed.
 *
 * [onUnauthorized], when given, is told of every 401 on a request that carried credentials — see
 * [UnauthorizedHandler]. [networkConfigs], when given, resolves this point's URL and header values per
 * request — runtime network configs, see [NetworkConfigRepository]. [debugBuild] selects which half of
 * [AccessPointRegistry.policy] applies; false (release) is the safe default for a caller that cannot tell.
 */
fun AccessPointRegistry.ktorfitFor(
    accessPointId: String,
    headerProvider: DefaultHeaderProvider = DefaultHeaderProvider.None,
    runtimeHeaders: RuntimeHeaderStore = RuntimeHeaderStore(),
    onUnauthorized: UnauthorizedHandler? = null,
    networkConfigs: NetworkConfigRepository? = null,
    debugBuild: Boolean = false,
): Ktorfit {
    val ap = byId(accessPointId)
        ?: error(
            "No network access point '$accessPointId' declared in app-profile network.access_points " +
                "(AccessPointRegistry). Declare it there, or fix the id.",
        )
    // The Ktorfit wrapper stays per API (it is cheap and holds no connections); the CLIENT is
    // shared per access point — see AccessPointRegistry.restClients for the measurement.
    return Ktorfit.Builder()
        .httpClient(
            client = sharedRestClient(accessPointId) {
                httpClient(
                    restClientConfig(
                        ap = ap,
                        headerProvider = headerProvider,
                        runtimeHeaders = runtimeHeaders,
                        onUnauthorized = onUnauthorized,
                        networkConfigs = networkConfigs,
                        policy = policy,
                        debugBuild = debugBuild,
                    ),
                )
            },
        )
        .build()
}

/**
 * The client configuration [ktorfitFor] builds an access point's shared client with — public so it can
 * be exercised on a test engine exactly as the app runs it. [policy] + [debugBuild] decide what is logged
 * and whether web routes [AccessPoint.proxiedHost] through a CORS relay.
 */
fun restClientConfig(
    ap: AccessPoint,
    headerProvider: DefaultHeaderProvider = DefaultHeaderProvider.None,
    runtimeHeaders: RuntimeHeaderStore = RuntimeHeaderStore(),
    onUnauthorized: UnauthorizedHandler? = null,
    networkConfigs: NetworkConfigRepository? = null,
    policy: NetworkPolicy = NetworkPolicy(),
    debugBuild: Boolean = false,
): HttpClientConfig<*>.() -> Unit = {
    setupDefaultHttpClient(
        baseUrl = ap.effectiveUrl,
        // Release builds log nothing unless app-profile says otherwise, and never bodies — a request
        // body is where a name, an account or a token lives.
        isReleaseBuild = !debugBuild,
        httpLogLevel = policy.logDetail(debugBuild).level,
        redactQueryParams = policy.allRedactedQueryParams,
        corsProxyBaseUrl = policy.webCorsProxy,
        loggableHosts = listOf(ap.loggableHost),
        proxiedHosts = ap.proxiedHost?.let { listOf(it) } ?: emptyList(),
        // Per access point: a key belongs to ONE endpoint, and sending it to the others leaks a
        // credential to hosts that never needed it.
        defaultHeaders = headerProvider.headersFor(ap.id),
        // The point's DECLARED headers, resolved per request so a runtimeKey picks up the value login
        // wrote after this client was built — with the runtime network config's header values applied.
        // This is also what makes ONE client per access point correct rather than merely cheaper.
        dynamicHeaders = {
            val resolved = runtimeHeaders.resolve(ap.headers)
            networkConfigs?.headersFor(ap, resolved) ?: resolved
        },
        // Runtime network configs: the URL is resolved per request, so an edit or a main switch
        // reaches a client that already exists. Null = the declared URL.
        dynamicUrlProvider = networkConfigs?.let { configs ->
            object : DynamicUrlConfigProvider {
                override fun getBaseUrl(): String = configs.urlFor(ap)
                override fun getLoggableHosts(): List<String> = listOf(ap.loggableHost)
            }
        },
    ).invoke(this)
    onUnauthorized?.let { notifyUnauthorized(ap.id, it) }
}

/**
 * Register a Ktorfit REST API [T] wired to the access point [accessPointId]. The [AccessPointRegistry]
 * is resolved from Koin (the fork registers `single { AccessPointRegistry(AppAccessPoints.points) }`).
 * Usage:
 * ```
 * val ForkNetworkModule = module {
 *     restApi("jsonplaceholder") { it.createJsonPlaceholderApi() }
 * }
 * ```
 */
inline fun <reified T : Any> Module.restApi(
    accessPointId: String,
    crossinline create: (Ktorfit) -> T,
) {
    single<T> {
        create(
            get<AccessPointRegistry>().ktorfitFor(
                accessPointId = accessPointId,
                // Optional: a fork that binds no provider gets the empty default, unchanged behaviour.
                headerProvider = getOrNull<DefaultHeaderProvider>() ?: DefaultHeaderProvider.None,
                runtimeHeaders = getOrNull<RuntimeHeaderStore>() ?: RuntimeHeaderStore(),
                // Optional: bound by a fork that wants a rejected session to end it (UnauthorizedHandler).
                onUnauthorized = getOrNull<UnauthorizedHandler>(),
                networkConfigs = getOrNull<NetworkConfigRepository>(),
                debugBuild = isDebugBuild(),
            ),
        )
    }
}

/**
 * Register a Supabase-backed API [T] wired to the Supabase access point [accessPointId] — the exact
 * twin of [restApi], so both transports are declared the same way and neither is second-class.
 *
 * The [SupabaseClientFactory] is resolved from Koin (`NetworkModule` binds it); it owns URL resolution
 * (from [AccessPoint.baseUrl]) and anon-key lookup, so a fork writes ONLY the facade [T] over
 * `client.postgrest` and declares the endpoint in `app-profile/app.yaml#network.access_points`.
 *
 * Unlike REST, supabase-kt has no interface-generation step: `T` is the fork's own hand-written facade
 * (a typed wrapper over Postgrest queries), not a generated stub. That is the one real asymmetry
 * between the two paths, and it is inherent to supabase-kt rather than to this DSL.
 *
 * Usage:
 * ```
 * val ForkNetworkModule = module {
 *     supabaseApi("project") { ServerInstanceApi(it) }
 * }
 * ```
 */
inline fun <reified T : Any> Module.supabaseApi(
    accessPointId: String,
    crossinline create: (SupabaseConfigClient) -> T,
) {
    single<T> { create(get<SupabaseClientFactory>().requireClientFor(accessPointId)) }
}
