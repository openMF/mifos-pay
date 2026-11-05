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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * The stored credential, as a stream.
 *
 * A PORT, implemented where the credential actually lives (`core/data`, over `core/datastore`'s
 * encrypted store). `core-base/network` cannot depend on the datastore without inverting the module
 * graph, and it should not need to: what it needs is "the current token", not where it is kept.
 *
 * Emits null when signed out.
 */
fun interface AuthTokenSource {
    /** The current credential for [accessPointId], re-emitting whenever it changes. */
    fun tokenFlow(accessPointId: String): Flow<String?>
}

/**
 * Keeps [RuntimeHeaderStore] in step with the stored credential, so `Authorization` is automatic.
 *
 * ## Why a bridge rather than reading the store per request
 * Header resolution is SYNCHRONOUS — it runs inside Ktor's `defaultRequest`, which cannot suspend —
 * while the credential lives in a suspending, persistent store. Bridging inverts the problem: the
 * durable store stays the source of truth, this collects its changes once, and the hot path reads an
 * in-memory map. A token restored from disk at startup lands before the first request; one obtained
 * at login lands the moment it is written.
 *
 * ## What it does NOT do
 * It never invents a value. Only points that DECLARE an `auth:` scheme are bridged, each in its own
 * scheme's wire format, so a public endpoint is never handed a credential it did not ask for.
 *
 * ## Manual control is preserved
 * The bridge writes the same [RuntimeHeaderStore] a fork can write directly. Anything not declared —
 * a signed request, a per-tenant key, a scheme this enum does not model — is still just
 * `runtimeHeaders["<id>.auth"] = "…"`, and a fork that wants to TRANSFORM the stored token (wrap it,
 * exchange it, add a prefix) can bind its own [AuthTokenSource] that maps the flow.
 */
class AuthHeaderBridge(
    private val points: List<AccessPoint>,
    private val tokenSource: AuthTokenSource,
    private val headers: RuntimeHeaderStore,
) {
    /** Start collecting. Called once, eagerly, at graph construction. */
    fun start(scope: CoroutineScope) {
        points.filter { it.auth != AuthScheme.NONE }.forEach { point ->
            val key = AuthScheme.runtimeKeyFor(point.id)
            tokenSource.tokenFlow(point.id)
                .distinctUntilChanged()
                .onEach { token ->
                    val value = point.auth.format(token)
                    // A null/blank token CLEARS rather than writes empty — signing out must actually
                    // stop sending the previous user's credential.
                    if (value == null) headers.clear(key) else headers[key] = value
                }
                .launchIn(scope)
        }
    }
}
