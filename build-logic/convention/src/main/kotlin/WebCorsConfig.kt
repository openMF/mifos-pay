/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.gradle.api.GradleException

/**
 * `app-profile/app.yaml#network.web_cors`, parsed and validated.
 *
 * @property mode `relay` (web calls to allowlisted hosts go through [relay]) or `direct` (no rewriting).
 * @property relay a same-origin path (`/cors`) or an https:// external relay; null in `direct` mode.
 * @property allowlist the host patterns relayed — every access point's host, each legacy `proxied_host`,
 *   and `web_cors.hosts` — distinct, in that order. Empty in `direct` mode, so the worker relays nothing.
 * @property warnings deprecation notes for the build log (the retired `web_cors_proxy` key).
 */
internal data class WebCorsConfig(
    val mode: String,
    val relay: String?,
    val allowlist: List<String>,
    val warnings: List<String> = emptyList(),
    /** Page origins (`https://host[:port]`) another site serves the app from, using THIS site's relay. */
    val allowedOrigins: List<String> = emptyList(),
)

private val RELAY_PATH = Regex("^/[A-Za-z0-9_-]+$")
private val HOST_LABEL = "[a-z0-9]([a-z0-9-]*[a-z0-9])?"
private val PLAIN_HOST = Regex("^$HOST_LABEL(\\.$HOST_LABEL)*$")
private val WILDCARD_HOST = Regex("^\\*\\.$HOST_LABEL(\\.$HOST_LABEL)+$")
private val ORIGIN = Regex("^https://$HOST_LABEL(\\.$HOST_LABEL)*(:[0-9]{1,5})?$")

private fun fail(msg: String): Nothing = throw GradleException("syncForkConfig: network.web_cors $msg")

/**
 * Parses `network.web_cors` from the `network` map. An absent block means relay mode through `/cors`. The
 * retired `network.web_cors_proxy` is still read when `web_cors` is absent (`none` → direct), with a
 * deprecation warning; when both are present the new block wins.
 */
internal fun parseWebCors(network: Map<*, *>): WebCorsConfig {
    val warnings = mutableListOf<String>()
    val legacy = network["web_cors_proxy"]?.toString()?.trim()
    val block = network["web_cors"]
    var mode = "relay"
    var relay: String? = "/cors"
    var hosts: List<String> = emptyList()
    var origins: List<String> = emptyList()
    when {
        block != null -> {
            val m = block as? Map<*, *> ?: fail("must be a map (mode / relay / hosts).")
            mode = m["mode"]?.toString()?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: "relay"
            if (mode !in setOf("relay", "direct")) fail("mode '$mode' must be relay or direct.")
            if (m.containsKey("relay")) relay = m["relay"]?.toString()?.trim()?.takeIf { it.isNotEmpty() && it != "none" }
            val rawHosts = m["hosts"] ?: emptyList<Any>()
            hosts = (rawHosts as? List<*> ?: fail("hosts must be a list of host names."))
                .map { it.toString().trim().lowercase() }
            val rawOrigins = m["allowed_origins"] ?: emptyList<Any>()
            origins = (rawOrigins as? List<*> ?: fail("allowed_origins must be a list of https:// origins."))
                .map { it.toString().trim().lowercase().trimEnd('/') }
            if (legacy != null) warnings += "network.web_cors_proxy is ignored — network.web_cors is set; delete the old key."
        }
        legacy != null -> {
            warnings += "network.web_cors_proxy is deprecated — move it to network.web_cors.relay " +
                "(or `web_cors: { mode: direct }` for none)."
            if (legacy.isEmpty() || legacy == "none") mode = "direct" else relay = legacy
        }
    }
    if (mode == "relay") {
        val r = relay ?: fail("mode relay needs a relay (`/cors`, or an https:// URL) — or set mode: direct.")
        if (!r.startsWith("https://") && !RELAY_PATH.matches(r)) {
            fail("relay '$r' must be a same-origin path like /cors or an https:// URL.")
        }
    }
    hosts.firstOrNull { !PLAIN_HOST.matches(it) && !WILDCARD_HOST.matches(it) }?.let {
        fail(
            "hosts entry '$it' must be a host name (api.example.com) or a subdomain wildcard (*.example.com) — " +
                "no scheme, path, port or bare *.",
        )
    }
    origins.firstOrNull { !ORIGIN.matches(it) }?.let {
        fail("allowed_origins entry '$it' must be an https:// origin (https://example.github.io) — no path or wildcard.")
    }
    if (mode == "direct") return WebCorsConfig("direct", null, emptyList(), warnings)

    val points = (network["access_points"] as? List<*>).orEmpty().mapNotNull { it as? Map<*, *> }
    val pointHosts = points.mapNotNull { p ->
        runCatching { java.net.URI(p["base_url"]?.toString()?.trim().orEmpty()).host }.getOrNull()?.lowercase()
    }
    val proxied = points.mapNotNull { it["proxied_host"]?.toString()?.trim()?.lowercase()?.takeIf(String::isNotEmpty) }
    return WebCorsConfig("relay", relay, (pointHosts + proxied + hosts).distinct(), warnings, origins.distinct())
}

/**
 * `cmp-web/src/{js,wasmJs}Main/resources/relay-allowlist.json` — the list the deployed `_worker.js`
 * relays, so the worker and the app read ONE list. JSON has no comments, hence the `_generated` key.
 */
internal fun relayAllowlistJson(cors: WebCorsConfig): String {
    fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    fun list(xs: List<String>) = if (xs.isEmpty()) "[]" else xs.joinToString(",\n    ", "[\n    ", "\n  ]") { q(it) }
    val hosts = list(cors.allowlist)
    return "{\n" +
        "  \"_generated\": \"GENERATED by syncForkConfig from app-profile/app.yaml#network — do not hand-edit\",\n" +
        "  \"mode\": ${q(cors.mode)},\n" +
        "  \"relay\": ${cors.relay?.let(::q) ?: "null"},\n" +
        "  \"hosts\": $hosts,\n" +
        "  \"allowedOrigins\": ${list(cors.allowedOrigins)}\n" +
        "}\n"
}
