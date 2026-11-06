/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-pay/blob/master/LICENSE.md
 */
package org.mifos.corebase.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLBuilder

private fun pageOrigin(): String = js("window.location.origin") as String

private fun pageHostname(): String = js("window.location.hostname") as String

/**
 * Routes calls to the Mifos servers through this site's own relay (`/cors`, cmp-web `_worker.js`).
 * Fineract rejects any browser Origin on real requests, so a browser cannot call it directly; the
 * relay forwards the call server-side without an Origin. Local dev servers have no relay, so
 * localhost calls the servers directly.
 */
internal val WebRelayPlugin = createClientPlugin("WebRelay") {
    onRequest { request, _ ->
        val page = pageHostname()
        if (page == "localhost" || page == "127.0.0.1") return@onRequest
        if (!isRelayedHost(request.url.host)) return@onRequest
        val target = request.url.buildString()
        val relay = URLBuilder(relayOriginFor(page, pageOrigin()) + RELAY_PATH)
        request.url.apply {
            protocol = relay.protocol
            host = relay.host
            port = relay.port
            parameters.clear()
            encodedParameters.clear()
            // "/cors/?https://host/path?query" — the relay reads everything after the first "?".
            encodedPathSegments = listOf("") + relay.encodedPathSegments.filter { it.isNotEmpty() } + "?$target"
        }
    }
}
