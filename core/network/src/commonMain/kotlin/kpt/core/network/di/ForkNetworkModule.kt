/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.di

import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.SupabaseExtrasProvider
import org.koin.dsl.module

/**
 * THE FORK'S network DI seam. Empty on the neutral template — this is yours to fill.
 *
 * It lives outside any per-endpoint package on purpose, so `scripts/remove-demo.sh` leaves it
 * standing: a fork that runs the customizer (which strips the demo BY DEFAULT — "forking = starting
 * clean") keeps this file and can wire network DI immediately.
 *
 * There is no `DemoNetworkModule` any more. Endpoint code now lives in a package named for its
 * ACCESS POINT (`kpt.core.network.<id>.api` / `.dto` / `.config`), generated per declared endpoint,
 * so the demo's API types are deleted with their access points rather than with a `demo/` package.
 * What is left over is DI — and DI for a non-derivable single belongs here, demo-fenced.
 *
 * ## What does NOT go here
 * API client bindings. Every server is declared once in `app-profile/app.yaml#network.access_points`
 * and its Koin binding is GENERATED into [GeneratedApiBindings], which [NetworkModule] includes.
 * Adding a `restApi(...)`/`supabaseApi(...)` line here is refused by
 * `scripts/product-health/checks/network-access-points.sh` (NAP-7).
 *
 * ## What DOES go here
 * Network singles that cannot be derived from an endpoint declaration — request-time API-key configs,
 * interceptor/auth providers, custom serializers. Example:
 * ```
 * single<MyApiConfig> { MyApiConfig(apiKey = BuildKonfig.MY_API_KEY.takeIf { it.isNotBlank() }) }
 * ```
 *
 * ## Supabase modules beyond Postgrest — [SupabaseExtrasProvider]
 * [SupabaseConfigClient] installs Postgrest only, under the point's publishable key — enough for an
 * app with no sign-in. Auth / Realtime / Storage are opt-in. Bind the seam HERE — it is the one place
 * a fork can install them without either editing template code (deleted on the next sync) or
 * building a second `createSupabaseClient`:
 * ```
 * single<SupabaseExtrasProvider> {
 *     SupabaseExtrasProvider { id ->
 *         when (id) {
 *             // Social sign-in: kmp-supabase-auth (io.github.mobilebytelabs:cmp-supabase-auth[-compose])
 *             // installs Auth + native Google/Apple onto this client. Never hand-write install(Auth).
 *             // libs.cmp.supabase.auth.compose (0.3.3). secureStorage keeps the session + PKCE verifier
 *             // in the encrypted named("secure") Settings instead of supabase-kt's plain default.
 *             "<your-project-ref>" -> kmpSupabaseAuthInstall(authConfig) { secureStorage(get(named("secure"))) }
 *             // Several projects sharing a module — one branch, no repetition.
 *             "<project-b>", "<project-c>" -> {
 *                 { install(Realtime) }
 *             }
 *             else -> {
 *                 {}
 *             }
 *         }
 *     }
 * }
 * ```
 * A SECOND CLIENT IS THE BUG THIS PREVENTS: sign in on your own `createSupabaseClient` and the
 * generated `supabaseApi(...)` binding hands `@ApiBinding` types a DIFFERENT instance carrying no
 * session, so every RLS-gated call resolves no `auth.uid()` — compiling cleanly the whole way. The
 * binding is optional; bind nothing and you get Postgrest only.
 *
 * For sign-in, also include the library's client binding over the SAME client (mbs/cappy does exactly this):
 * ```
 * includes(kmpSupabaseAuthNetwork(authConfig) { get<SupabaseClientFactory>().requireClientFor("<ref>").client })
 * ```
 *
 * THIS FILE IS `owner: fork`. `/kmp-project-template-sync` preserves it — the template ships it empty
 * and never overwrites what you add. That is why the seam is bound here and not in the template-owned
 * [NetworkModule], which the sync full-copies.
 */
val ForkNetworkModule = module {
    // Intentionally empty on the template — a fork adds its own non-derivable network singles here.

    // ── Supabase modules beyond Postgrest ────────────────────────────────────────────────────────
    // BOUND, not merely documented. SupabaseClientFactory resolves this with `getOrNull`, so an
    // absent binding and a binding that returns `{}` behave identically — but a live binding puts the
    // extension point in CODE, where a fork editing this file will see it, instead of in a KDoc it
    // has to know to read. The template itself needs nothing beyond Postgrest, so every branch is
    // `{}` until a fork adds its own.
    //
    // `when`, not `if (id != X)`: the negated-if shape reads as "one project, everything else is a
    // no-op" and stops scaling the moment a second project appears. A `when` takes N branches and a
    // comma-separated branch for modules several projects share, with `else -> {}` as the explicit
    // "this point needs nothing extra".
    //
    // A SECOND CLIENT IS THE BUG THIS PREVENTS: install Auth by building your own
    // `createSupabaseClient` and the generated `supabaseApi(...)` binding hands `@ApiBinding` types a
    // DIFFERENT instance carrying no session, so every RLS-gated call resolves no `auth.uid()` —
    // compiling cleanly the whole way.
    single<SupabaseExtrasProvider> {
        SupabaseExtrasProvider { id ->
            when (id) {
                // "<your-project-ref>" -> kmpSupabaseAuthInstall(authConfig) { secureStorage(get(named("secure"))) }
                else -> {
                    {}
                }
            }
        }
    }
}
