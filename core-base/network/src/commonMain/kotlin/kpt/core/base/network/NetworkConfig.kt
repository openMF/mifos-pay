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

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * One network config as the app runs it — a declared access point (`app-profile/app.yaml`) with any
 * runtime changes applied, or a config added at runtime.
 *
 * [baseUrl] is the full URL requests resolve against (origin + API path). [headers] are header VALUES
 * that replace the declared ones by name and add any the declaration lacks (a tenant, a region);
 * declared runtime headers such as `Authorization` are untouched.
 */
@Serializable
data class NetworkConfig(
    /** The access point id it configures, or a new id for one added at runtime. */
    val id: String,
    /** REST or Supabase — main only switches between configs of the same kind. */
    val kind: AccessPointKind,
    /** The full URL requests resolve against: origin + API path. */
    val baseUrl: String,
    /** Header values that replace the declared ones by name, or add ones the declaration lacks. */
    val headers: Map<String, String> = emptyMap(),
    /** False = requests through it fail loudly instead of going to a server the app switched off. */
    val enabled: Boolean = true,
    /** Created at runtime, not declared in app-profile — the only kind [NetworkConfigRepository.remove] removes. */
    val runtimeAdded: Boolean = false,
)

/** The persisted half: only what differs from app-profile. */
@Serializable
data class NetworkConfigOverrides(
    /** The config main-bound clients follow; null = the one app-profile marks `main: true`. */
    val mainId: String? = null,
    /** Changed declared configs and runtime-added ones, by id. */
    val configs: Map<String, NetworkConfig> = emptyMap(),
)

/** Where [NetworkConfigRepository] keeps its overrides. */
interface NetworkConfigStore {
    /** The saved overrides, or empty ones when nothing was saved. Called once, at construction. */
    fun load(): NetworkConfigOverrides

    /** Replace the saved overrides with [overrides]. */
    fun save(overrides: NetworkConfigOverrides)
}

/** Not persisted — for tests and for an app that wants changes to last one session only. */
class InMemoryNetworkConfigStore(
    private var overrides: NetworkConfigOverrides = NetworkConfigOverrides(),
) : NetworkConfigStore {
    override fun load() = overrides
    override fun save(overrides: NetworkConfigOverrides) {
        this.overrides = overrides
    }
}

/** Persists the overrides as JSON in [settings] (the app's plain settings). A corrupt value reads as none. */
class SettingsNetworkConfigStore(private val settings: Settings, private val key: String = KEY) : NetworkConfigStore {
    override fun load(): NetworkConfigOverrides =
        settings.getStringOrNull(key)?.let {
            runCatching { json.decodeFromString<NetworkConfigOverrides>(it) }.getOrNull()
        }
            ?: NetworkConfigOverrides()

    override fun save(overrides: NetworkConfigOverrides) = settings.putString(key, json.encodeToString(overrides))

    private companion object {
        const val KEY = "kpt.network.configs"
        val json = Json { ignoreUnknownKeys = true }
    }
}

/**
 * The app's network configs at runtime: every access point app-profile declares, changeable, plus any
 * added at runtime — with one of them MAIN.
 *
 * **Main.** App-profile marks one access point `main: true`. Clients bound to THAT point follow
 * whichever config is main now, so [setMain] repoints them — prod ↔ staging, or a server the user typed
 * in. Main switches only between configs of the same [AccessPointKind] (a REST client cannot talk to a
 * Supabase project). Every other client follows its own config.
 *
 * **Persisted and seeded.** Changes go to the [NetworkConfigStore] and are loaded at construction —
 * synchronously, so the first request of a cold start already uses them. [reset] / [resetAll] return to
 * app-profile.
 *
 * Wired by core-base: `ktorfitFor` resolves each request's URL and headers through [urlFor] and
 * [headersFor] when this is bound, and `SupabaseClientFactory` builds from [configFor].
 */
class NetworkConfigRepository(
    private val registry: AccessPointRegistry,
    private val store: NetworkConfigStore = InMemoryNetworkConfigStore(),
) {
    private val declared: Map<String, NetworkConfig> = registry.points.associate { it.id to it.toNetworkConfig() }

    /** The access point app-profile marks `main: true`, or null when it marks none. */
    val declaredMain: AccessPoint? = registry.points.filter { it.isMain }.let { mains ->
        require(mains.size <= 1) {
            "app-profile marks ${mains.size} access points main: ${mains.map { it.id }} — mark exactly one"
        }
        mains.singleOrNull()
    }

    private val overrides = MutableStateFlow(store.load().validated())

    /** Every config — declared ones with changes applied, then runtime-added ones. */
    val configs: StateFlow<NetworkConfigOverrides> = overrides.asStateFlow()

    /** The config main-bound clients follow now. */
    val mainId: String? get() = overrides.value.mainId ?: declaredMain?.id

    /** Every config — declared ones with changes applied, then runtime-added ones. */
    fun all(): List<NetworkConfig> = (declared.keys + overrides.value.configs.keys).distinct().mapNotNull(::configFor)

    /** The config main-bound clients use now, or null when app-profile marks no main. */
    fun mainConfig(): NetworkConfig? = mainId?.let(::configFor)

    /** [id]'s config as the app runs it, or null when there is none. */
    fun configFor(id: String): NetworkConfig? = overrides.value.configs[id] ?: declared[id]

    /** The config [point]'s clients use: the main one for the main-bound point, else its own. */
    fun effectiveFor(point: AccessPoint): NetworkConfig? =
        configFor(if (point.isMain) mainId ?: point.id else point.id)

    /** The URL [point]'s next request goes to. Throws for a disabled config, so the call fails loudly. */
    fun urlFor(point: AccessPoint): String {
        val config = effectiveFor(point) ?: return point.effectiveUrl
        check(config.enabled) { "Network config '${config.id}' is disabled" }
        return config.baseUrl
    }

    /** [declaredHeaders] (already resolved for [point]) with the effective config's values applied. */
    fun headersFor(point: AccessPoint, declaredHeaders: Map<String, String>): Map<String, String> {
        val overrideValues = effectiveFor(point)?.headers.orEmpty().filterValues { it.isNotBlank() }
        return declaredHeaders + overrideValues
    }

    /** Changes [id]'s URL and/or header values (a declared or runtime-added config). */
    fun update(id: String, baseUrl: String? = null, headers: Map<String, String>? = null) {
        val current = requireNotNull(configFor(id)) { "No network config '$id'" }
        put(current.copy(baseUrl = baseUrl ?: current.baseUrl, headers = headers ?: current.headers))
    }

    /** Adds a config at runtime — e.g. a server the user typed in — which [setMain] can then select. */
    fun add(config: NetworkConfig) {
        require(configFor(config.id) == null) { "Network config '${config.id}' already exists" }
        put(config.copy(runtimeAdded = true))
    }

    /** Makes [id] the config main-bound clients follow. Same kind as the declared main, and enabled. */
    fun setMain(id: String) {
        val config = requireNotNull(configFor(id)) { "No network config '$id'" }
        val main = requireNotNull(declaredMain) { "app-profile marks no access point main: true" }
        require(config.kind == main.kind) { "'$id' is ${config.kind}; the main config is ${main.kind}" }
        require(config.enabled) { "'$id' is disabled" }
        save(overrides.value.copy(mainId = id.takeUnless { it == main.id }))
    }

    /** Switches [id] off so its requests fail. The main config cannot be disabled. */
    fun disable(id: String) {
        require(id != mainId) { "The main config cannot be disabled — switch main first" }
        put(requireNotNull(configFor(id)) { "No network config '$id'" }.copy(enabled = false))
    }

    /** Switches [id] back on. */
    fun enable(id: String) = put(requireNotNull(configFor(id)) { "No network config '$id'" }.copy(enabled = true))

    /** Deletes a runtime-added config. Declared ones are not removed — [disable] or [reset] them. */
    fun remove(id: String) {
        val config = requireNotNull(configFor(id)) { "No network config '$id'" }
        require(config.runtimeAdded) { "'$id' is declared in app-profile; disable or reset it instead" }
        require(id != mainId) { "The main config cannot be removed — switch main first" }
        save(overrides.value.copy(configs = overrides.value.configs - id))
    }

    /** Back to app-profile for [id]; a main pointing at a config that no longer exists returns to the declared main. */
    fun reset(id: String) {
        val next = overrides.value.configs - id
        save(overrides.value.copy(configs = next).validated())
    }

    /** Back to app-profile for everything: drops every change, runtime-added config and main switch. */
    fun resetAll() = save(NetworkConfigOverrides())

    private fun put(config: NetworkConfig) {
        val changed = config.takeUnless { !it.runtimeAdded && it == declared[it.id] }
        val configs = overrides.value.configs
        val next = if (changed == null) configs - config.id else configs + (config.id to config)
        save(overrides.value.copy(configs = next))
    }

    private fun save(next: NetworkConfigOverrides) {
        overrides.value = next
        store.save(next)
    }

    /** Drops a stored main that no longer resolves (e.g. app-profile removed that point). */
    private fun NetworkConfigOverrides.validated(): NetworkConfigOverrides {
        val main = mainId ?: return this
        val ok = (configs[main] ?: declared[main])?.let { it.enabled && it.kind == declaredMain?.kind } == true
        return if (ok) this else copy(mainId = null)
    }
}

/** A declared point as a [NetworkConfig]: its URL and its static header values. */
fun AccessPoint.toNetworkConfig(): NetworkConfig = NetworkConfig(
    id = id,
    kind = kind,
    baseUrl = effectiveUrl,
    headers = headers.mapNotNull { spec -> spec.value?.let { spec.name to it } }.toMap(),
)
