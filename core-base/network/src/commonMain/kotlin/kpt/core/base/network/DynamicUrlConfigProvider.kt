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

import kotlin.jvm.JvmInline

/**
 * Interface for providing dynamic URL configuration at runtime.
 *
 * Implementations of this interface allow the HTTP client to dynamically
 * switch between different server endpoints without recreating the client.
 *
 * This is useful for:
 * - Multi-tenant applications where users can switch between servers
 * - Development/staging/production environment switching
 * - White-label apps with different backend endpoints
 *
 * Example implementation:
 * ```kotlin
 * class MyConfigProvider(
 *     private val preferencesRepository: UserPreferencesRepository
 * ) : DynamicUrlConfigProvider {
 *     override fun getBaseUrl(): String = preferencesRepository.selectedServer.value?.url
 *         ?: "https://default.api.com"
 *
 *     override fun getLoggableHosts(): List<String> = listOf(
 *         preferencesRepository.selectedServer.value?.host ?: "default.api.com"
 *     )
 * }
 * ```
 */
interface DynamicUrlConfigProvider {
    /**
     * Returns the current base URL to use for API requests.
     * This is called for each request, allowing runtime URL switching.
     */
    fun getBaseUrl(): String

    /**
     * Returns the list of hostnames that should have HTTP logging enabled.
     * This is called dynamically, allowing the logging filter to adapt
     * to the currently selected server.
     */
    fun getLoggableHosts(): List<String>
}

/**
 * Open, extensible identifier for a named API endpoint.
 *
 * This is deliberately **not** a closed enum: `core-base` ships only the generic [MAIN] default,
 * and each project defines its own endpoint names at the `core/` level with vocabulary that fits
 * its domain — e.g. `PRODUCTION`, `STAGING`, `SERVER1`, `PAYMENT_GATEWAY`. See the `AppUrlTypes`
 * example in `core/network`.
 *
 * ```kotlin
 * // in core/ (your project) — name them however you like, add as many as you need
 * object AppUrlTypes {
 *     val MAIN    = UrlType.MAIN
 *     val SERVER1 = UrlType("SERVER1")
 *     val STAGING = UrlType("STAGING")
 * }
 * ```
 *
 * @property key the access-point key, as declared in
 *   `app-profile#network.access_points`.
 */
@JvmInline
value class UrlType(val key: String) {
    /** Known url types. */
    companion object {
        /** The single generic default. Every project has at least this one. */
        val MAIN: UrlType = UrlType("MAIN")
    }
}

/**
 * Extension of [DynamicUrlConfigProvider] for applications that expose more than one endpoint
 * (identified by an open [UrlType] — the project names them in `core/`).
 */
interface MultiUrlConfigProvider : DynamicUrlConfigProvider {
    /**
     * Returns the base URL for the specified [type].
     */
    fun getBaseUrl(type: UrlType): String

    /**
     * Default implementation returns the [UrlType.MAIN] URL.
     */
    override fun getBaseUrl(): String = getBaseUrl(UrlType.MAIN)
}
