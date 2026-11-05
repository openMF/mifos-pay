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

import kpt.core.network.BuildKonfig
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
 */
val ForkNetworkModule = module {
    // Intentionally empty on the template — a fork adds its own non-derivable network singles here.
    // demo:begin — FRED's key is a request-time @Query param, not client setup, so it cannot be
    // derived from the access-point declaration the way a base URL or an anon key can. Fenced, so
    // `remove-demo.sh` drops it together with the `fred` package and its access point.
    single<kpt.core.network.fred.config.FredApiConfig> {
        kpt.core.network.fred.config.FredApiConfig(
            apiKey = BuildKonfig.FRED_API_KEY.takeIf { it.isNotBlank() },
        )
    }
    // demo:end
}
