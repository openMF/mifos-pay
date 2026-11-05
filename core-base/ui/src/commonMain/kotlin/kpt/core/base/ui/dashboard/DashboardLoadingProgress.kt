/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.dashboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.screen.ScreenState

/**
 * Fan-in operator that converts a stream of per-card states into a single
 * [DashboardProgressState] suitable for top-of-screen progress display.
 *
 * Typical dashboard ViewModel:
 * ```kotlin
 * val cards: StateFlow<List<ScreenState<*>>> = combine(
 *     loansStream.state,
 *     billsStream.state,
 *     ratesStream.state,
 *     fxStream.state,
 * ) { l, b, r, f -> listOf(l, b, r, f) }
 *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
 *
 * val progress: StateFlow<DashboardProgressState> = cards
 *     .aggregateDashboardProgress()
 *     .stateIn(
 *         viewModelScope,
 *         SharingStarted.WhileSubscribed(5_000),
 *         DashboardProgressState(0, 0, false, false, false),
 *     )
 * ```
 *
 * Then in the screen:
 * ```kotlin
 * DashboardProgressBar(state = progressState)
 * IndependentCardLayout(states = cards, onRetry = vm::onRetryCard) { i, content -> /* ... */ }
 * ```
 */
fun Flow<List<ScreenState<*>>>.aggregateDashboardProgress(): Flow<DashboardProgressState> =
    map { states -> states.toDashboardProgressState() }

/**
 * Pure conversion from a list of per-card states to a single [DashboardProgressState].
 * Exposed so tests + non-Flow call sites can re-use the aggregation contract.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun List<ScreenState<*>>.toDashboardProgressState(): DashboardProgressState =
    DashboardProgressState(
        loaded = count { it is ScreenState.Content<*> },
        total = size,
        isAnyLoading = any { it is ScreenState.Loading },
        hasAnyError = any { it is ScreenState.Error || it is ScreenState.NoNetwork },
        hasAnyEmpty = any { it is ScreenState.Empty },
        // Stalest loaded card drives the dashboard-level "Updated X ago" freshness signal.
        oldestFetchedAt = mapNotNull { (it as? ScreenState.Content<*>)?.fetchedAt }.minOrNull(),
    )
