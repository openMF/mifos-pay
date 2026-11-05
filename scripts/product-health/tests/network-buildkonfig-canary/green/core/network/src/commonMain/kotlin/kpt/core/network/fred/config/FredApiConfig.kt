/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.fred.config

/**
 * Runtime configuration for [kpt.core.network.fred.api.FredApi].
 *
 * The fork is responsible for sourcing the key — typical patterns:
 *
 * ```kotlin
 * // BuildKonfig (preferred)
 * single { FredApiConfig(apiKey = BuildKonfig.FRED_API_KEY) }
 *
 * // System env / Gradle property
 * single { FredApiConfig(apiKey = System.getenv("FRED_API_KEY")) }
 *
 * // Hardcoded for local dev (NEVER commit)
 * single { FredApiConfig(apiKey = "deadbeef...") }
 * ```
 *
 * `apiKey` is intentionally nullable so the toolkit can ship demo data when no
 * key is configured — the repository layer surfaces an explicit "FRED key not
 * configured" error rather than firing the network request with `apiKey=null`.
 */
data class FredApiConfig(
    val apiKey: String?,
    // Base URL is no longer here — the "fred" access point (app-profile network.access_points) owns it.
) {
    /** True when the fork has supplied a non-blank key. */
    val isConfigured: Boolean
        get() = !apiKey.isNullOrBlank()

    companion object {
        /** Sentinel for forks that haven't wired the FRED key yet. */
        val Unconfigured: FredApiConfig = FredApiConfig(apiKey = null)
    }
}
