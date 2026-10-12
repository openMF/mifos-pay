/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.core.network.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import org.mifospay.core.common.GlobalAuthManager
import org.mifospay.core.datastore.UserPreferencesRepository
import org.mifospay.core.network.config.InstanceConfigManager

class KtorInterceptor(
    private val getToken: () -> String?,
    private val configManager: InstanceConfigManager,
) {
    companion object Plugin : HttpClientPlugin<Config, KtorInterceptor> {

        override val key: AttributeKey<KtorInterceptor> = AttributeKey("KtorInterceptor")

        override fun install(plugin: KtorInterceptor, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.header(BaseURL.HEADER_CONTENT_TYPE, BaseURL.HEADER_CONTENT_TYPE_VALUE)
                context.header(BaseURL.HEADER_ACCEPT, BaseURL.HEADER_ACCEPT_VALUE)
                context.header(BaseURL.HEADER_TENANT, plugin.configManager.getPlatformTenantId())

                plugin.getToken()?.let { token ->
                    val sanitizedToken = sanitizeHeaderValue(token)
                    if (sanitizedToken.isNotEmpty()) {
                        context.headers[BaseURL.HEADER_AUTHORIZATION] = "Basic $sanitizedToken"
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

class Config {
    lateinit var getToken: () -> String?
    lateinit var configManager: InstanceConfigManager
}

class KtorInterceptorRe(
    private val repository: UserPreferencesRepository,
    private val configManager: InstanceConfigManager,
) {
    companion object Plugin : HttpClientPlugin<ConfigRe, KtorInterceptorRe> {

        override val key: AttributeKey<KtorInterceptorRe> = AttributeKey("KtorInterceptorRe")

        override fun install(plugin: KtorInterceptorRe, scope: HttpClient) {
            val token = plugin.repository.token.value

            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.header(BaseURL.HEADER_CONTENT_TYPE, BaseURL.HEADER_CONTENT_TYPE_VALUE)
                context.header(BaseURL.HEADER_ACCEPT, BaseURL.HEADER_ACCEPT_VALUE)
                context.header(BaseURL.HEADER_TENANT, plugin.configManager.getPlatformTenantId())

                token?.let { token ->
                    val sanitizedToken = sanitizeHeaderValue(token)
                    if (sanitizedToken.isNotEmpty()) {
                        context.headers[BaseURL.HEADER_AUTHORIZATION] = "Basic $sanitizedToken"
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

class ConfigRe {
    lateinit var repository: UserPreferencesRepository
    lateinit var configManager: InstanceConfigManager
}

private fun sanitizeHeaderValue(value: String): String {
    return value
        .trim()
        .replace("\r", "")
        .replace("\n", "")
}
