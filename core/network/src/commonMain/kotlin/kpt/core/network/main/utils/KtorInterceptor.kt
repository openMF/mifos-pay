/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kpt.core.common.GlobalAuthManager
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.network.main.config.InstanceConfigManager
/**
 * Ktor client plugin adding JSON, tenant and Basic auth headers and flagging 401 responses as unauthorized.
 */
class KtorInterceptor(
    private val getToken: () -> String?,
    private val configManager: InstanceConfigManager,
) {
    /** Ktor plugin installer for [KtorInterceptor]. */
    companion object Plugin : HttpClientPlugin<Config, KtorInterceptor> {

        override val key: AttributeKey<KtorInterceptor> = AttributeKey("KtorInterceptor")

        override fun install(plugin: KtorInterceptor, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.header(BaseURL.HEADER_CONTENT_TYPE, BaseURL.HEADER_CONTENT_TYPE_VALUE)
                context.header(BaseURL.HEADER_ACCEPT, BaseURL.HEADER_ACCEPT_VALUE)
                context.header(BaseURL.HEADER_TENANT, plugin.configManager.getPlatformTenantId())

                plugin.getToken()?.let { token ->
                    if (token.isNotEmpty()) {
                        context.headers[BaseURL.HEADER_AUTHORIZATION] = "Basic $token"
                    }
                }
            }

            scope.responsePipeline.intercept(HttpResponsePipeline.After) {
                if (context.response.status == HttpStatusCode.Unauthorized) {
                    GlobalAuthManager.markUnauthorized()
                }
                proceedWith(subject)
            }
        }

        override fun prepare(block: Config.() -> Unit): KtorInterceptor {
            val config = Config().apply(block)
            return KtorInterceptor(config.getToken, config.configManager)
        }
    }
}

/** Configuration for [KtorInterceptor]. */
class Config {
    /** Supplier of the current auth token, or null when signed out. */
    lateinit var getToken: () -> String?

    /** Source of the active server instance. */
    lateinit var configManager: InstanceConfigManager
}

/** Variant of [KtorInterceptor] that reads the auth token from the preferences repository. */
class KtorInterceptorRe(
    private val repository: ForkPreferencesRepository,
    private val configManager: InstanceConfigManager,
) {
    /** Ktor plugin installer for [KtorInterceptorRe]. */
    companion object Plugin : HttpClientPlugin<ConfigRe, KtorInterceptorRe> {

        override val key: AttributeKey<KtorInterceptorRe> = AttributeKey("KtorInterceptorRe")

        override fun install(plugin: KtorInterceptorRe, scope: HttpClient) {
            val token = plugin.repository.token.value

            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.header(BaseURL.HEADER_CONTENT_TYPE, BaseURL.HEADER_CONTENT_TYPE_VALUE)
                context.header(BaseURL.HEADER_ACCEPT, BaseURL.HEADER_ACCEPT_VALUE)
                context.header(BaseURL.HEADER_TENANT, plugin.configManager.getPlatformTenantId())

                token?.let { token ->
                    if (token.isNotEmpty()) {
                        context.headers[BaseURL.HEADER_AUTHORIZATION] = "Basic $token"
                    }
                }
            }

            scope.responsePipeline.intercept(HttpResponsePipeline.After) {
                if (context.response.status == HttpStatusCode.Unauthorized) {
                    GlobalAuthManager.markUnauthorized()
                }
                proceedWith(subject)
            }
        }

        override fun prepare(block: ConfigRe.() -> Unit): KtorInterceptorRe {
            val config = ConfigRe().apply(block)
            return KtorInterceptorRe(config.repository, config.configManager)
        }
    }
}

/** Configuration for [KtorInterceptorRe]. */
class ConfigRe {
    /** Preferences repository providing the stored auth token. */
    lateinit var repository: ForkPreferencesRepository

    /** Source of the active server instance. */
    lateinit var configManager: InstanceConfigManager
}
