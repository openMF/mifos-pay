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

/** Transport kind of a declared network access point. */
enum class AccessPointKind {
    /** A REST endpoint reached over Ktor/Ktorfit. */
    REST,

    /** A Supabase project reached over supabase-kt (Postgrest by default). */
    SUPABASE,
}

/**
 * One declared network access point — a named endpoint the app talks to.
 *
 * INFRA (owner: template) — the STRUCTURE is framework-owned; the per-fork VALUES are generated into
 * `kpt.core.network.config.AppAccessPoints.points` by `syncForkConfig` from
 * `app-profile/app.yaml#network.access_points`.
 *
 * @param id stable string id a fork references from `restApi<T>(id)`.
 * @param kind [AccessPointKind.REST] (Ktor/Ktorfit) or [AccessPointKind.SUPABASE] (supabase-kt).
 * @param baseUrl the resolved base URL (REST) or the Supabase project URL.
 * @param loggableHost the host enabled for HTTP logging on this access point.
 * @param proxiedHost host routed through the platform proxy (Ktor's DynamicBaseUrlPlugin); null = none.
 * @param type [UrlType] key for dynamic base-URL switching; defaults to the id upper-cased.
 */
data class AccessPoint(
    val id: String,
    val kind: AccessPointKind,
    /**
     * ORIGIN only — scheme + host, ending at the first `/` (`https://sandbox.mifos.community/`).
     *
     * Split from [basePath] because they change for different reasons and at different times. The
     * origin is what a fork swaps (sandbox -> production, or a per-flavour host) and what
     * [DynamicBaseUrlPlugin] rewrites per [UrlType] at runtime; the path is a property of the API
     * itself and travels with the endpoint. Keeping them in one string meant a fork could not
     * repoint the host without also copying the vendor's path — and a runtime host swap silently
     * dropped it.
     */
    val baseUrl: String,
    /**
     * API path under [baseUrl] (`fineract-provider/api/v1/`). Empty when the API lives at the root.
     *
     * Joined to the origin exactly once, with a single `/` between, so neither side has to be
     * careful about trailing slashes.
     */
    val basePath: String = "",
    val loggableHost: String,
    val proxiedHost: String? = null,
    val type: UrlType = UrlType(id.uppercase()),
    /**
     * Headers sent on every request to this endpoint, declared in
     * `app-profile/app.yaml#network.access_points[].headers[]`. Static values are baked in; a
     * `runtimeKey` header takes its value from [RuntimeHeaderStore] at request time (login, OAuth).
     */
    val headers: List<HeaderSpec> = emptyList(),
    /**
     * How this endpoint authenticates. Declaring it makes the `Authorization` header automatic —
     * the generator emits its spec and the auth bridge fills the value in the right wire format, so
     * no call site formats a credential by hand.
     */
    val auth: AuthScheme = AuthScheme.NONE,
    /**
     * `main: true` in app-profile — at most one point. Clients bound to it follow whichever network
     * config is MAIN at runtime ([NetworkConfigRepository.setMain]); every other point's clients follow
     * their own config.
     */
    val isMain: Boolean = false,
    /**
     * Supabase client config for a `type: supabase` point — its publishable key. `null` on REST points.
     * [SupabaseClientFactory] reads it directly, so a fork never wires a key by hand.
     */
    val supabase: SupabaseSpec? = null,
) {
    /**
     * The URL requests are actually made against — [baseUrl] + [basePath], normalised.
     *
     * Ktor resolves a relative request path against this, so the trailing slash matters: without it
     * the last segment is replaced rather than appended, and `offices` would hit
     * `…/api/offices` instead of `…/api/v1/offices`.
     */
    val effectiveUrl: String
        get() = when {
            basePath.isBlank() -> baseUrl
            else -> baseUrl.trimEnd('/') + "/" + basePath.trim('/') + "/"
        }
}

/**
 * Template registry MECHANISM over a fork-provided list of [points].
 *
 * The fork supplies its generated points (`AppAccessPoints.points`) and registers
 * `single { AccessPointRegistry(AppAccessPoints.points) }` in its `NetworkModule`; `core-base/network`
 * owns the resolution logic + the [restApi]/[ktorfitFor] DSL — so a fork writes only API interfaces plus
 * one `restApi("<id>")` line each, and there is exactly one source of truth for "which servers this app
 * talks to" (`app-profile/app.yaml#network.access_points`).
 *
 * @property points every access point this fork declares, from `app-profile#network.access_points`. Order is
 *   irrelevant — lookup is by id.
 * @property policy app-wide network privacy (logging detail per build type, query redaction, web CORS
 *   relay), from `app-profile/app.yaml#network` via `AppAccessPoints.policy`. Defaults are the secure ones.
 */
class AccessPointRegistry(
    val points: List<AccessPoint>,
    val policy: NetworkPolicy = NetworkPolicy(),
) {

    /**
     * One [HttpClient] per ACCESS POINT, not per API.
     *
     * [ktorfitFor] used to build a fresh client on every call, and `restApi(...)` registers one Koin
     * `single` per API TYPE. For a server exposing many resource APIs that multiplies: each API gets
     * its own connection pool and TLS session cache, nothing is reused, and every API pays a full
     * TCP + TLS handshake on first use.
     *
     * The network KSP processor already assumed the monolith case — its own comment reads "Fineract
     * is one server, one base_url, one credential — and 21 resource-oriented APIs over it." The
     * generator was taught that shape; this factory was not.
     *
     * Two forks hit it independently:
     *  - `mifos-x-field-officer-app` MIGRATED onto `@ApiBinding`: 22 interfaces on one host, 23
     *    `restApi` lines, 23 HttpClients. On device (2026-10-20, live Fineract) an authenticated
     *    read never settled — minutes of "Refreshing…" with no error and no retry.
     *  - `mifos-pay` never migrated, and sidestepped it by hand in its legacy stack: three
     *    `single<KtorfitClient>` keyed by ROLE, ~24 services from one Ktorfit each. Its template path
     *    carries 0 `@ApiBinding` APIs, so the defect is latent there rather than absent.
     *
     * Keying on the access-point id alone is correct, not merely cheaper: `dynamicHeaders` is a
     * lambda resolved on EVERY request, so a credential written at login reaches a client built
     * before it, and `defaultHeaders` are per access point by construction. Distinct access points
     * keep distinct clients, so a per-endpoint credential never reaches a host that did not need it.
     *
     * Not separately locked. Koin serializes `single` creation, so the per-API registrations cannot
     * race here; and if they ever did, the loser's client is discarded unused rather than observed —
     * a wasteful outcome, not an incorrect one.
     */
    private val restClients: MutableMap<String, HttpClient> = mutableMapOf()

    /** The shared REST client for [accessPointId], built by [build] on first use. */
    internal fun sharedRestClient(accessPointId: String, build: () -> HttpClient): HttpClient =
        restClients.getOrPut(accessPointId, build)

    /** Resolve the access point declared under [id], or `null`. */
    fun byId(id: String): AccessPoint? = points.firstOrNull { it.id == id }

    /** Resolve the REST base URL registered for [type], or `null` if none is declared. */
    fun restBaseUrl(type: UrlType): String? =
        points.firstOrNull { it.type == type && it.kind == AccessPointKind.REST }?.effectiveUrl

    /** Every declared Supabase access point, in registry order. Empty if none declared. */
    fun supabasePoints(): List<AccessPoint> =
        points.filter { it.kind == AccessPointKind.SUPABASE }

    /** The first Supabase access point, or `null` if none is declared. Prefer [supabasePoints]. */
    fun supabasePoint(): AccessPoint? = supabasePoints().firstOrNull()

    /** Every declared host enabled for HTTP logging, across all access points. */
    fun loggableHosts(): List<String> = points.map { it.loggableHost }
}
