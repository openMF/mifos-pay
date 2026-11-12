/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.di

import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.DefaultHeaderProvider
import kpt.core.base.network.MultiUrlConfigProvider
import kpt.core.base.network.di.NetworkBaseModule
import kpt.core.base.network.di.NetworkConfigModule
import kpt.core.network.config.AppAccessPoints
import kpt.core.network.config.AppMultiUrlConfigProvider
import kpt.core.network.config.KptNetworkHeaders
import org.koin.dsl.module

// NOTE: Backend base URLs are NOT hardcoded here or in config classes anymore — every server is a
// declared access point in app-profile/app.yaml#network.access_points (→ AccessPointRegistry via
// syncForkConfig). Every API is wired in the GENERATED [GeneratedApiBindings] via
// `restApi("<id>") { … }`, which auto-builds each transport from its access point.
// Fork-customisation = edit app.yaml + syncForkConfig.
// FRED's API key remains a per-request @Query param (a vault secret: mifos-x-fred-api-key → BuildKonfig).
// Dynamic server config (consumer-facing, from core-base/network):
//   - SupabaseConfigClient(s) are registered below so forks can fetch runtime server config from a
//     Supabase `app_server_config`-style table. The AccessPointRegistry is the single URL SoT: SupabaseClientFactory
//     materializes one client per declared SUPABASE access point (URL from AccessPoint.baseUrl). Each point's
//     publishable key (`publishable_key_env:` → BuildKonfig) is generated onto its AccessPoint; absent a
//     key the client stays inert (isConfigured == false). Forks edit app-profile only.
//     The legacy single-project path (SupabaseConfigConventionPlugin generating a SupabaseCredentials object
//     from a secrets file, plus an unnamed single<SupabaseConfigClient>) was REMOVED on 2026-09-06: nothing
//     referenced the generated object, and the registry had already become the single URL SoT.
//   - DynamicBaseUrlPlugin is an opt-in of setupDefaultHttpClient(...): a fork implements
//     DynamicUrlConfigProvider (reads its selected server, keyed by AppUrlTypes) and passes it as
//     `dynamicUrlProvider = get()` on any client that should switch base URL at runtime. The
//     toolkit's own fixed-URL APIs (FRED / World Bank / CoinGecko / Frankfurter) don't use it.
// INFRA-ONLY, owner: template (E1 / C2). API types live in per-access-point packages
// (`kpt.core.network.<id>.api`), their bindings are GENERATED into [GeneratedApiBindings], and any
// non-derivable single goes in the fork-owned [ForkNetworkModule]. So this aggregator carries no
// endpoint-specific reference at all and a template sync can blind-copy it.
/**
 * Koin module for `core/network` — the Ktorfit API bindings over the shared client.
 *
 * Bindings are GENERATED from `app-profile/app.yaml#network.access_points` by `syncForkConfig`;
 * declare an endpoint there and write its API interface, and the wiring follows.
 */
val NetworkModule = module {
    // Framework half — bindings that touch nothing app-profile generates.
    includes(NetworkBaseModule)

    // Runtime header values — written at login (Basic / OAuth), read on EVERY request. A singleton,
    // because the whole point is that a value set after the clients were built still reaches them.

    // Default request headers, from the fork-owned KptNetworkHeaders seam. Every REST client
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
    single<DefaultHeaderProvider> { KptNetworkHeaders }

    // Every declared endpoint's Koin binding, GENERATED from app-profile#network.access_points into
    // the sibling [GeneratedApiBindings] (same package — no import, so this file keeps its zero-demo
    // -imports property and stays blind-copyable on a template sync). It lives HERE rather than in the
    // demo aggregator because it is derived from the FORK's SoT: `remove-demo.sh` deletes every
    // `**/demo/**` package, which used to take the generated file with it and turn every later
    // syncForkConfig into a silent no-op for a cleaned fork.
    includes(GeneratedApiBindings)
    // Runtime network configs (core-base): every access point editable/switchable at runtime, persisted.
    includes(NetworkConfigModule)

    // The fork's generated access points, wrapped by the framework registry mechanism (core-base/network).
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    // + the app-wide network privacy policy (logging per build type, query redaction, web CORS relay).
    single { AccessPointRegistry(AppAccessPoints.points, AppAccessPoints.policy) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Supabase clients are bound in core-base NetworkBaseModule (included above): each point's publishable
    // key is on its generated AccessPoint, so they need nothing from here.
    // Map<id, client> — every declared Supabase access point materializes exactly one client.
    // This is the ONLY Supabase client surface. There is deliberately no unnamed
    // `single<SupabaseConfigClient>`: "the" Supabase client is not a meaningful concept once a fork
    // can declare N projects, and the per-id `supabaseApi("<id>")` binding is how consumers reach one.
}
