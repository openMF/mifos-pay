/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.freshness

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Pure-staleness signal carried by [kpt.core.base.store.screen.ScreenDataStream.freshness]
 * alongside `state`. Decouples cache age from network connectivity.
 *
 * UI maps `band` to a per-card freshness indicator (icon + tooltip); the surrounding
 * `lastSyncedAt` / `ttl` / `lastError` fields are exposed so tooltips can humanize the
 * age and categorize the error message.
 *
 * Construction is normally indirect via [kpt.core.base.store.infra.DecisionEngine.decideFreshness]
 * or the sibling Flow on `ScreenDataStream`. Tests + initial StateFlow values use [initial].
 */
@OptIn(ExperimentalTime::class)
data class FreshnessSignal(
    /** When this value last came from the source of truth, or null if it never has. */
    val lastSyncedAt: Instant?,
    /** How long the value is considered fresh before it is shown as stale. */
    val ttl: Duration,
    /**
     * The most recent refresh failure, if the last attempt failed. Present alongside data, because stale content plus
     * a failure is a different state than no content.
     */
    val lastError: Throwable?,
    /** The derived band the UI renders — computed from [lastSyncedAt] against [ttl]. */
    val band: FreshnessBand,
    /**
     * Request-state — true iff a background network refresh is currently in flight
     * for this card. Drives the `refreshingIndicator` slot on `ScreenContent`.
     * Replaces the deprecated `DataFreshness.UPDATING` enum value as the single
     * "currently-refreshing" signal. Kept separate from [band] because staleness
     * (time-based, derived from `lastSyncedAt` vs `ttl`) and request state
     * (lifecycle, derived from Store5 `isRefreshing`) are different concerns.
     */
    val isRefreshing: Boolean = false,
) {
    /** Signal presets. */
    companion object {
        /**
         * Convenience factory for the "never synced, no error" initial state.
         * Used as the initial value for `StateFlow<FreshnessSignal>` in ViewModels and
         * as the default in `ScreenState.Content.freshnessSignal`.
         *
         * The `ttl` argument has no effect on the resulting band (which is always
         * [FreshnessBand.Initial]); it defaults to 24 hours so the resulting signal
         * is a safe fallback if a consumer later reads it without a real TTL bound.
         */
        fun initial(ttl: Duration = 24.hours): FreshnessSignal = FreshnessSignal(
            lastSyncedAt = null,
            ttl = ttl,
            lastError = null,
            band = FreshnessBand.Initial,
            isRefreshing = false,
        )
    }
}
