/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Unified UI state produced by fork repositories (and, once Phase 4 lands, by
 * `Store<Key, Output>.asScreenStream(...)`). Replaces the legacy
 * [DataState] sealed class as the single fork-wide read-side model.
 *
 * Mirrors the template's `kpt.core.base.store.screen.ScreenState` — SAME six
 * variants, SAME semantics — with two intentional simplifications for the
 * Phase-3 transitional window:
 *  - `Content.freshnessSignal` is OMITTED (the freshness engine + FreshnessBands
 *    machinery arrive with Phase 4's Store5 infra).
 *  - `NoNetwork.isCaptivePortal` defaults `false`; captive-portal detection
 *    ships with the cmp-network-monitor bridge in Phase 4.
 *
 * The six-branch surface (`Loading | Empty | NoNetwork | Unauthenticated |
 * Error | Content`) is fixed and MUST match the template so that Phase-4's
 * `ScreenContent` wrapper can consume fork ScreenStates unchanged.
 *
 * Post-migration NOTE: Kotlin does NOT support nested-type access through a
 * typealias, which prevents collapsing this into a single canonical type via
 * `typealias ScreenState<T> = kpt.core.base.store.screen.ScreenState<T>`
 * (callers use `ScreenState.Content(...)` etc. — that access resolves against
 * the class identifier, not through aliases). So we keep the fork sealed
 * interface as the canonical fork-side identifier and provide two bridges:
 *  - [toForkScreenState] converts `kpt.ScreenState<T>` → fork `ScreenState<T>`
 *    (used by Store5 impls to satisfy fork repository interface contracts).
 *  - [Flow<kpt.ScreenState<T>>.toForkScreenStateFlow] Flow-level convenience.
 */
sealed interface ScreenState<out T> {

    /** Initial load — no data available yet. */
    data object Loading : ScreenState<Nothing>

    /** Data fetched successfully but content is empty (e.g., empty list). */
    data object Empty : ScreenState<Nothing>

    /**
     * No network connectivity and no cached data available.
     * @param isCaptivePortal true when behind a captive portal (hotel WiFi login).
     *   Always `false` in Phase-3 (no cmp-network-monitor bridge yet).
     */
    data class NoNetwork(
        val isCaptivePortal: Boolean = false,
    ) : ScreenState<Nothing>

    /**
     * Authentication required — HTTP 401/403 or equivalent token expiry.
     * Screens should redirect the user to the login flow.
     */
    data object Unauthenticated : ScreenState<Nothing>

    /**
     * Error occurred with no usable cached data.
     * @param error The underlying throwable (typically a [HttpStatusException],
     *   [NetworkException], or fork-mapped exception from [MifosErrorMapper]).
     * @param isNetworkError set to `true` when the underlying cause is an
     *   [IOException] or [NetworkException] that has been surfaced as
     *   [Error] rather than [NoNetwork] (e.g. a request that reached the
     *   server but the response body couldn't be read).
     */
    data class Error(
        val error: Throwable,
        val isNetworkError: Boolean = false,
    ) : ScreenState<Nothing>

    /**
     * Data available.
     * @param data The domain data payload.
     * @param fetchedAt Wall-clock instant of last successful fetch. Nullable in
     *   Phase-3 because the fork has no `FetchedAtRepository` wired yet;
     *   Phase-4 populates this from Store5 metadata.
     */
    @OptIn(ExperimentalTime::class)
    data class Content<T>(
        val data: T,
        val fetchedAt: Instant? = null,
    ) : ScreenState<T>
}

// -----------------------------------------------------------------------------
// Small helpers used by consumers migrating off DataState — mirror the subset
// of the template's `ScreenStateExtensions` that Phase-3 repos actually need.
// The template's full FreshnessSignal-carrying variants land with Phase-4.
// -----------------------------------------------------------------------------

/** Extracts data from [ScreenState.Content] or null for other states. */
val <T> ScreenState<T>.dataOrNull: T?
    get() = (this as? ScreenState.Content<T>)?.data

/** `true` when the underlying state is one of the terminal loaded variants. */
val ScreenState<*>.isTerminal: Boolean
    get() = this !is ScreenState.Loading

/** `true` when the underlying state carries no data (any error/loading/empty variant). */
val ScreenState<*>.hasData: Boolean
    get() = this is ScreenState.Content<*>

/** Extract the wrapped throwable when the state is [ScreenState.Error]. */
val ScreenState<*>.errorOrNull: Throwable?
    get() = (this as? ScreenState.Error)?.error

/**
 * Transforms only [ScreenState.Content.data], passing through all other
 * states unchanged. Mirrors the template's `mapContent { data, freshness -> }`
 * shape but drops the freshness argument (unavailable pre-Phase-4).
 */
inline fun <T, R> ScreenState<T>.map(transform: (T) -> R): ScreenState<R> = when (this) {
    is ScreenState.Content<T> -> ScreenState.Content(
        data = transform(data),
        fetchedAt = fetchedAt,
    )
    is ScreenState.Loading -> ScreenState.Loading
    is ScreenState.Empty -> ScreenState.Empty
    is ScreenState.NoNetwork -> this
    is ScreenState.Error -> this
    is ScreenState.Unauthenticated -> ScreenState.Unauthenticated
}
