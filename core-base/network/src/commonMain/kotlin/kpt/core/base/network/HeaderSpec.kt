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
 * A header an access point sends on every request, declared in
 * `app-profile/app.yaml#network.access_points[].headers[]`.
 *
 * Two kinds, because they have different lifetimes:
 *
 *  - **static** ([value] set) — known at build time. A tenant id, an API version. Baked into the
 *    generated `AppAccessPoints`.
 *  - **runtime** ([runtimeKey] set) — NOT known until the app runs: a Basic credential built at
 *    login, an OAuth bearer that rotates. Declared here by NAME only; the value is written into
 *    [RuntimeHeaderStore] when it becomes known and read again on every request.
 *
 * A runtime header is declared, not invented: the endpoint states which headers it needs, and login
 * only supplies the value. That is what stops "which header does this API want" from living in a
 * ViewModel somewhere.
 *
 * @param name the HTTP header name.
 * @param value a build-time constant, or null when the value is supplied at runtime.
 * @param runtimeKey the [RuntimeHeaderStore] key holding the value, or null for a static header.
 */
data class HeaderSpec(
    val name: String,
    val value: String? = null,
    val runtimeKey: String? = null,
) {
    init {
        require((value == null) != (runtimeKey == null)) {
            "HeaderSpec('$name') must set exactly one of value= (static) or runtimeKey= (runtime)"
        }
    }
}

/**
 * Values for the `runtimeKey` headers, written when they become known and read on every request.
 *
 * ## Why a store rather than a header value
 * A value captured when the HTTP client is BUILT can never reflect a later login: the client is a
 * singleton created at Koin start, long before anyone signs in, and a `Bearer null` pinned then stays
 * `Bearer null` for the process lifetime. Reading through this store on each request is what makes
 * "log in, and every subsequent call carries the credential" true without rebuilding clients.
 *
 * ```kotlin
 * // after a successful sign-in
 * runtimeHeaders[FineractHeaders.AUTH] = "Basic ${base64("$user:$password")}"
 *
 * // on sign-out — clearing is as important as setting
 * runtimeHeaders.clear(FineractHeaders.AUTH)
 * ```
 *
 * Unset and cleared both resolve to null, and a null-valued header is OMITTED rather than sent empty:
 * an empty `Authorization:` is a malformed credential that some servers answer 400 to and others
 * treat as anonymous, which is a confusing way to discover you are logged out.
 *
 * COPY-ON-WRITE behind a `@Volatile` reference: every mutation publishes a whole new immutable map,
 * so a request thread either sees the old map or the new one — never a half-updated one. That is the
 * same pattern `ChargeTypeConverters` uses for its encryptor, and it needs no extra dependency.
 */
class RuntimeHeaderStore {
    @kotlin.concurrent.Volatile
    private var snapshot: Map<String, String> = emptyMap()

    /** The current value for [key], or null when unset. */
    operator fun get(key: String): String? = snapshot[key]

    /** Set (or replace) the value for [key]. */
    operator fun set(key: String, value: String) {
        snapshot = snapshot + (key to value)
    }

    /** Drop [key] — call this on sign-out, or the previous user's credential outlives the session. */
    fun clear(key: String) {
        snapshot = snapshot - key
    }

    /** Drop every value. */
    fun clearAll() {
        snapshot = emptyMap()
    }

    /**
     * Resolve [specs] to the headers to send NOW.
     *
     * Runtime headers whose value is unset are omitted entirely — see the class KDoc for why an
     * empty header is worse than an absent one.
     */
    fun resolve(specs: List<HeaderSpec>): Map<String, String> = buildMap {
        specs.forEach { spec ->
            val v = spec.value ?: spec.runtimeKey?.let { snapshot[it] }
            if (!v.isNullOrEmpty()) put(spec.name, v)
        }
    }
}
