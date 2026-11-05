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
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLBuilder
import kotlinx.browser.window

/**
 * A relay given as a path (`/cors`) is served by the site itself — resolve it against the page's
 * origin. An absolute URL (`https://corsproxy.io`) is used as-is.
 */
internal fun resolveProxyBase(base: String): String = if (base.startsWith("/")) window.location.origin + base else base

private class WebApiProxyConfig {
    var proxyBaseUrl: String = "https://corsproxy.io"
    val hosts = mutableSetOf<String>()
}

/**
 * Generic CORS proxy plugin for browser targets.
 *
 * Requests to any host in [WebApiProxyConfig.hosts] are transparently rewritten through
 * [WebApiProxyConfig.proxyBaseUrl] — corsproxy.io format: `https://proxy/?<original-url>`.
 *
 * The target URL is embedded in `encodedPath` as `"/?$originalUrl"` so Ktor never adds a
 * trailing `=` (which would corrupt the last query parameter on the proxied API). The path
 * segments are used as-is (no re-encoding) and parameters are cleared, so the final URL
 * becomes `https://corsproxy.io/?https://api.target.com/path?param=value` exactly.
 *
 * Configured by [installProxyPlugin] via [setupDefaultHttpClient]'s `proxiedHosts` param.
 * The plugin is never installed on non-web platforms.
 */
private val WebApiProxyPlugin = createClientPlugin("WebApiProxy", ::WebApiProxyConfig) {
    // Capture config here — pluginConfig is not in scope inside onRequest's lambda.
    val proxyConfig = pluginConfig

    onRequest { request, _ ->
        if (request.url.host in proxyConfig.hosts) {
            val originalUrl = request.url.build().toString()
            val proxyMeta = URLBuilder(resolveProxyBase(proxyConfig.proxyBaseUrl))
            request.url.apply {
                protocol = proxyMeta.protocol
                host = proxyMeta.host
                port = proxyMeta.port
                // Embed the complete target URL after the relay's own path, as "/<path>/?{targetUrl}".
                // The empty first segment gives the leading /. Segments are used as-is (no
                // re-encoding); clearing parameters ensures Ktor appends no extra "?key=" to the URL.
                // Result: https://corsproxy.io/?https://api.target.com/path?param=value
                //     or  https://<this site>/cors/?https://api.target.com/path?param=value
                parameters.clear()
                encodedParameters.clear()
                val relayPath = proxyMeta.encodedPathSegments.filter { it.isNotEmpty() }
                encodedPathSegments = listOf("") + relayPath + "?$originalUrl"
            }
        }
    }
}

internal actual fun HttpClientConfig<*>.installProxyPlugin(
    proxiedHosts: List<String>,
    corsProxyBaseUrl: String,
) {
    if (proxiedHosts.isNotEmpty()) {
        install(WebApiProxyPlugin) {
            proxyBaseUrl = corsProxyBaseUrl
            hosts += proxiedHosts
        }
    }
}

/** `httpClient` on Web (WasmJS). */
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js) {
    config(this)
}
