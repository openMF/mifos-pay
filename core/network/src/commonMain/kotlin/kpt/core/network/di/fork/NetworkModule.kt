/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.di.fork

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.jan.supabase.logging.LogLevel
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import kpt.core.common.MifosDispatchers
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.network.FineractApiManager
import kpt.core.network.InterBankApiManager
import kpt.core.network.KtorfitClient
import kpt.core.network.SelfServiceApiManager
import kpt.core.network.SupabaseApiManager
import kpt.core.network.config.InstanceConfigLoader
import kpt.core.network.config.InstanceConfigManager
import kpt.core.network.config.SupabaseCredentialsImpl
import kpt.core.network.config.SupabaseInstanceConfigLoader
import kpt.core.network.di.BaseClient
import kpt.core.network.di.InterBankClient
import kpt.core.network.di.SelfClient
import kpt.core.network.main.utils.BaseURL
import kpt.core.network.main.utils.FlowConverterFactory
import kpt.core.network.main.utils.KtorInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.corebase.network.DynamicBaseUrlPlugin
import org.mifos.corebase.network.DynamicLoggableHosts
import org.mifos.corebase.network.MultiUrlConfigProvider
import org.mifos.corebase.network.SupabaseConfigClient
import org.mifos.corebase.network.httpClient
import org.mifos.corebase.network.setupDefaultHttpClient
import kotlin.io.encoding.ExperimentalEncodingApi

private val ioDispatcher = named(MifosDispatchers.IO.name)

// On JS / Wasm Koin keys a binding by the type's simple name; the template's
// kpt.core.base.network.SupabaseConfigClient is bound too, so this one needs its own key.
private val supabaseConfigClient = named("org.mifos.corebase.network.SupabaseConfigClient")

/** Koin module providing the HTTP clients, Ktorfit instances, API managers and service wiring of the network layer. */
@OptIn(ExperimentalEncodingApi::class)
val NetworkModule = module {
    single(supabaseConfigClient) {
        SupabaseConfigClient(
            credentials = SupabaseCredentialsImpl,
            logLevel = LogLevel.DEBUG,
        )
    }

    single {
        SupabaseApiManager(supabaseClient = get(supabaseConfigClient))
    }

    single<InstanceConfigLoader> {
        SupabaseInstanceConfigLoader(
            supabaseApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            json = get(),
        )
    }

    single {
        InstanceConfigManager(userPreferencesRepository = get())
    }

    single {
        BaseURL(configManager = get())
    }

    single<KtorfitClient>(qualifier = SelfClient) {
        val preferencesRepository = get<ForkPreferencesRepository>()
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
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
                        // Fail-fast connect/request timeouts. The corebase default sets
                        // request+socket to 60s but no connectTimeout; a shorter connect
                        // bound + request timeout surfaces a stalled call as an Error/Retry
                        // the offline-first layer handles, instead of a long spinner.
                        install(HttpTimeout) {
                            requestTimeoutMillis = 30_000
                            connectTimeoutMillis = 20_000
                            socketTimeoutMillis = 30_000
                        }
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
                .build(),
        )
    }

    single<KtorfitClient>(qualifier = BaseClient) {
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        config = setupDefaultHttpClient(
                            // Use placeholder - actual URL is set dynamically by DynamicBaseUrlPlugin
                            baseUrl = "https://placeholder.local/",
                            basicCredentialsProvider = {
                                BasicAuthCredentials(
                                    username = "mifos",
                                    password = "password",
                                )
                            },
                            // Headers are set dynamically by KtorInterceptor
                            defaultHeaders = mapOf(
                                BaseURL.HEADER_CONTENT_TYPE to BaseURL.HEADER_CONTENT_TYPE_VALUE,
                                BaseURL.HEADER_ACCEPT to BaseURL.HEADER_ACCEPT_VALUE,
                            ),
                            // Dynamic loggable hosts - reads current endpoints from configManager
                            loggableHosts = DynamicLoggableHosts(configManager),
                        ),
                    ).config {
                        install(DynamicBaseUrlPlugin) {
                            multiConfigProvider = configManager
                            urlType = MultiUrlConfigProvider.UrlType.MAIN
                        }
                        install(KtorInterceptor) {
                            getToken = { null }
                            this.configManager = configManager
                        }
                    },
                )
                .converterFactories(
                    FlowConverterFactory(),
                )
                .build(),
        )
    }

    single<KtorfitClient>(qualifier = InterBankClient) {
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        config = setupDefaultHttpClient(
                            // Use placeholder - actual URL is set dynamically by DynamicBaseUrlPlugin
                            baseUrl = "https://placeholder.local/",
                            // Headers are set dynamically by KtorInterceptor
                            defaultHeaders = mapOf(
                                BaseURL.HEADER_CONTENT_TYPE to BaseURL.HEADER_CONTENT_TYPE_VALUE,
                                BaseURL.HEADER_ACCEPT to BaseURL.HEADER_ACCEPT_VALUE,
                            ),
                            // Dynamic loggable hosts - reads current endpoints from configManager
                            loggableHosts = DynamicLoggableHosts(configManager),
                        ),
                    ).config {
                        install(DynamicBaseUrlPlugin) {
                            multiConfigProvider = configManager
                            urlType = MultiUrlConfigProvider.UrlType.INTERBANK
                        }
                        install(KtorInterceptor) {
                            getToken = { null }
                            this.configManager = configManager
                        }
                    },
                )
                .converterFactories(FlowConverterFactory())
                .build(),
        )
    }

    single {
        FineractApiManager(ktorfitClient = get(BaseClient))
    }

    single {
        SelfServiceApiManager(ktorfitClient = get(SelfClient))
    }

    single {
        InterBankApiManager(ktorfitClient = get(InterBankClient))
    }
}
