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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessSignal
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [skipMemoryData] — verifies it bypasses in-memory cache and reads from
 * the Fetcher (source-of-truth / network) instead.
 */
class StoreDataExtensionsTest {

    // ─── skipMemoryData ───────────────────────────────────────────────────────

    @Test
    fun `skipMemoryData emits data from fetcher`() = runTest {
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { key ->
                    fetchCount++
                    "fetched:$key"
                },
            )
            .build()

        // skipMemoryData on an unprimed key must reach the fetcher (no cached data to fall back to).
        // For memory-only stores (no SourceOfTruth), skipMemory on a primed key falls back to the
        // in-memory cache without a new fetch — only testable with a SOT-backed store.
        store.skipMemoryData("key1").test {
            // First emission may be an empty sentinel (fetch in progress); drain to actual data.
            // Receiving real data guarantees the fetcher has been called.
            var item = awaitItem()
            if (item.isEmpty) item = awaitItem()
            assertFalse(item.isEmpty, "skipMemoryData must eventually return data from the fetcher")
            assertEquals("fetched:key1", item.data)
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(fetchCount > 0, "skipMemoryData must call the fetcher for an unprimed key")
    }

    @Test
    fun `skipMemoryData with refresh=false returns cached data without new fetch`() = runTest {
        // The invariant — refresh=false serves cached data without a network call — is only
        // well-defined for a SourceOfTruth-backed store. Use one (mirrors LoadPageRefreshSemanticsTest)
        // and seed the SoT directly, so the assertion is deterministic and never depends on the timing
        // of an async fetch write-through or on memory-cache eviction (the prior memory-only version
        // raced under coverage instrumentation).
        val sotData = mutableMapOf<String, String>()
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, String, String>(
                fetcher = Fetcher.of { key ->
                    fetchCount++
                    "fetched:$key"
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { key -> flow { emit(sotData[key]) } },
                    writer = { key, value -> sotData[key] = value },
                ),
            )
            .build()

        // Seed the SoT directly so refresh=false has cached data to short-circuit on.
        sotData["key"] = "fetched:key"

        // refresh=false serves the SoT value; it must NOT fall through to the fetcher.
        store.skipMemoryData("key", refresh = false).test {
            var item = awaitItem()
            if (item.isEmpty) item = awaitItem()
            assertEquals("fetched:key", item.data)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, fetchCount, "skipMemoryData(refresh=false) must serve SoT-cached data without a network fetch")
    }

    // ─── combineScreenStates priority ────────────────────────────────────────

    @Test
    fun `combineScreenStates both Content combines data`() = runTest {
        val a = kotlinx.coroutines.flow.flowOf(ScreenState.Content("hello"))
        val b = kotlinx.coroutines.flow.flowOf(ScreenState.Content(42))

        combineScreenStates(a, b) { s, n -> "$s:$n" }.test {
            val item = assertIs<ScreenState.Content<String>>(awaitItem())
            assertEquals("hello:42", item.data)
            // Default initial signal — not refreshing.
            assertEquals(false, item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `combineScreenStates NoNetwork wins over Content`() = runTest {
        val a = kotlinx.coroutines.flow.flowOf<ScreenState<String>>(ScreenState.NoNetwork())
        val b = kotlinx.coroutines.flow.flowOf(ScreenState.Content("data"))

        combineScreenStates(a, b) { s, _ -> s }.test {
            assertIs<ScreenState.NoNetwork>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `combineScreenStates Loading wins over Content`() = runTest {
        val a = kotlinx.coroutines.flow.flowOf<ScreenState<String>>(ScreenState.Loading)
        val b = kotlinx.coroutines.flow.flowOf(ScreenState.Content("data"))

        combineScreenStates(a, b) { s, _ -> s }.test {
            assertIs<ScreenState.Loading>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `combineScreenStates STALE band from one source propagates to result`() = runTest {
        val staleSignal = FreshnessSignal.initial().copy(band = FreshnessBand.Stale)
        val a = kotlinx.coroutines.flow.flowOf(ScreenState.Content("a", freshnessSignal = staleSignal))
        val b = kotlinx.coroutines.flow.flowOf(ScreenState.Content("b"))

        combineScreenStates(a, b) { x, y -> x + y }.test {
            val item = assertIs<ScreenState.Content<String>>(awaitItem())
            assertEquals(FreshnessBand.Stale, item.freshnessSignal.band)
            awaitComplete()
        }
    }

    @Test
    fun `combineScreenStates isRefreshing propagates from any source`() = runTest {
        val refreshingSignal = FreshnessSignal.initial().copy(isRefreshing = true)
        val a = kotlinx.coroutines.flow.flowOf(ScreenState.Content("a", freshnessSignal = refreshingSignal))
        val b = kotlinx.coroutines.flow.flowOf(ScreenState.Content("b"))

        combineScreenStates(a, b) { x, y -> x + y }.test {
            val item = assertIs<ScreenState.Content<String>>(awaitItem())
            assertEquals(true, item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }
}
