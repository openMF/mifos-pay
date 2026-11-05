/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network.di

import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.base.network.SupabaseClientFactory
import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.SupabaseExtrasProvider
import org.koin.dsl.module

/**
 * Framework network wiring — the half of the network graph a fork never configures.
 *
 * ## Why these bindings are here and not in `core/network`
 * A binding belongs in `core-base` when it touches nothing the fork generates. These two qualify:
 * `RuntimeHeaderStore` takes no arguments at all, and the client map is derived entirely through
 * `get()`. Everything still in `core/network`'s `NetworkModule` fails that test — `AccessPointRegistry`
 * and `MultiUrlConfigProvider` read an `App*` object generated into `core/network` from `app-profile`,
 * and `core-base` sits below that and cannot see it. Supabase passes it: each point's publishable key is
 * ON its generated [kpt.core.base.network.AccessPoint], so the factory needs only the registry.
 *
 * ## Why relocating is safe without a graph test
 * `NetworkModule` includes this module unconditionally, and `KoinModules` includes `NetworkModule`
 * unconditionally. Moving a definition between two always-included modules yields the SAME container
 * with the SAME definitions — there is no ordering or visibility question to get wrong. (Koin's own
 * `verifyAll` cannot check this graph: it matches constructor parameters by type and this codebase
 * binds heavily by QUALIFIER, so it reports real bindings like `AlertsRepository` as missing.)
 *
 * ## What a fork does instead
 * Nothing here. Fork-owned network singles go in `core/network`'s `ForkNetworkModule`, which sync
 * preserves. This file is template-owned and blind-copied.
 */
val NetworkBaseModule = module {
    // No arguments, no fork input — there was never a reason for this to sit a layer up.
    single { RuntimeHeaderStore() }

    // Per-point Supabase clients, built from the registry alone: each generated AccessPoint carries its
    // publishable key, so no fork wiring reaches this.
    single {
        SupabaseClientFactory(
            registry = get(),
            // The fork seam for Auth (kmp-supabase-auth) / Realtime / Storage. OPTIONAL: a fork binding
            // nothing gets the no-op. If this lambda were not passed the default would win and a fork's
            // SupabaseExtrasProvider would silently never run.
            installExtrasFor = { id -> getOrNull<SupabaseExtrasProvider>()?.forId(id) ?: {} },
            networkConfigs = getOrNull(),
        )
    }

    single<Map<String, SupabaseConfigClient>> { get<SupabaseClientFactory>().clients() }
}
