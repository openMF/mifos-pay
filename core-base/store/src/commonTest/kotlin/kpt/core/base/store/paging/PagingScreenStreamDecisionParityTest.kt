/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.paging

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kpt.core.base.store.infra.DecisionEngine
import kpt.core.base.store.screen.DataOrigin
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.StoreData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Parity tests for the PagingScreenStream→DecisionEngine refactor (PLAN-fw-260504 §5).
 *
 * Asserts that wrapping paging state into a [StoreData] and feeding it through
 * [DecisionEngine.decide] produces the same [ScreenState] as the previous inline
 * `combine { when ... }` block in PagingScreenStream — for every supported scenario.
 *
 * One deliberate behavioral change is documented inline:
 * - Network-shaped error + items empty + online: old paging returned Error(err);
 *   new path returns NoNetwork() because DecisionEngine recognizes IOException-class
 *   throwables. This is an improvement (consistent with single-key flow); test
 *   asserts the NEW behavior.
 */
class PagingScreenStreamDecisionParityTest {

    private val networkInfo = NetworkInfo(type = NetworkType.WiFi, isMetered = false)
    private val available = NetworkStatus.Available(networkInfo)
    private val unavailable = NetworkStatus.Unavailable
    private val captive = NetworkStatus.CaptivePortal(networkInfo)

    // ── Helper: simulate the new combine block's transform ────────────────

    private fun decideForPaging(
        items: List<String>,
        status: NetworkStatus,
        isInitialLoading: Boolean,
        error: Throwable? = null,
    ): ScreenState<List<String>> {
        if (items.isEmpty() && !isInitialLoading && error == null) {
            return ScreenState.Empty
        }
        val storeData = StoreData(
            data = items,
            origin = DataOrigin.NETWORK,
            isRefreshing = isInitialLoading,
            fetchedAt = null,
            fetchedAtInstant = null,
            error = error,
            isEmpty = items.isEmpty(),
        )
        return DecisionEngine.decide(storeData, status)
    }

    // ── Empty + Loading scenarios ────────────────────────────────────────

    @Test
    fun loading_empty_offline_returnsNoNetwork() {
        val result = decideForPaging(emptyList(), unavailable, isInitialLoading = true)
        assertIs<ScreenState.NoNetwork>(result)
        assertEquals(false, result.isCaptivePortal)
    }

    @Test
    fun loading_empty_captive_returnsNoNetworkCaptive() {
        val result = decideForPaging(emptyList(), captive, isInitialLoading = true)
        assertIs<ScreenState.NoNetwork>(result)
        assertTrue(result.isCaptivePortal)
    }

    @Test
    fun loading_empty_online_returnsLoading() {
        val result = decideForPaging(emptyList(), available, isInitialLoading = true)
        assertIs<ScreenState.Loading>(result)
    }

    // ── Error + empty scenarios ──────────────────────────────────────────

    @Test
    fun error_empty_offline_returnsNoNetwork() {
        val result = decideForPaging(
            emptyList(),
            unavailable,
            isInitialLoading = false,
            error = RuntimeException("any error"),
        )
        assertIs<ScreenState.NoNetwork>(result)
    }

    @Test
    fun error_empty_online_nonNetworkError_returnsError() {
        val err = IllegalStateException("server fault")
        val result = decideForPaging(
            emptyList(),
            available,
            isInitialLoading = false,
            error = err,
        )
        assertIs<ScreenState.Error>(result)
        assertEquals(err, result.error)
    }

    @Test
    fun error_empty_online_networkShapedError_returnsNoNetwork_intentionalImprovement() {
        // Behavioral change vs old paging combine: old returned Error(err) regardless
        // of error type; new path returns NoNetwork because DecisionEngine recognizes
        // IOException-class throwables. This makes paging consistent with single-key
        // flow (asScreenStream) and is the correct UX (network-shaped errors get
        // NoNetwork treatment, not generic "Something went wrong").
        val ioError = FakeIOExceptionForPaging("connection reset")
        val result = decideForPaging(
            emptyList(),
            available,
            isInitialLoading = false,
            error = ioError,
        )
        assertIs<ScreenState.NoNetwork>(result)
    }

    // ── Definitive empty (load completed, no items, no error) ─────────────

    @Test
    fun empty_loadCompleted_noError_returnsEmpty() {
        val result = decideForPaging(emptyList(), available, isInitialLoading = false)
        assertIs<ScreenState.Empty>(result)
    }

    // ── Has-data scenarios ───────────────────────────────────────────────

    @Test
    fun hasData_online_notRefreshing_returnsContentFresh() {
        val items = listOf("a", "b")
        val result = decideForPaging(items, available, isInitialLoading = false)
        assertIs<ScreenState.Content<*>>(result)
        assertEquals(items, result.data)
        assertFalse(result.freshnessSignal.isRefreshing, "Expected not refreshing")
    }

    @Test
    fun hasData_offline_returnsContent() {
        // Phase A of data-freshness-redesign (2026-06-17): paging Content path
        // no longer encodes network state — DecisionEngine returns Content with
        // isRefreshing=false regardless of online/offline. Network state lives in
        // ConnectivityBanner; per-card staleness in FreshnessSignal.band.
        val items = listOf("a", "b")
        val result = decideForPaging(items, unavailable, isInitialLoading = false)
        assertIs<ScreenState.Content<*>>(result)
        assertFalse(result.freshnessSignal.isRefreshing, "Content path not refreshing while offline")
    }

    @Test
    fun hasData_captive_returnsContent() {
        // Same as offline — captive portal is a connectivity concern, not staleness.
        val items = listOf("a")
        val result = decideForPaging(items, captive, isInitialLoading = false)
        assertIs<ScreenState.Content<*>>(result)
        assertFalse(result.freshnessSignal.isRefreshing, "Content path not refreshing on captive portal")
    }

    @Test
    fun hasData_refreshing_returnsContentUpdating() {
        val items = listOf("a")
        val result = decideForPaging(items, available, isInitialLoading = true)
        assertIs<ScreenState.Content<*>>(result)
        assertTrue(result.freshnessSignal.isRefreshing, "Expected refreshing")
    }

    @Test
    fun hasData_refreshing_offline_returnsContentUpdating_refreshingWins() {
        // Matches DecisionEngine and old paging behavior: refreshing trumps offline
        // when content is present.
        val items = listOf("a")
        val result = decideForPaging(items, unavailable, isInitialLoading = true)
        assertIs<ScreenState.Content<*>>(result)
        assertTrue(result.freshnessSignal.isRefreshing, "Expected refreshing")
    }

    @Test
    fun hasData_errorAfterRefresh_returnsContent() {
        // Phase A: error after refresh no longer maps to STALE on the Content path.
        // The error is surfaced via FreshnessSignal.lastError for the per-card
        // FreshnessIndicator; Content.isRefreshing reflects request lifecycle only.
        val items = listOf("a")
        val result = decideForPaging(
            items,
            available,
            isInitialLoading = false,
            error = RuntimeException("refresh failed"),
        )
        assertIs<ScreenState.Content<*>>(result)
        assertFalse(result.freshnessSignal.isRefreshing, "Content path not refreshing after error")
    }
}

/** Throwable whose simpleName contains "IOException" — triggers DecisionEngine's network-error heuristic. */
private class FakeIOExceptionForPaging(message: String) : Exception(message)
