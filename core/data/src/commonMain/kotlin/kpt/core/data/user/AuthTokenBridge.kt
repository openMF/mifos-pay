/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.user

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.AuthHeaderBridge
import kpt.core.base.network.AuthScheme
import kpt.core.base.network.AuthTokenSource
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.datastore.prefs.UserPreferencesRepository

/**
 * Binds the network's [AuthTokenSource] port to where the credential actually lives.
 *
 * `core-base/network` must not depend on `core/datastore` — that would invert the module graph — so
 * it declares the port and `core/data`, which already depends on both, supplies it. Same shape as
 * `UserDataSource`, which bridges `core/store` to the same preferences.
 *
 * Every access point sees the SAME token today. That is deliberate rather than incidental: a fork
 * needing per-endpoint credentials overrides this binding with its own [AuthTokenSource] and keys off
 * `accessPointId` — the parameter exists precisely so that is a substitution, not a rewrite.
 */
@DataProvider
fun provideAuthTokenSource(preferences: UserPreferencesRepository): AuthTokenSource =
    AuthTokenSource { _: String -> preferences.observeAuthToken }

/**
 * Eager: starts collecting the credential at graph construction.
 *
 * `createdAtStart` is load-bearing. Built lazily, this would construct on first injection — and
 * nothing injects it, because its whole job is a side effect. `Authorization` would then be empty
 * until something unrelated happened to touch it, which is the kind of bug that reproduces only on
 * a cold start.
 */
@DataProvider(createdAtStart = true)
fun provideAuthHeaderBridge(
    registry: AccessPointRegistry,
    tokenSource: AuthTokenSource,
    headers: RuntimeHeaderStore,
    preferences: UserPreferencesRepository,
    scope: CoroutineScope,
): AuthHeaderBridge = AuthHeaderBridge(
    points = registry.points,
    tokenSource = tokenSource,
    headers = headers,
).also { bridge ->
    // SEED SYNCHRONOUSLY, then start collecting.
    //
    // `start()` publishes the header from a coroutine, so until its first emission lands the store
    // holds no `Authorization` -- and any request issued in that window goes out unauthenticated.
    // The window is normally invisible, which is what makes it worth pinning: it opens widest on the
    // FIRST launch after an install or UPDATE, when dexopt and cold caches slow startup while the
    // app restores straight to an authenticated screen that fetches immediately. Every store update
    // (Play, App Store, Firebase App Distribution) is exactly that launch.
    //
    // MEASURED on device 2026-10-20 (OnePlus CPH2423), same APK installed with `-r` so that only
    // the update itself varied:
    //   first launch after update -> 401, no Authorization header
    //   plain restart after that  -> Authorization present, 200
    // The credential was never lost -- `secure_prefs.xml` was byte-identical across both -- so this
    // is a startup race, not a persistence bug, and seeding is the fix rather than re-reading.
    //
    // Why it is not merely belt-and-braces: a 401 does not necessarily surface as an error. Before
    // `expectSuccess` landed alongside this, the error body was deserialized onto the success type,
    // so a list read became an EMPTY LIST and a write returned `Applied`. Losing this race was
    // SILENT, and a user saw "no data" and "saved" instead of "signed out".
    //
    // `preferences.authToken` is the synchronous accessor that exists for exactly this case.
    // `createdAtStart` runs during Koin graph construction, so this completes before any screen can
    // compose, let alone fetch.
    registry.points
        .filter { point -> point.auth != AuthScheme.NONE }
        .forEach { point ->
            val key = AuthScheme.runtimeKeyFor(point.id)
            when (val value = point.auth.format(preferences.authToken)) {
                null -> headers.clear(key)
                else -> headers[key] = value
            }
        }
    bridge.start(scope)
}
