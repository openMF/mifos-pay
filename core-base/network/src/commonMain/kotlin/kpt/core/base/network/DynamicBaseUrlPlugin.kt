/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

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
 *
 * ## Usage with DynamicUrlConfigProvider
 *
 * ```kotlin
 * val client = httpClient {
 *     install(DynamicBaseUrlPlugin) {
 *         configProvider = myConfigProvider
 *     }
 * }
 * ```
 *
 * ## Usage with MultiUrlConfigProvider
 *
 * The [UrlType] is open — the project names its endpoints in `core/` (see `AppUrlTypes`):
 *
 * ```kotlin
 * // one client per named endpoint
 * val server1Client = httpClient {
 *     install(DynamicBaseUrlPlugin) {
 *         multiConfigProvider = myMultiConfigProvider
 *         urlType = AppUrlTypes.SERVER1   // == UrlType("SERVER1")
 *     }
 * }
 *
 * val stagingClient = httpClient {
 *     install(DynamicBaseUrlPlugin) {
 *         multiConfigProvider = myMultiConfigProvider
 *         urlType = AppUrlTypes.STAGING
 *     }
 * }
 * ```
 */
class DynamicBaseUrlPlugin private constructor(
    private val configProvider: DynamicUrlConfigProvider?,
    private val multiConfigProvider: MultiUrlConfigProvider?,
    private val urlType: UrlType,
    private val staticBasePath: String,
) {
    /**
     * Ktor plugin installer — rewrites each request's base URL from the resolved access point, so an endpoint change
     * needs no per-call wiring.
     */
    companion object Plugin : HttpClientPlugin<DynamicBaseUrlConfig, DynamicBaseUrlPlugin> {
        override val key: AttributeKey<DynamicBaseUrlPlugin> =
            AttributeKey("DynamicBaseUrlPlugin")

        override fun prepare(block: DynamicBaseUrlConfig.() -> Unit): DynamicBaseUrlPlugin {
            val config = DynamicBaseUrlConfig().apply(block)
            return DynamicBaseUrlPlugin(
                configProvider = config.configProvider,
                multiConfigProvider = config.multiConfigProvider,
                urlType = config.urlType,
                staticBasePath = config.staticBaseUrl?.let { Url(it).encodedPath.trim('/') }.orEmpty(),
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
                    else -> return@intercept // No provider configured, skip
                }

                val originalUrl = context.url.build()
                val newUrl = rebuildUrl(originalUrl, currentBaseUrl, plugin.staticBasePath)

                context.url.takeFrom(newUrl)
            }
        }

        /**
         * Rebuilds the request URL using the dynamic base URL while preserving
         * the original path and query parameters.
         */
        private fun rebuildUrl(originalUrl: Url, baseUrl: String, staticBasePath: String): Url {
            val baseUrlParsed = Url(baseUrl)

            return URLBuilder().apply {
                protocol = baseUrlParsed.protocol
                host = baseUrlParsed.host
                port = baseUrlParsed.port

                // Combine base path with original path
                val basePath = baseUrlParsed.encodedPath.trimEnd('/')
                // The request was resolved against the client's STATIC base, so its path begins with
                // that base's path. Keep only what follows it — otherwise the dynamic path is stacked
                // ON the static one (`/fineract-provider/api/v1/1.0/field/v1/clients`).
                val originalPath = originalUrl.encodedPath.trimStart('/').let { path ->
                    when {
                        staticBasePath.isEmpty() -> path
                        path == staticBasePath -> ""
                        path.startsWith("$staticBasePath/") -> path.removePrefix("$staticBasePath/")
                        else -> path
                    }
                }

                encodedPath = if (originalPath.isNotEmpty()) {
                    "$basePath/$originalPath"
                } else {
                    basePath
                }

                // Preserve query parameters
                originalUrl.parameters.forEach { name, values ->
                    parameters.appendAll(name, values)
                }

                // Preserve fragment
                fragment = originalUrl.fragment
            }.build()
        }
    }
}

/**
 * Configuration class for [DynamicBaseUrlPlugin].
 */
class DynamicBaseUrlConfig {
    /**
     * The client's static base URL — the one requests are resolved against before this plugin runs.
     * Its PATH is stripped from each request before the dynamic base is applied, so a server with an
     * API path replaces the static one instead of being prefixed to it. `setupDefaultHttpClient`
     * sets it; leave it null only for a client whose requests carry no base path.
     */
    var staticBaseUrl: String? = null

    /**
     * Simple config provider for single URL type applications.
     */
    var configProvider: DynamicUrlConfigProvider? = null

    /**
     * Multi-URL config provider for applications with multiple API endpoints.
     * Takes precedence over [configProvider] if both are set.
     */
    var multiConfigProvider: MultiUrlConfigProvider? = null

    /**
     * The URL type to use when [multiConfigProvider] is set.
     * Defaults to [UrlType.MAIN].
     */
    var urlType: UrlType = UrlType.MAIN
}

/**
 * A dynamic list implementation that provides loggable hosts from a [DynamicUrlConfigProvider].
 *
 * This list is evaluated each time it's iterated, so it reflects the currently
 * configured loggable hosts. This is useful for Ktor's Logging plugin filter.
 *
 * ## Usage
 *
 * ```kotlin
 * val client = httpClient(
 *     setupDefaultHttpClient(
 *         baseUrl = "https://placeholder.local/",
 *         loggableHosts = DynamicLoggableHosts(myConfigProvider),
 *     )
 * ) {
 *     install(DynamicBaseUrlPlugin) {
 *         configProvider = myConfigProvider
 *     }
 * }
 * ```
 */
class DynamicLoggableHosts(
    private val configProvider: DynamicUrlConfigProvider,
) : AbstractList<String>() {

    override val size: Int
        get() = configProvider.getLoggableHosts().size

    override fun get(index: Int): String =
        configProvider.getLoggableHosts()[index]
}
