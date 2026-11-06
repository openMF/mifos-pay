/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network.di

import de.jensklingenberg.ktorfit.Ktorfit
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.network.config.InstanceConfigManager
import kpt.core.network.utils.FlowConverterFactory
import kpt.core.network.utils.KtorInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.corebase.network.DynamicBaseUrlPlugin
import org.mifos.corebase.network.DynamicLoggableHosts
import org.mifos.corebase.network.MultiUrlConfigProvider
import org.mifos.corebase.network.httpClient
import org.mifos.corebase.network.setupDefaultHttpClient
import org.mifos.lib.loan.network.LoansService
import org.mifos.lib.loan.network.createLoansService
/**
 * Koin module providing the `libs/mifos-loans` module's own, self-contained Ktorfit/HttpClient
 * engine and [LoansService]. This mirrors the pattern used for the app's SelfClient in
 * `core/network`'s `NetworkModule`, but is owned entirely by this library so it can remain
 * independently publishable.
 */
val LoansClient = named("LoansClient")

val LoansNetworkModule = module {
    single<Ktorfit>(qualifier = LoansClient) {
        val preferencesRepository = get<ForkPreferencesRepository>()
        val configManager = get<InstanceConfigManager>()
        Ktorfit.Builder()
            .httpClient(
                client = httpClient(
                    config = setupDefaultHttpClient(
                        // Use placeholder - actual URL is set dynamically by DynamicBaseUrlPlugin
                        baseUrl = "https://placeholder.local/",
                        // Dynamic loggable hosts - reads current endpoints from configManager
                        loggableHosts = DynamicLoggableHosts(configManager),
                    ),
                ).config {
                    install(DynamicBaseUrlPlugin) {
                        multiConfigProvider = configManager
                        urlType = MultiUrlConfigProvider.UrlType.SELF_SERVICE
                    }
                    install(KtorInterceptor) {
                        getToken = { preferencesRepository.authToken }
                        this.configManager = configManager
                    }
                },
            )
            .converterFactories(FlowConverterFactory())
            .build()
    }

    single<LoansService> { get<Ktorfit>(LoansClient).createLoansService() }
}
