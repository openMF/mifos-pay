/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.base.store.freshness.FreshnessBands
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.DataOrigin
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.StoreData
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Pure function combining StoreData metadata + NetworkStatus into ScreenState.
 * No side effects, no coroutines — exhaustively unit testable.
 *
 * Uses full [NetworkStatus] (not just Boolean) to detect captive portals.
 * Error classification delegates to [categorize] — single source of truth.
 */
object DecisionEngine {

    /**
     * Maps [storeData] + [networkStatus] to the correct [ScreenState].
     *
     * @param storeData snapshot from the Store pipeline (data, error, freshness flags)
     * @param networkStatus current connectivity; [NetworkStatus.CaptivePortal] is treated
     *   as offline for content decisions but surfaces a distinct UI flag
     * @param fetchPolicy the policy that drove the upstream Store5 request shape. Consumed for
     *   ONE terminal-emptiness decision: a [FetchPolicy.CACHE_ONLY] (offline-local, no fetcher)
     *   store maps its "no data" state to [ScreenState.Empty] rather than [ScreenState.Loading],
     *   because there is no network fetch that could ever fill it. Every other mapping is a pure
     *   function of `(storeData, networkStatus)`; remaining policy-specific behaviour materialises
     *   at the stream layer (see `streamDataForPolicy` in `StoreDataExtensions.kt`).
     * @return the [ScreenState] variant the screen should render
     */
    fun <T> decide(
        storeData: StoreData<T>,
        networkStatus: NetworkStatus,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    ): ScreenState<T> {
        val noData = storeData.isEmpty
        val error = storeData.error
        val isOnline = networkStatus is NetworkStatus.Available
        val isCaptivePortal = networkStatus is NetworkStatus.CaptivePortal

        // === No data branch === (extracted to decideNoData/decideError so each function stays
        // within the detekt CyclomaticComplexMethod threshold; behaviour is identical to the
        // inlined when. See those helpers for the full offline-local / offline-first / error rules.)
        // An EMPTY collection read from the cache, alongside a fetch error, carries no information
        // but the error. It is what an unfilled table emits before its first fetch, and a repository
        // that (rightly) counts an empty list as real content passes no `isEmpty` — so the failure
        // used to render as content: a 403 on a fresh install showed "No reports found" instead of
        // why. Measured on device 2026-10-24. A non-empty cache, or an empty list the NETWORK
        // returned, is real content and keeps the has-data branch.
        val emptyCacheBehindError =
            error != null && storeData.origin != DataOrigin.NETWORK && storeData.data.isEmptyCollection()
        if (noData || emptyCacheBehindError) return decideNoData(fetchPolicy, error, isOnline, isCaptivePortal)

        // === Has data branch ===
        // Phase A of data-freshness-redesign epic (2026-06-17): network state no longer
        // conflated into DataFreshness on the Content path. Offline / captive-portal /
        // error are surfaced separately:
        //  - Network connectivity → global ConnectivityBanner (already shipped)
        //  - Per-card staleness    → FreshnessSignal sibling Flow (decideFreshness())
        // Here we just record request state: UPDATING during in-flight network refresh
        // (legitimate request signal — NOT network state), FRESH otherwise.
        val fetchedAt = storeData.fetchedAtInstant

        @Suppress("UNUSED_VARIABLE")
        val networkStateNoLongerConsumedHere = isOnline || isCaptivePortal || (error != null)
        // freshnessSignal default (Initial band) is overwritten by ScreenDataStream's
        // sibling Flow which computes the real signal via decideFreshness(). Here we
        // just bake in isRefreshing so the refreshing-banner visibility check works.
        return ScreenState.Content(
            data = storeData.data,
            fetchedAt = fetchedAt,
            freshnessSignal = FreshnessSignal.initial().copy(isRefreshing = storeData.isRefreshing),
        )
    }

    /**
     * No-data decision, split out of [decide] to keep each function within the detekt
     * CyclomaticComplexMethod threshold. Precedence: offline-local terminal-empty → captive portal →
     * offline-first empty → offline → error category → still-fetching Loading. Behaviour is identical
     * to the previously-inlined `when`.
     */
    private fun <T> decideNoData(
        fetchPolicy: FetchPolicy,
        error: Throwable?,
        isOnline: Boolean,
        isCaptivePortal: Boolean,
    ): ScreenState<T> = when {
        // Offline-local ([CACHE_ONLY]) with no DB error → terminal Empty (no fetcher can ever fill it).
        fetchPolicy == FetchPolicy.CACHE_ONLY && error == null -> ScreenState.Empty
        isCaptivePortal -> ScreenState.NoNetwork(isCaptivePortal = true)
        // OFFLINE-FIRST — a cache-first ([CACHE_FIRST_SWR], asScreenStream's default) screen with no
        // cached data offline surfaces its own Empty state, not a blocking full-screen NoNetwork.
        // Other policies (NETWORK_WITH_CACHE / NETWORK_ONLY) still show NoNetwork offline; the
        // reconnect trigger re-runs this decision once connectivity returns.
        //
        // A NETWORK-category error does not disqualify Empty here. The guard used to be
        // `error == null`, on the assumption that offline CACHE_FIRST_SWR never reaches a fetcher —
        // but Store5's `cached(key, refresh = false)` DOES invoke the fetcher when nothing is
        // cached (`refresh = false` means "don't refetch when data exists", not "never fetch").
        // So an empty cache offline reliably produces a connection failure, and whether that error
        // or the empty emission arrived first decided the screen: Empty on a fast machine,
        // NoNetwork on a slow one. That is the very blocking-NoNetwork this rule exists to prevent,
        // and it was reproducible as a flaky test rather than as the user-facing bug it is.
        //
        // Offline, a network error carries no information — it is the expected outcome of a doomed
        // attempt, so it must not change the verdict. A NON-network error (a serialization or
        // mapping fault that happens to surface offline) is real signal and still falls through to
        // [decideError].
        !isOnline &&
            fetchPolicy == FetchPolicy.CACHE_FIRST_SWR &&
            (error == null || error.isExpectedWhenOffline()) -> ScreenState.Empty
        !isOnline -> ScreenState.NoNetwork()
        error != null -> decideError(error)
        else -> ScreenState.Loading
    }

    /**
     * Whether [this] is the kind of failure that is GUARANTEED offline and therefore says nothing
     * about the screen's data.
     *
     * Deliberately narrow: only the transport categories. An auth or server error cannot legitimately
     * originate from an offline device, so if one appears it is real signal and must still surface.
     */
    private fun Throwable.isExpectedWhenOffline(): Boolean = when (categorize(this)) {
        ErrorCategory.Network,
        ErrorCategory.Timeout.Connect,
        ErrorCategory.Timeout.Read,
        -> true
        else -> false
    }

    /** Error → ScreenState mapping, split out to keep [decideNoData] within the complexity threshold. */
    private fun decideError(error: Throwable): ScreenState<Nothing> = when (categorize(error)) {
        ErrorCategory.Network, ErrorCategory.Timeout.Connect, ErrorCategory.Timeout.Read ->
            ScreenState.NoNetwork()
        ErrorCategory.Auth -> ScreenState.Unauthenticated
        ErrorCategory.RateLimit,
        ErrorCategory.QuotaExceeded,
        ErrorCategory.Generic,
        is ErrorCategory.Server,
        is ErrorCategory.ClientError,
        -> ScreenState.Error(error, isNetworkError = false)
    }

    /**
     * Pure sibling function: maps [storeData] + [ttl] to a [FreshnessSignal].
     *
     * Decoupled from [decide]; runs in parallel and outputs the per-card freshness
     * signal consumed by `FreshnessIndicator`. `networkStatus` is accepted for
     * API symmetry with [decide] but **deliberately ignored** — freshness is purely
     * time-relative + last-error-aware. Network connectivity is rendered separately
     * by `ConnectivityBanner`.
     *
     * @param storeData snapshot from the Store pipeline (uses `fetchedAtInstant` + `error`)
     * @param networkStatus accepted for API symmetry; NOT read
     * @param ttl per-store TTL bound (from `AppStoreRegistry.Ttl`); above this age the
     *   band degrades from Fresh → Stale → VeryStale
     */
    @OptIn(ExperimentalTime::class)
    fun <T> decideFreshness(
        storeData: StoreData<T>,
        @Suppress("UNUSED_PARAMETER")
        networkStatus: NetworkStatus,
        ttl: Duration,
    ): FreshnessSignal {
        val now = Clock.System.now()
        val lastSyncedAt = storeData.fetchedAtInstant
        val lastError = storeData.error
        val band = FreshnessBands.bandFor(
            now = now,
            lastSyncedAt = lastSyncedAt,
            ttl = ttl,
            lastError = lastError,
        )
        return FreshnessSignal(
            lastSyncedAt = lastSyncedAt,
            ttl = ttl,
            lastError = lastError,
            band = band,
        )
    }
}

private fun Any?.isEmptyCollection(): Boolean = when (this) {
    is Collection<*> -> isEmpty()
    is Map<*, *> -> isEmpty()
    else -> false
}
