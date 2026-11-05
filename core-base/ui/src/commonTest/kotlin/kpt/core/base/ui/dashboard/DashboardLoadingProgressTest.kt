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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pure-data contract tests for [aggregateDashboardProgress] + [toDashboardProgressState].
 * Compose UI tests for [DashboardProgressBar] / [IndependentCardLayout] land separately
 * once `compose.uiTest` is wired into core-base/ui's commonTest source set.
 */
class DashboardLoadingProgressTest {

    @Test
    fun emptyList_yieldsZeroProgress() = runTest {
        val result = flowOf(emptyList<ScreenState<Any>>()).aggregateDashboardProgress().first()
        assertEquals(
            DashboardProgressState(
                loaded = 0,
                total = 0,
                isAnyLoading = false,
                hasAnyError = false,
                hasAnyEmpty = false,
            ),
            result,
        )
    }

    @Test
    fun allLoading_marksAnyLoadingTrue_andZeroLoaded() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Loading,
            ScreenState.Loading,
            ScreenState.Loading,
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(0, result.loaded)
        assertEquals(3, result.total)
        assertTrue(result.isAnyLoading)
        assertFalse(result.hasAnyError)
        assertFalse(result.hasAnyEmpty)
    }

    @Test
    fun mixedContentAndLoading_countsLoadedOnly() = runTest {
        val states: List<ScreenState<String>> = listOf(
            ScreenState.Content(data = "a"),
            ScreenState.Loading,
            ScreenState.Content(data = "b"),
            ScreenState.Loading,
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(2, result.loaded)
        assertEquals(4, result.total)
        assertTrue(result.isAnyLoading)
        assertFalse(result.hasAnyError)
    }

    @Test
    fun allContent_marksIsAnyLoadingFalse_andAllLoaded() = runTest {
        val states: List<ScreenState<Int>> = listOf(
            ScreenState.Content(data = 1),
            ScreenState.Content(data = 2),
            ScreenState.Content(data = 3, freshnessSignal = FreshnessSignal.initial().copy(isRefreshing = true)),
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(3, result.loaded)
        assertEquals(3, result.total)
        assertFalse(result.isAnyLoading)
        assertFalse(result.hasAnyError)
        assertFalse(result.hasAnyEmpty)
    }

    @Test
    fun anyError_marksHasAnyErrorTrue() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Content(data = "ok"),
            ScreenState.Error(error = RuntimeException("boom")),
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(1, result.loaded)
        assertEquals(2, result.total)
        assertTrue(result.hasAnyError)
    }

    @Test
    fun noNetwork_alsoCountsAsError() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.NoNetwork(isCaptivePortal = false),
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(0, result.loaded)
        assertEquals(1, result.total)
        assertTrue(result.hasAnyError, "NoNetwork is a failure-to-load — must roll up into hasAnyError.")
    }

    @Test
    fun anyEmpty_marksHasAnyEmptyTrue() = runTest {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Content(data = "ok"),
            ScreenState.Empty,
        )
        val result = flowOf(states).aggregateDashboardProgress().first()
        assertEquals(1, result.loaded)
        assertEquals(2, result.total)
        assertTrue(result.hasAnyEmpty)
        assertFalse(result.hasAnyError)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    @Test
    fun oldestFetchedAt_isStalestContentTimestamp() {
        val newer = kotlin.time.Instant.fromEpochSeconds(2000)
        val older = kotlin.time.Instant.fromEpochSeconds(1000)
        val states: List<ScreenState<String>> = listOf(
            ScreenState.Content(data = "a", fetchedAt = newer),
            ScreenState.Loading,
            ScreenState.Content(data = "b", fetchedAt = older),
        )
        assertEquals(
            older,
            states.toDashboardProgressState().oldestFetchedAt,
            "oldestFetchedAt is the stalest (min) Content fetchedAt across cards.",
        )
    }

    @Test
    fun oldestFetchedAt_nullWhenNoContentCarriesTimestamp() {
        val states: List<ScreenState<String>> = listOf(
            ScreenState.Loading,
            ScreenState.Content(data = "x"),
        )
        assertEquals(
            null,
            states.toDashboardProgressState().oldestFetchedAt,
            "With no fetchedAt on any card, the strip has no age to show and falls back to Loading.",
        )
    }

    @Test
    fun pureConversion_matchesFlowExtension() {
        val states: List<ScreenState<Any>> = listOf(
            ScreenState.Loading,
            ScreenState.Content(data = "x"),
            ScreenState.Empty,
            ScreenState.Error(error = IllegalStateException()),
        )
        val result = states.toDashboardProgressState()
        assertEquals(1, result.loaded)
        assertEquals(4, result.total)
        assertTrue(result.isAnyLoading)
        assertTrue(result.hasAnyEmpty)
        assertTrue(result.hasAnyError)
    }
}
