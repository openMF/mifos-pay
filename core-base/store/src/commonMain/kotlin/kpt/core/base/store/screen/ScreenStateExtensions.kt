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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.submit.SubmitHandler
import kpt.core.base.store.submit.SubmitState
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Transforms only [ScreenState.Content] data, passing through all other states unchanged.
 */
fun <T, R> Flow<ScreenState<T>>.mapContent(
    transform: (data: T, freshnessSignal: FreshnessSignal) -> R,
): Flow<ScreenState<R>> = map { state ->
    when (state) {
        is ScreenState.Content -> ScreenState.Content(
            data = transform(state.data, state.freshnessSignal),
            fetchedAt = state.fetchedAt,
            freshnessSignal = state.freshnessSignal,
        )
        is ScreenState.Loading -> ScreenState.Loading
        is ScreenState.Empty -> ScreenState.Empty
        is ScreenState.NoNetwork -> state
        is ScreenState.Error -> state
        is ScreenState.Unauthenticated -> state
    }
}

/**
 * Combines ScreenState with a local state flow for reactive filter/sort/preferences.
 */
fun <T, S, R> Flow<ScreenState<T>>.combineContent(
    other: Flow<S>,
    transform: (data: T, extra: S, freshnessSignal: FreshnessSignal) -> R,
): Flow<ScreenState<R>> = combine(this, other) { state, extra ->
    when (state) {
        is ScreenState.Content -> ScreenState.Content(
            data = transform(state.data, extra, state.freshnessSignal),
            fetchedAt = state.fetchedAt,
            freshnessSignal = state.freshnessSignal,
        )
        is ScreenState.Loading -> ScreenState.Loading
        is ScreenState.Empty -> ScreenState.Empty
        is ScreenState.NoNetwork -> state
        is ScreenState.Error -> state
        is ScreenState.Unauthenticated -> state
    }
}

/**
 * Converts Content to Empty when business-level predicate says data is empty.
 * Applied AFTER DecisionEngine (which only handles structural empty from Store).
 */
fun <T> Flow<ScreenState<T>>.emptyIfContent(
    predicate: (T) -> Boolean,
): Flow<ScreenState<T>> = map { state ->
    when (state) {
        is ScreenState.Content -> if (predicate(state.data)) ScreenState.Empty else state
        else -> state
    }
}

/** Extracts data from Content state or null for other states. */
val <T> ScreenState<T>.dataOrNull: T?
    get() = (this as? ScreenState.Content)?.data

/** True if state has displayable content. */
val <T> ScreenState<T>.hasContent: Boolean
    get() = this is ScreenState.Content

/** Maps Error throwable to a user-facing type. */
fun <T> Flow<ScreenState<T>>.mapError(
    transform: (Throwable) -> Throwable,
): Flow<ScreenState<T>> = map { state ->
    when (state) {
        is ScreenState.Error -> ScreenState.Error(
            error = transform(state.error),
            isNetworkError = state.isNetworkError,
        )
        else -> state
    }
}

/**
 * Execute [block] only when [screenState] has loaded content, passing its data.
 * No-op when state is Loading / Error / NoNetwork / Empty — safe to call at any time.
 * Guards against premature submits before data arrives on edit screens.
 */
fun <T, R> SubmitHandler<R>.submitWhenContent(
    screenState: ScreenState<T>,
    block: suspend (data: T) -> R,
) {
    val data = screenState.dataOrNull ?: return
    submit { block(data) }
}

/**
 * True when the screen has content AND no submission is in-flight.
 *
 * Use to enable a submit button without collecting two flows separately:
 * ```kotlin
 * Button(enabled = screenState.canInteract(submitState)) { … }
 * ```
 */
fun <T, R> ScreenState<T>.canInteract(submitState: SubmitState<R>): Boolean =
    hasContent && submitState !is SubmitState.Submitting

/**
 * Wraps any value in a [ScreenState.Content] with [DataFreshness.FRESH].
 * Use for utility or settings screens that hold local state and don't use Store5.
 *
 * Example:
 * ```kotlin
 * val screenState = stateFlow
 *     .map { it.asLocalScreenState() }
 *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)
 * ```
 */
fun <T> T.asLocalScreenState(): ScreenState<T> =
    ScreenState.Content(this)

/**
 * Turns any [Flow]<T> into a [Flow]<[ScreenState]<T>> for use with [ScreenContent].
 *
 * - Emits [ScreenState.Loading] before the first value
 * - Maps each value to [ScreenState.Content] with [DataFreshness.FRESH]
 * - Catches exceptions → [ScreenState.Error]
 *
 * Use on settings, calculator, or other screens that have no network source.
 * For network-backed screens use `Store.asScreenStream` instead.
 */
fun <T> Flow<T>.asLocalScreenStream(): Flow<ScreenState<T>> =
    map { it.asLocalScreenState() }
        .onStart { emit(ScreenState.Loading) }
        .catch { emit(ScreenState.Error(it)) }

/**
 * Combines two independent [ScreenState] flows into one, merging their [ScreenState.Content]
 * data via [transform] when both sources have loaded.
 *
 * **Priority** (first match wins on each emission):
 * `NoNetwork > Loading > Unauthenticated > Error > Empty > Content`
 *
 * Both sources must reach [ScreenState.Content] before [transform] is called.
 * The resulting [DataFreshness] is the "worst" of the two:
 * STALE if either is STALE; UPDATING if either is UPDATING; FRESH only when both are FRESH.
 *
 * Typical use — dashboard screen combining two independent data sources:
 * ```kotlin
 * val screenState: Flow<ScreenState<DashboardUiState>> = combineScreenStates(
 *     a = accountStore.asScreenStream(Unit),
 *     b = transactionStore.asScreenStream(Unit),
 * ) { account, transactions ->
 *     DashboardUiState(account = account, transactions = transactions)
 * }
 * ```
 *
 * For 3, 4, or 5 sources use the corresponding typed overloads.
 */
@OptIn(ExperimentalTime::class)
fun <A, B, R> combineScreenStates(
    a: Flow<ScreenState<A>>,
    b: Flow<ScreenState<B>>,
    transform: (A, B) -> R,
): Flow<ScreenState<R>> = combine(a, b) { sa, sb ->
    @Suppress("UNCHECKED_CAST")
    resolveScreenStates(listOf(sa, sb)) {
        transform(
            (sa as ScreenState.Content<A>).data,
            (sb as ScreenState.Content<B>).data,
        )
    }
}

/**
 * Combines three independent [ScreenState] flows. See [combineScreenStates] for priority rules.
 */
@OptIn(ExperimentalTime::class)
fun <A, B, C, R> combineScreenStates(
    a: Flow<ScreenState<A>>,
    b: Flow<ScreenState<B>>,
    c: Flow<ScreenState<C>>,
    transform: (A, B, C) -> R,
): Flow<ScreenState<R>> = combine(a, b, c) { sa, sb, sc ->
    @Suppress("UNCHECKED_CAST")
    resolveScreenStates(listOf(sa, sb, sc)) {
        transform(
            (sa as ScreenState.Content<A>).data,
            (sb as ScreenState.Content<B>).data,
            (sc as ScreenState.Content<C>).data,
        )
    }
}

/**
 * Combines four independent [ScreenState] flows. See [combineScreenStates] for priority rules.
 */
@OptIn(ExperimentalTime::class)
fun <A, B, C, D, R> combineScreenStates(
    a: Flow<ScreenState<A>>,
    b: Flow<ScreenState<B>>,
    c: Flow<ScreenState<C>>,
    d: Flow<ScreenState<D>>,
    transform: (A, B, C, D) -> R,
): Flow<ScreenState<R>> = combine(a, b, c, d) { sa, sb, sc, sd ->
    @Suppress("UNCHECKED_CAST")
    resolveScreenStates(listOf(sa, sb, sc, sd)) {
        transform(
            (sa as ScreenState.Content<A>).data,
            (sb as ScreenState.Content<B>).data,
            (sc as ScreenState.Content<C>).data,
            (sd as ScreenState.Content<D>).data,
        )
    }
}

/**
 * Combines five independent [ScreenState] flows. See [combineScreenStates] for priority rules.
 */
@OptIn(ExperimentalTime::class)
fun <A, B, C, D, E, R> combineScreenStates(
    a: Flow<ScreenState<A>>,
    b: Flow<ScreenState<B>>,
    c: Flow<ScreenState<C>>,
    d: Flow<ScreenState<D>>,
    e: Flow<ScreenState<E>>,
    transform: (A, B, C, D, E) -> R,
): Flow<ScreenState<R>> = combine(a, b, c, d, e) { sa, sb, sc, sd, se ->
    @Suppress("UNCHECKED_CAST")
    resolveScreenStates(listOf(sa, sb, sc, sd, se)) {
        transform(
            (sa as ScreenState.Content<A>).data,
            (sb as ScreenState.Content<B>).data,
            (sc as ScreenState.Content<C>).data,
            (sd as ScreenState.Content<D>).data,
            (se as ScreenState.Content<E>).data,
        )
    }
}

/**
 * Combines N independent [ScreenState] flows (vararg form). The resulting [ScreenState.Content]
 * holds a `List<Any?>` of the per-source `Content.data` values in input order.
 *
 * Priority rules + freshness aggregation are identical to the typed 2-5 arity overloads.
 *
 * Use this when the source count is large enough (or dynamic) to make a typed overload
 * impractical. Most dashboards stay below 5 sources and should prefer the typed overloads
 * for better compile-time safety on the transform lambda.
 *
 * ```kotlin
 * val combined: Flow<ScreenState<List<Any?>>> = combineScreenStates(
 *     accountStream, transactionStream, ratesStream, billsStream, loansStream, alertsStream,
 * )
 * ```
 */
@OptIn(ExperimentalTime::class)
fun combineScreenStates(
    vararg flows: Flow<ScreenState<*>>,
): Flow<ScreenState<List<Any?>>> = combineScreenStates(flows.toList())

/**
 * Combines N independent [ScreenState] flows (list form). Mirror of the vararg overload —
 * convenient when callers already have a `List<Flow<ScreenState<*>>>` (e.g. a dynamic
 * collection built per-screen).
 *
 * An empty input list emits a single `ScreenState.Content(emptyList())`
 * — a degenerate but well-defined result that lets `combineScreenStates(emptyList())` plug
 * into existing pipelines without special-casing.
 */
@OptIn(ExperimentalTime::class)
fun combineScreenStates(
    flows: List<Flow<ScreenState<*>>>,
): Flow<ScreenState<List<Any?>>> {
    if (flows.isEmpty()) {
        return flowOf(ScreenState.Content(emptyList()))
    }
    return combine(flows) { statesArray ->
        val states = statesArray.toList()
        @Suppress("UNCHECKED_CAST")
        resolveScreenStates(states) {
            states.map { (it as ScreenState.Content<Any?>).data }
        }
    }
}

/**
 * Resolves N [ScreenState]s to a single result, applying the standard priority rule.
 *
 * Priority: NoNetwork > Loading > Unauthenticated > Error > Empty > Content
 * Freshness: worst across all Content sources (STALE > UPDATING > FRESH).
 * fetchedAt: null if any Content source has null fetchedAt; otherwise the minimum (oldest).
 */
@OptIn(ExperimentalTime::class)
private fun <R> resolveScreenStates(
    states: List<ScreenState<*>>,
    buildResult: () -> R,
): ScreenState<R> {
    val contents = states.filterIsInstance<ScreenState.Content<*>>()
    // Worst-of band aggregation across all Content sources. Any source still refreshing
    // surfaces isRefreshing = true on the combined signal so the refreshing banner shows.
    val combinedIsRefreshing = contents.any { it.freshnessSignal.isRefreshing }
    val worstBand = contents.maxOfOrNull { it.freshnessSignal.band.severityOrdinal() } ?: 0
    val combinedFetchedAt: Instant? = if (contents.any { it.fetchedAt == null }) {
        null
    } else {
        contents.mapNotNull { it.fetchedAt }.minOrNull()
    }
    val combinedSignal = if (contents.isEmpty()) {
        FreshnessSignal.initial()
    } else {
        contents.first().freshnessSignal.copy(
            band = bandFromOrdinal(worstBand),
            isRefreshing = combinedIsRefreshing,
        )
    }

    return when {
        states.any { it is ScreenState.NoNetwork } -> ScreenState.NoNetwork()
        states.any { it is ScreenState.Loading } -> ScreenState.Loading
        states.any { it is ScreenState.Unauthenticated } -> ScreenState.Unauthenticated
        states.any { it is ScreenState.Error } -> {
            val err = states.first { it is ScreenState.Error } as ScreenState.Error
            ScreenState.Error(err.error, err.isNetworkError)
        }
        states.any { it is ScreenState.Empty } -> ScreenState.Empty
        states.size == contents.size ->
            ScreenState.Content(buildResult(), combinedFetchedAt, combinedSignal)
        else -> ScreenState.Loading
    }
}

private fun FreshnessBand.severityOrdinal(): Int = when (this) {
    FreshnessBand.Initial -> 0
    FreshnessBand.Fresh -> 1
    FreshnessBand.Stale -> 2
    FreshnessBand.VeryStale -> 3
}

private fun bandFromOrdinal(ord: Int): FreshnessBand = when (ord) {
    0 -> FreshnessBand.Initial
    1 -> FreshnessBand.Fresh
    2 -> FreshnessBand.Stale
    else -> FreshnessBand.VeryStale
}
