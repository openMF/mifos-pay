/*
 * Copyright 2025 Mifos Initiative
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kpt.core.base.store.infra.DecisionEngine
import kpt.core.base.store.infra.FetchedAtRepository
import org.mobilenativefoundation.store.store5.Store

/**
 * Load-once variant of [asScreenStream] for edit/mutation screens.
 *
 * Transitions: `Loading → Content(initialData)` — then stops. Further Store updates
 * are ignored so in-progress user edits are never overwritten by background refreshes.
 * Error / NoNetwork states still propagate so the user can retry and reach Content.
 *
 * Use this on edit screens where data is fetched once, then the user edits and submits.
 * [asScreenStream] on an edit screen would silently reset form fields on background refresh.
 *
 * @param key Store key to load.
 * @param networkMonitor Network state provider (injected via Koin).
 * @param fetchedAtRepository Persists load timestamps for freshness indicators.
 * @param cacheKey Identifier for [fetchedAtRepository] persistence.
 * @param scope CoroutineScope (typically viewModelScope).
 * @param isEmpty Optional empty-data predicate (same semantics as [asScreenStream]).
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
fun <Key : Any, Output : Any> Store<Key, Output>.asLoadOnceStream(
    key: Key,
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKey: String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
): ScreenDataStream<Output> {
    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Load-once captures the first Content and stops, so it wires no forceFreshTrigger — a forced
    // fresh fetch is meaningless once the stream has terminated. ScreenDataStream defaults that
    // parameter, so refreshFresh() on a load-once stream is simply inert.
    val stateFlow = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            // Re-seed persisted timestamp for freshness banner on retry.
            val persistedFetchedAt = fetchedAtRepository.read(cacheKey)

            combine(
                streamDataForPolicy(key = key, policy = fetchPolicy, isEmpty = isEmpty),
                networkMonitor.networkStatus,
            ) { storeData, networkStatus ->
                val enriched = if (
                    storeData.fetchedAtInstant == null && persistedFetchedAt != null
                ) {
                    storeData.copy(fetchedAtInstant = persistedFetchedAt)
                } else {
                    storeData
                }
                Pair(enriched, networkStatus)
            }
                .transformWhile { (storeData, networkStatus) ->
                    val state = DecisionEngine.decide(storeData, networkStatus)
                    emit(state)
                    if (state is ScreenState.Content) {
                        // Persist the timestamp on first successful load.
                        storeData.fetchedAtInstant?.let {
                            scope.launch { fetchedAtRepository.write(cacheKey, it) }
                        }
                        false // Stop — content captured, ignore further Store updates.
                    } else {
                        true // Keep emitting until Content arrives.
                    }
                }
        }

    return ScreenDataStream(
        state = stateFlow,
        refreshTrigger = refreshTrigger,
    )
}
