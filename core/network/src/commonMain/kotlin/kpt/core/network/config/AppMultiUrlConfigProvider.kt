/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.MultiUrlConfigProvider
import kpt.core.base.network.UrlType

/**
 * Concrete [MultiUrlConfigProvider] backed by the declarative [AccessPointRegistry].
 *
 * A fork declares its N REST endpoints ONCE in [AccessPointRegistry]; every client then threads the
 * right base URL by [UrlType] (via `setupDefaultHttpClient(multiUrlProvider = get(), urlType = …)`)
 * with no per-client wiring. [getBaseUrl] with no argument falls back to the registry's
 * [UrlType.MAIN] entry (the [MultiUrlConfigProvider] default).
 *
 * Registered in Koin as `single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }` (see
 * `NetworkModule`), with the [AccessPointRegistry] resolved from the fork's `AppAccessPoints.points`.
 */
class AppMultiUrlConfigProvider(
    private val registry: AccessPointRegistry,
) : MultiUrlConfigProvider {

    override fun getBaseUrl(type: UrlType): String =
        registry.restBaseUrl(type)
            ?: registry.restBaseUrl(UrlType.MAIN)
            ?: error("No REST access point registered for $type or MAIN")

    override fun getLoggableHosts(): List<String> = registry.loggableHosts()
}
