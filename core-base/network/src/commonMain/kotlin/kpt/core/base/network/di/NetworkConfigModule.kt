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

import com.russhwolf.settings.Settings
import kpt.core.base.network.InMemoryNetworkConfigStore
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.base.network.SettingsNetworkConfigStore
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Runtime network configs, bound by core-base — an app includes this module and gets the whole feature:
 * every declared access point editable and switchable at runtime, persisted, with `restApi` and
 * `SupabaseClientFactory` following it. Needs the app's `AccessPointRegistry`.
 *
 * Persists in the core-base plain settings (`named("plain")`, from the datastore base module) when they
 * are bound; otherwise changes last one session.
 */
val NetworkConfigModule = module {
    single {
        NetworkConfigRepository(
            registry = get(),
            store = getOrNull<Settings>(named("plain"))?.let(::SettingsNetworkConfigStore)
                ?: InMemoryNetworkConfigStore(),
        )
    }
}
