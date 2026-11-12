/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.di

import de.jensklingenberg.ktorfit.converter.Converter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kpt.core.base.network.UnauthorizedHandler
import kpt.core.common.GlobalAuthManager
import kpt.core.network.main.config.InstanceConfigManager
import kpt.core.network.main.config.InstanceNetworkConfigBridge
import kpt.core.network.main.utils.FlowConverterFactory
import org.koin.dsl.module

/**
 * The wallet's network wiring that is not an API: how the selected server reaches the access points.
 * The server list itself comes from the template's Supabase point (ServerInstanceApi) through the
 * `serverInstances` store.
 *
 * The Fineract services themselves bind through `@ApiBinding` (GeneratedApiBindings) on the `main`,
 * `selfservice` and `interbank` access points declared in app-profile.
 */
val NetworkModule = module {
    single {
        InstanceConfigManager(userPreferencesRepository = get())
    }

    // The selected server -> the access points' URLs and tenant, before any screen fetches.
    single(createdAtStart = true) {
        InstanceNetworkConfigBridge(
            instances = get(),
            networkConfigs = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ).also { it.start() }
    }

    // The Fineract services return Flow<T>; every REST access point gets this converter.
    single<Converter.Factory> { FlowConverterFactory() }

    // A 401 on a signed-in request ends the session (the app shows its re-login dialog).
    single<UnauthorizedHandler> { UnauthorizedHandler { GlobalAuthManager.markUnauthorized() } }
}
