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

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

/**
 * Told when a request that CARRIED credentials is answered 401 — the session the app holds has been
 * rejected (expired, revoked, password changed, or issued by another server).
 *
 * Opt-in: bind one in Koin and `restApi` wires it into every REST client; bind none and nothing
 * changes. Without it a rejected session is silent wherever a screen has cached data — the refresh
 * fails in the background and the user keeps reading stale data as if signed in (measured on device
 * 2026-10-24). A typical handler ends the session so the app returns to sign-in.
 *
 * Called once per such response, possibly concurrently; make it idempotent. It must not throw.
 */
fun interface UnauthorizedHandler {
    /** The server rejected a credentialed request to [accessPointId] with 401. */
    fun onUnauthorized(accessPointId: String)
}

/**
 * Notifies [handler] of a 401 on a request that sent an `Authorization` header. A 401 without one —
 * a mistyped password on the sign-in call itself — is not a rejected session and is ignored.
 *
 * Observes only: the exception still propagates, so the caller's own error handling is unchanged.
 * Uses the exception hook rather than a response validator because `expectSuccess` raises the 401
 * as a [ResponseException] before a later validator would run.
 */
fun HttpClientConfig<*>.notifyUnauthorized(accessPointId: String, handler: UnauthorizedHandler) {
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            val status = (cause as? ResponseException)?.response?.status
            if (status == HttpStatusCode.Unauthorized && request.headers.contains(HttpHeaders.Authorization)) {
                handler.onUnauthorized(accessPointId)
            }
        }
    }
}
