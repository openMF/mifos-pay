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

/**
 * Headers this app sends on EVERY request, resolved per access point.
 *
 * `setupDefaultHttpClient` has always accepted `defaultHeaders`, but nothing reached it: the REST
 * clients are all built by [AccessPointRegistry.ktorfitFor], which passed only the base URL and the
 * loggable/proxied hosts. A fork that needed an `X-Api-Key` or an `X-Client-Version` on every call
 * had no way to supply one short of hand-building its own Ktorfit and giving up the generated
 * bindings. This is the seam that closes it.
 *
 * ## Per access point, not global
 * [headersFor] takes the access-point id because a header is almost never app-wide: a key belongs to
 * ONE endpoint, and sending it to the others leaks a credential to hosts that never needed it.
 * Returning the same map for every id is a deliberate choice a fork can still make; the reverse —
 * scoping a global map back down — is not expressible.
 *
 * ```kotlin
 * object KptNetworkHeaders : DefaultHeaderProvider {
 *     override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
 *         "main" -> mapOf("X-Client-Version" to BuildKonfig.VERSION_NAME)
 *         else -> emptyMap()
 *     }
 * }
 * ```
 *
 * ## What does NOT belong here
 * `Authorization`. Bearer/Basic/Digest go through `setupDefaultHttpClient`'s `authProviders`, which
 * installs Ktor's `Auth` plugin and can refresh a token; a header pinned here is captured once at
 * client construction and never refreshes. It would work until the token expired.
 *
 * A per-REQUEST value (a request id, a trace span) does not belong here either — this map is read
 * once per client, not once per call. Use a Ktor plugin for that.
 *
 * Bound in `NetworkModule`; a fork overrides it by binding its own implementation. Resolved
 * optionally, so a fork that never binds one gets the empty default and no behaviour change.
 */
interface DefaultHeaderProvider {

    /**
     * Headers to append to every request for [accessPointId] — the `id:` from
     * `app-profile/app.yaml#network.access_points`.
     *
     * Returns empty by default: the neutral template sends nothing extra.
     */
    fun headersFor(accessPointId: String): Map<String, String> = emptyMap()

    /** Provider presets. */
    companion object {
        /** The no-op default. Bound by `NetworkModule` unless a fork binds its own. */
        val None: DefaultHeaderProvider = object : DefaultHeaderProvider {}
    }
}
