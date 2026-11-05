/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-pay/blob/master/LICENSE.md
 */
package org.mifos.corebase.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.util.AttributeKey

/**
 * Ktor plugin that dynamically sets the base URL for each request based on
 * a [DynamicUrlConfigProvider] or [MultiUrlConfigProvider].
 *
 * This allows the app to switch between different server instances at runtime
 * without recreating the HTTP client.
 */
class DynamicBaseUrlPlugin private constructor(
    private val configProvider: DynamicUrlConfigProvider?,
    private val multiConfigProvider: MultiUrlConfigProvider?,
    private val urlType: MultiUrlConfigProvider.UrlType,
) {
    companion object Plugin : HttpClientPlugin<DynamicBaseUrlConfig, DynamicBaseUrlPlugin> {
        override val key: AttributeKey<DynamicBaseUrlPlugin> =
            AttributeKey("DynamicBaseUrlPlugin")

        override fun prepare(block: DynamicBaseUrlConfig.() -> Unit): DynamicBaseUrlPlugin {
            val config = DynamicBaseUrlConfig().apply(block)
            return DynamicBaseUrlPlugin(
                configProvider = config.configProvider,
                multiConfigProvider = config.multiConfigProvider,
                urlType = config.urlType,
            )
        }

        override fun install(plugin: DynamicBaseUrlPlugin, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                val currentBaseUrl = when {
                    plugin.multiConfigProvider != null -> {
                        plugin.multiConfigProvider.getBaseUrl(plugin.urlType)
                    }
                    plugin.configProvider != null -> {
                        plugin.configProvider.getBaseUrl()
                    }
                    else -> return@intercept
                }

                val originalUrl = context.url.build()
                val newUrl = rebuildUrl(originalUrl, currentBaseUrl)

                context.url.takeFrom(newUrl)
            }
        }

        private fun rebuildUrl(originalUrl: Url, baseUrl: String): Url {
            val baseUrlParsed = Url(baseUrl)

            return URLBuilder().apply {
                protocol = baseUrlParsed.protocol
                host = baseUrlParsed.host
                port = baseUrlParsed.port

                val basePath = baseUrlParsed.encodedPath.trimEnd('/')
                val originalPath = originalUrl.encodedPath.trimStart('/')

                encodedPath = if (originalPath.isNotEmpty()) {
                    "$basePath/$originalPath"
                } else {
                    basePath
                }

                originalUrl.parameters.forEach { name, values ->
                    parameters.appendAll(name, values)
                }

                fragment = originalUrl.fragment
            }.build()
        }
    }
}

/**
 * Configuration class for [DynamicBaseUrlPlugin].
 */
class DynamicBaseUrlConfig {
    var configProvider: DynamicUrlConfigProvider? = null
    var multiConfigProvider: MultiUrlConfigProvider? = null
    var urlType: MultiUrlConfigProvider.UrlType = MultiUrlConfigProvider.UrlType.MAIN
}

/**
 * A dynamic list that provides loggable hosts from a [DynamicUrlConfigProvider].
 */
class DynamicLoggableHosts(
    private val configProvider: DynamicUrlConfigProvider,
) : AbstractList<String>() {

    override val size: Int
        get() = configProvider.getLoggableHosts().size

    override fun get(index: Int): String =
        configProvider.getLoggableHosts()[index]
}
