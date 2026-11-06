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
