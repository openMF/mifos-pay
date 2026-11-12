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

import kpt.core.base.store.freshness.FreshnessSignal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Unified UI state produced by [ScreenDataStream].
 * Replaces per-ViewModel ScreenUiState + isFromCache + isRefreshing + networkStatus.
 */
sealed interface ScreenState<out T> {

    /** Initial load — no data available yet. */
    data object Loading : ScreenState<Nothing>

    /** Data fetched successfully but content is empty (e.g., empty list). */
    data object Empty : ScreenState<Nothing>

    /**
     * No network connectivity and no cached data available.
     * @param isCaptivePortal true when behind a captive portal (hotel WiFi login).
     */
    data class NoNetwork(
        val isCaptivePortal: Boolean = false,
    ) : ScreenState<Nothing>

    /**
     * Authentication required — HTTP 401/403 or equivalent token expiry.
     * Screens should redirect the user to the login flow.
     */
    data object Unauthenticated : ScreenState<Nothing>

    /** Error occurred with no usable cached data. */
    data class Error(
        /** The failure that produced this state. */
        val error: Throwable,
        /**
         * Whether it was a connectivity failure. Kept separate because offline has a different remedy than a server
         * error, and a retry button that cannot succeed is worse than none.
         */
        val isNetworkError: Boolean = false,
    ) : ScreenState<Nothing>

    /**
     * Data available with freshness metadata.
     * @param data The domain data payload.
     * @param fetchedAt Wall-clock instant of last successful fetch. Also exposed
     *   via [freshnessSignal]`.lastSyncedAt`; kept here for legacy display call sites.
     * @param freshnessSignal Single per-card signal carrying staleness band
     *   (time-based) AND request state (`isRefreshing`) AND error context. ViewModels
     *   normally read this from the sibling `ScreenDataStream.freshness` Flow rather
     *   than from this field. Drives the `FreshnessIndicator` info icon UI and the
     *   `refreshingIndicator` slot visibility check on `ScreenContent`.
     */
    @OptIn(ExperimentalTime::class)
    data class Content<T>(
        val data: T,
        val fetchedAt: Instant? = null,
        val freshnessSignal: FreshnessSignal = FreshnessSignal.initial(),
    ) : ScreenState<T>
}
