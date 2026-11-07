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

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Generic Supabase client wrapper for configuration/data fetching.
 * Projects provide their own credentials implementation.
 */
class SupabaseConfigClient(
    private val credentials: SupabaseCredentials,
    private val logLevel: LogLevel = LogLevel.INFO,
) {
    /** Lazily created Supabase client with the Postgrest plugin installed. */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = credentials.url,
            supabaseKey = credentials.anonKey,
        ) {
            defaultLogLevel = logLevel
            install(Postgrest)
        }
    }

    /** The Postgrest (database) API of the configured Supabase project. */
    val postgrest get() = client.postgrest

    /** Whether Supabase credentials are present. */
    val isConfigured: Boolean get() = credentials.isConfigured
}

/**
 * Interface for providing Supabase credentials.
 * Projects implement this with their own credential sources.
 */
interface SupabaseCredentials {
    /** Supabase project URL. */
    val url: String

    /** Supabase anonymous API key. */
    val anonKey: String

    /** Whether the credentials are configured. */
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()
}
