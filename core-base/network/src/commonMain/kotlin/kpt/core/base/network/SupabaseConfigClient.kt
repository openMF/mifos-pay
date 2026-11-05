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

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Generic Supabase client wrapper for KMP projects.
 *
 * This class provides a reusable Supabase client setup with:
 * - Lazy initialization of Supabase client
 * - Configurable logging level
 * - Direct access to Postgrest for queries
 *
 * ## Usage
 *
 * ```kotlin
 * // Consumers do NOT construct this directly — SupabaseClientFactory builds one per declared
 * // SUPABASE access point, and `supabaseApi("<id>")` injects it into the fork's API type.
 * val configClient = supabaseClientFactory.requireClientFor("project")
 *
 * // Use the client for queries
 * if (configClient.isConfigured) {
 *     val result = configClient.postgrest
 *         .from("app_server_config")
 *         .select()
 *         .decodeList<MyServer>()
 * }
 * ```
 *
 * @param credentials The Supabase credentials (URL and publishable key).
 * @param logLevel The logging level for Supabase client. Defaults to INFO.
 */
class SupabaseConfigClient(
    private val credentials: SupabaseCredentials,
    private val logLevel: LogLevel = LogLevel.INFO,
    /**
     * Fork seam for the modules this exposer does not install.
     *
     * Postgrest is always installed; Auth / ComposeAuth / Realtime / Storage are opt-in. Before this
     * seam existed a fork that needed Auth had to build its OWN `createSupabaseClient` — and then the
     * client the generated `supabaseApi(...)` binding hands to an `@ApiBinding` type was a DIFFERENT
     * instance from the one the fork signed in on, so every RLS-gated call resolved no `auth.uid()`.
     * The only way out was to stop using `@ApiBinding` and hand-wire the singles, which is the drift
     * NAP-7 refuses. With the seam there is ONE client per access point: sign-in and every table API
     * share it, and the annotation keeps working.
     */
    private val installExtras: SupabaseClientBuilder.() -> Unit = {},
) {
    /**
     * Lazily initialized Supabase client.
     * Only created when first accessed.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = credentials.url,
            supabaseKey = credentials.publishableKey,
        ) {
            defaultLogLevel = logLevel
            install(Postgrest)
            installExtras()
        }
    }

    /**
     * Direct access to Postgrest for database operations (the default data plane).
     *
     * Postgrest is installed by default (see [client]), so this is the always-available surface.
     * Auth / Realtime / Storage are OPT-IN — a fork installs those modules explicitly in its own
     * client builder; they are out of scope for this default exposer.
     */
    val postgrest get() = client.postgrest

    /**
     * Default data-plane exposer — Postgrest. Prefer [data] in fork call-sites so the default
     * transport can evolve without touching every caller.
     */
    val data get() = postgrest

    /**
     * Checks if Supabase is properly configured.
     *
     * @return true if credentials are valid, false otherwise.
     */
    val isConfigured: Boolean
        get() = credentials.isConfigured
}

/**
 * A Supabase access point's client config — GENERATED onto [AccessPoint.supabase] by `syncForkConfig`
 * from `app-profile/app.yaml#network.access_points[]`, so a fork configures Supabase in app-profile only.
 *
 * @property publishableKey the project's client key (`sb_publishable_…`, or the legacy anon JWT), read at
 *   BUILD time from `publishable_key_env:` — never committed. It is public by design: Row Level Security is
 *   the boundary. Empty leaves the client inert (`isConfigured == false`).
 *
 * There is no sign-in config here on purpose. A point's client talks to Supabase under this key alone;
 * an app that signs users in (Google, Apple, guest) adds `io.github.mobilebytelabs:cmp-supabase-auth`
 * through [SupabaseExtrasProvider], which installs Auth on this same client.
 */
data class SupabaseSpec(
    val publishableKey: String,
)

/**
 * Supabase URL + client key for one [SupabaseConfigClient].
 *
 * [SupabaseClientFactory] supplies them per access point — URL from [AccessPoint.baseUrl], key from
 * [SupabaseSpec.publishableKey] — so a fork never implements this; it is the seam a test fakes.
 */
interface SupabaseCredentials {
    /** The Supabase project URL, e.g. `https://<ref>.supabase.co`. */
    val url: String

    /**
     * The project's publishable (client) key. Safe in client code; never a `service_role` / `sb_secret_`
     * key, which bypasses RLS (enforced by `scripts/product-health/checks/supabase-client-security.sh`).
     */
    val publishableKey: String

    /** False while the URL or key is blank, a `YOUR_` placeholder, or the URL is not https. */
    val isConfigured: Boolean
        get() = url.isNotBlank() &&
            publishableKey.isNotBlank() &&
            !url.contains("YOUR_") &&
            !publishableKey.contains("YOUR_") &&
            url.startsWith("https://")
}
