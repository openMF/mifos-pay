/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.logging.LogLevel

/**
 * Per-point Supabase client factory.
 *
 * Builds one [SupabaseConfigClient] per declared Supabase [AccessPoint], entirely from that point's
 * generated [AccessPoint.supabase]: URL from [AccessPoint.baseUrl], key from [SupabaseSpec.publishableKey].
 * Sign-in is not installed here: an app that needs it adds kmp-supabase-auth via [installExtrasFor].
 *
 * Cached per id so consumers can inject the factory + resolve on demand without re-building.
 */
class SupabaseClientFactory(
    private val registry: AccessPointRegistry,
    private val logLevel: LogLevel = LogLevel.INFO,
    /**
     * Per-access-point fork seam: extra supabase-kt modules to install on that point's client
     * (Auth via kmp-supabase-auth, Realtime, Storage). Defaults to none — the neutral template, which
     * talks to Supabase under the publishable key alone.
     *
     * Keyed by id because a fork may run several projects and want Auth on only one of them.
     */
    private val installExtrasFor: (id: String) -> SupabaseClientBuilder.() -> Unit = { {} },
    /**
     * Runtime network configs. When bound, a point's client is built against its EFFECTIVE config —
     * so editing it, or switching main to another Supabase config, repoints it. A client already
     * handed out keeps its project; the next [clientFor] builds the new one.
     */
    private val networkConfigs: NetworkConfigRepository? = null,
) {
    private val cache: MutableMap<String, Pair<String, SupabaseConfigClient>> = mutableMapOf()

    /** Client for [id], or `null` if [id] is not a declared Supabase access point. */
    fun clientFor(id: String): SupabaseConfigClient? {
        val point = registry.byId(id)?.takeIf { it.kind == AccessPointKind.SUPABASE } ?: return null
        val config = networkConfigs?.effectiveFor(point)
        val url = config?.baseUrl ?: point.baseUrl
        return cache[id]?.takeIf { (builtFor, _) -> builtFor == url }?.second ?: build(id, config, url)
    }

    /** A fresh client for [id] at [url], cached until the effective URL changes. */
    private fun build(id: String, config: NetworkConfig?, url: String): SupabaseConfigClient {
        // A declared config's key comes from the fork's generated spec; one added at runtime carries its own.
        val key = config?.headers?.get(API_KEY_HEADER)?.takeIf { it.isNotBlank() }
            ?: registry.byId(id)?.supabase?.publishableKey.orEmpty()
        val client = SupabaseConfigClient(
            credentials = object : SupabaseCredentials {
                override val url: String = url
                override val publishableKey: String = key
            },
            logLevel = logLevel,
            installExtras = installExtrasFor(id),
        )
        cache[id] = url to client
        return client
    }

    /**
     * Client for [id], or throw naming every declared Supabase point.
     *
     * The [clientFor] null is right for "probe whether this fork configured Supabase"; it is wrong for
     * DI wiring, where a typo'd or undeclared id must fail loudly at graph construction rather than
     * inject a null-shaped absence. This is the Supabase twin of [ktorfitFor]'s error, and [supabaseApi]
     * is its only intended caller.
     */
    fun requireClientFor(id: String): SupabaseConfigClient =
        clientFor(id) ?: error(
            "No SUPABASE access point '$id' declared in app-profile network.access_points " +
                "(AccessPointRegistry). Declared Supabase points: " +
                registry.supabasePoints().joinToString { it.id }.ifEmpty { "(none)" } +
                ". Declare it there with `type: supabase`, or fix the id.",
        )

    /** Map of every declared Supabase access-point id → its client. */
    fun clients(): Map<String, SupabaseConfigClient> =
        registry.supabasePoints().mapNotNull { p -> clientFor(p.id)?.let { p.id to it } }.toMap()

    private companion object {
        /** The header a Supabase key travels in — how a runtime-added config supplies its own. */
        const val API_KEY_HEADER = "apikey"
    }
}
