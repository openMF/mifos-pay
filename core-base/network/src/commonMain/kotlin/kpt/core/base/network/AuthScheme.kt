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
 * How an access point authenticates, declared as `auth:` in
 * `app-profile/app.yaml#network.access_points[]`.
 *
 * Declaring it is what makes the `Authorization` header AUTOMATIC: the generator emits the header
 * spec, and the auth bridge fills its value from the stored credential in the scheme's wire format.
 * Without it every endpoint would re-implement "prefix the token correctly", which is exactly the
 * kind of detail that is wrong once and then wrong everywhere.
 */
enum class AuthScheme {
    /** No credential. The endpoint is public, or handles auth some other way. */
    NONE,

    /** `Authorization: Basic <token>` — the token is the base64 of `user:password`. */
    BASIC,

    /** `Authorization: Bearer <token>`. */
    BEARER,

    /**
     * OAuth 2.0. On the wire this is `Bearer` — the difference is where the token comes from and
     * that it rotates, which is the bridge's problem, not the header's.
     */
    OAUTH,
    ;

    /**
     * The header value for [token], or null when nothing should be sent.
     *
     * Null for [NONE] and for a blank token: a header is OMITTED rather than sent empty, because an
     * empty `Authorization:` is a malformed credential that some servers answer 400 to and others
     * treat as anonymous — a confusing way to discover you are logged out.
     */
    fun format(token: String?): String? = when {
        this == NONE || token.isNullOrBlank() -> null
        this == BASIC -> "Basic $token"
        else -> "Bearer $token"
    }

    /** Built-in schemes. */
    companion object {
        /** Parse the `auth:` value; unknown or absent means [NONE]. */
        fun from(raw: String?): AuthScheme =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: NONE

        /** The [RuntimeHeaderStore] key an access point's credential is stored under. */
        fun runtimeKeyFor(accessPointId: String): String = "$accessPointId.auth"
    }
}
