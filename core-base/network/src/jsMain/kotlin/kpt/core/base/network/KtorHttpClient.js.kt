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
import kotlinx.browser.window

/**
 * Web: requests to a host matching [proxiedHosts] (exact or `*.` wildcard) go through the CORS relay
 * [corsProxyBaseUrl] — a same-origin path (`/cors`) is resolved against the page's origin, and degrades
 * to direct calls on a host that does not run the relay worker. See [WebCorsRelayPlugin].
 */
internal actual fun HttpClientConfig<*>.installProxyPlugin(
    proxiedHosts: List<String>,
    corsProxyBaseUrl: String,
) = installCorsRelay(
    hostPatterns = proxiedHosts,
    relayBase = resolveCorsRelayBase(corsProxyBaseUrl, window.location.origin),
    // Only the site's own relay can be detected as missing (a static host without the worker).
    fallbackToDirect = corsProxyBaseUrl.startsWith("/"),
)

/** `httpClient` on Web (JS). */
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js) {
    config(this)
}
