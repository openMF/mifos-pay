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

/** The site's own relay path on web (cmp-web `_worker.js`); keep in step with its allowlist. */
internal const val RELAY_PATH = "/cors"

/** Domains whose hosts the web build sends through the relay; the worker allows the same ones. */
internal val RELAYED_DOMAINS = listOf("mifos.community")

/** True when [host] is one of [RELAYED_DOMAINS] or a subdomain of one. */
internal fun isRelayedHost(host: String): Boolean =
    RELAYED_DOMAINS.any { host == it || host.endsWith(".$it") }

/**
 * The relay for a page on a host that cannot run the relay worker (GitHub Pages): the Cloudflare
 * deploy's, whose worker allows that page's origin.
 */
internal const val HOSTED_RELAY_ORIGIN = "https://mifos-pay.pages.dev"

/** Cloudflare Pages hosts serve the relay worker themselves; any other host uses [HOSTED_RELAY_ORIGIN]. */
internal fun relayOriginFor(pageHost: String, pageOrigin: String): String =
    if (pageHost == "pages.dev" || pageHost.endsWith(".pages.dev")) pageOrigin else HOSTED_RELAY_ORIGIN
