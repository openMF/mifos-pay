/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.infra.FetchedAtRepository
import org.koin.mp.KoinPlatform
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Bundles the app-infra dependencies that [asScreenStream] needs — the [NetworkMonitor] and the
 * [FetchedAtRepository] — so a repository injects ONE screen-stream context instead of threading two
 * framework singletons through every read method. Homed here in `core-base/store` next to
 * [asScreenStream] and DI-provided once (a single).
 *
 * Note: with Koin constructor injection, one injected param is the floor — a repository can't reach
 * zero without a `KoinComponent` service-locator (an anti-pattern). This collapses the two scattered
 * infra params into one clearly-named context owned by `core-base/store`.
 */
class ScreenStreamContext(
    /** Connectivity source, so the stream can tell offline apart from a request failure. */
    val networkMonitor: NetworkMonitor,
    /** Last-fetched timestamps — the input to every freshness decision. */
    val fetchedAtRepository: FetchedAtRepository,
)

/**
 * [asScreenStream] overload taking a bundled [ScreenStreamContext] instead of the two infra deps —
 * so a repository reads `store.asScreenStream(key, screen, cacheKey, scope, …)` with `screen` its one
 * injected [ScreenStreamContext]. Delegates to the primary overload (debounce windows keep their
 * defaults; a call site needing custom debounce uses the primary overload directly).
 */
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    key: Key,
    context: ScreenStreamContext = KoinPlatform.getKoin().get(),
    cacheKey: String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
    ttl: Duration = 24.hours,
): ScreenDataStream<Output> = asScreenStream(
    key = key,
    networkMonitor = context.networkMonitor,
    fetchedAtRepository = context.fetchedAtRepository,
    cacheKey = cacheKey,
    scope = scope,
    isEmpty = isEmpty,
    fetchPolicy = fetchPolicy,
    ttl = ttl,
)

/**
 * Dynamic-key [asScreenStream] overload taking a bundled [ScreenStreamContext] — for repositories
 * whose key changes over time ([keyFlow]) with a per-key [cacheKeyFor]. Delegates to the primary
 * dynamic-key overload (debounce windows keep their defaults).
 */
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    keyFlow: Flow<Key>,
    context: ScreenStreamContext = KoinPlatform.getKoin().get(),
    cacheKeyFor: (Key) -> String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
    ttl: Duration = 24.hours,
): ScreenDataStream<Output> = asScreenStream(
    keyFlow = keyFlow,
    networkMonitor = context.networkMonitor,
    fetchedAtRepository = context.fetchedAtRepository,
    cacheKeyFor = cacheKeyFor,
    scope = scope,
    isEmpty = isEmpty,
    fetchPolicy = fetchPolicy,
    ttl = ttl,
)
