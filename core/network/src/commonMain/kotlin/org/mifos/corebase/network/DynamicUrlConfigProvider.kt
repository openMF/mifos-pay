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

/**
 * Interface for providing dynamic URL configuration at runtime.
 *
 * Implementations of this interface allow the HTTP client to dynamically
 * switch between different server endpoints without recreating the client.
 */
interface DynamicUrlConfigProvider {
    /**
     * Returns the current base URL to use for API requests.
     */
    fun getBaseUrl(): String

    /**
     * Returns the list of hostnames that should have HTTP logging enabled.
     */
    fun getLoggableHosts(): List<String>
}

/**
 * Extension of [DynamicUrlConfigProvider] for applications with multiple
 * URL types (e.g., main API, self-service API, interbank API).
 */
interface MultiUrlConfigProvider : DynamicUrlConfigProvider {
    /**
     * URL type identifier for different API endpoints.
     */
    enum class UrlType {
        MAIN,
        SELF_SERVICE,
        INTERBANK,
    }

    /**
     * Returns the base URL for the specified URL type.
     */
    fun getBaseUrl(type: UrlType): String

    override fun getBaseUrl(): String = getBaseUrl(UrlType.MAIN)
}
