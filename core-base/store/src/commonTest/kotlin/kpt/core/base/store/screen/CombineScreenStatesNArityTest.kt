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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessSignal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CombineScreenStatesNArityTest {

    // ─── 3-source ────────────────────────────────────────────────────────────

    @Test
    fun `3-source all Content FRESH produces Content FRESH`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
        ) { a, b, c -> Triple(a, b, c) }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Triple<Int, Int, Int>>>(item)
            assertEquals(Triple(1, 2, 3), item.data)
            assertEquals(FreshnessBand.Fresh, item.freshnessSignal.band)
            assertFalse(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `3-source any STALE produces STALE`() = runTest {
        combineScreenStates(
            content(1),
            content(2, band = FreshnessBand.Stale),
            content(3),
        ) { a, b, c -> a + b + c }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertEquals(FreshnessBand.Stale, item.freshnessSignal.band)
            awaitComplete()
        }
    }

    @Test
    fun `3-source any UPDATING with no STALE produces UPDATING`() = runTest {
        combineScreenStates(
            content(1),
            content(2, isRefreshing = true),
            content(3),
        ) { a, b, c -> a + b + c }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertTrue(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `3-source any NoNetwork produces NoNetwork`() = runTest {
        combineScreenStates(
            content(1),
            noNetwork<Int>(),
            content(3),
        ) { a, b, c -> a + b + c }.test {
            assertIs<ScreenState.NoNetwork>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `3-source any Loading produces Loading`() = runTest {
        combineScreenStates(
            content(1),
            loading<Int>(),
            content(3),
        ) { a, b, c -> a + b + c }.test {
            assertIs<ScreenState.Loading>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `3-source any Error produces Error`() = runTest {
        val err = RuntimeException("boom")
        combineScreenStates(
            content(1),
            error<Int>(err),
            content(3),
        ) { a, b, c -> a + b + c }.test {
            val item = awaitItem()
            assertIs<ScreenState.Error>(item)
            assertEquals("boom", item.error.message)
            awaitComplete()
        }
    }

    @Test
    fun `3-source any Empty produces Empty`() = runTest {
        combineScreenStates(
            content(1),
            empty<Int>(),
            content(3),
        ) { a, b, c -> a + b + c }.test {
            assertIs<ScreenState.Empty>(awaitItem())
            awaitComplete()
        }
    }

    // ─── 4-source ────────────────────────────────────────────────────────────

    @Test
    fun `4-source all Content FRESH produces Content FRESH`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
            content(4),
        ) { a, b, c, d -> a + b + c + d }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertEquals(10, item.data)
            assertEquals(FreshnessBand.Fresh, item.freshnessSignal.band)
            assertFalse(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `4-source STALE beats refreshing`() = runTest {
        combineScreenStates(
            content(1),
            content(2, isRefreshing = true),
            content(3, band = FreshnessBand.Stale),
            content(4),
        ) { a, b, c, d -> a + b + c + d }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            // Stale band aggregates worst-of; isRefreshing aggregates any-of.
            // Both must be reflected on the combined signal.
            assertEquals(FreshnessBand.Stale, item.freshnessSignal.band)
            assertTrue(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `4-source any NoNetwork produces NoNetwork`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            noNetwork<Int>(),
            content(4),
        ) { a, b, c, d -> a + b + c + d }.test {
            assertIs<ScreenState.NoNetwork>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `4-source NoNetwork beats Error`() = runTest {
        val err = RuntimeException("fail")
        combineScreenStates(
            error<Int>(err),
            noNetwork<Int>(),
            content(3),
            content(4),
        ) { a, b, c, d -> a + b + c + d }.test {
            assertIs<ScreenState.NoNetwork>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `4-source any Loading produces Loading`() = runTest {
        combineScreenStates(
            content(1),
            loading<Int>(),
            content(3),
            content(4),
        ) { a, b, c, d -> a + b + c + d }.test {
            assertIs<ScreenState.Loading>(awaitItem())
            awaitComplete()
        }
    }

    // ─── 5-source ────────────────────────────────────────────────────────────

    @Test
    fun `5-source all Content FRESH produces Content FRESH`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
            content(4),
            content(5),
        ) { a, b, c, d, e -> a + b + c + d + e }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertEquals(15, item.data)
            assertEquals(FreshnessBand.Fresh, item.freshnessSignal.band)
            assertFalse(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `5-source any STALE produces STALE`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3, band = FreshnessBand.Stale),
            content(4, isRefreshing = true),
            content(5),
        ) { a, b, c, d, e -> a + b + c + d + e }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertEquals(FreshnessBand.Stale, item.freshnessSignal.band)
            awaitComplete()
        }
    }

    @Test
    fun `5-source any NoNetwork produces NoNetwork`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
            noNetwork<Int>(),
            content(5),
        ) { a, b, c, d, e -> a + b + c + d + e }.test {
            assertIs<ScreenState.NoNetwork>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `5-source any Error produces first Error`() = runTest {
        val err1 = RuntimeException("first")
        val err2 = RuntimeException("second")
        combineScreenStates(
            content(1),
            error<Int>(err1),
            error<Int>(err2),
            content(4),
            content(5),
        ) { a, b, c, d, e -> a + b + c + d + e }.test {
            val item = awaitItem()
            assertIs<ScreenState.Error>(item)
            assertEquals("first", item.error.message)
            awaitComplete()
        }
    }

    @Test
    fun `5-source any Empty produces Empty`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            empty<Int>(),
            content(4),
            content(5),
        ) { a, b, c, d, e -> a + b + c + d + e }.test {
            assertIs<ScreenState.Empty>(awaitItem())
            awaitComplete()
        }
    }

    // ─── 2-source refactor parity ────────────────────────────────────────────

    @Test
    fun `2-source refactored still produces Content FRESH when both fresh`() = runTest {
        combineScreenStates(
            content(10),
            content(20),
        ) { a, b -> a + b }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertEquals(30, item.data)
            assertEquals(FreshnessBand.Fresh, item.freshnessSignal.band)
            assertFalse(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `2-source STALE wins over refreshing on band - isRefreshing also aggregated`() = runTest {
        combineScreenStates(
            content(10, band = FreshnessBand.Stale),
            content(20, isRefreshing = true),
        ) { a, b -> a + b }.test {
            val item = awaitItem()
            assertIs<ScreenState.Content<Int>>(item)
            assertEquals(FreshnessBand.Stale, item.freshnessSignal.band)
            assertTrue(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    // ─── N-arity vararg ──────────────────────────────────────────────────────

    @Test
    fun `vararg 6-source all Content FRESH produces Content with ordered list`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
            content(4),
            content(5),
            content(6),
        ).test {
            val item = awaitItem()
            assertIs<ScreenState.Content<List<Any?>>>(item)
            assertEquals(listOf(1, 2, 3, 4, 5, 6), item.data)
            assertEquals(FreshnessBand.Fresh, item.freshnessSignal.band)
            assertFalse(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `vararg 6-source any STALE produces Content STALE`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3, band = FreshnessBand.Stale),
            content(4),
            content(5),
            content(6),
        ).test {
            val item = awaitItem()
            assertIs<ScreenState.Content<List<Any?>>>(item)
            assertEquals(FreshnessBand.Stale, item.freshnessSignal.band)
            awaitComplete()
        }
    }

    @Test
    fun `vararg 6-source any NoNetwork produces NoNetwork`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
            noNetwork<Int>(),
            content(5),
            content(6),
        ).test {
            assertIs<ScreenState.NoNetwork>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `vararg 10-source all Content FRESH produces 10-element list`() = runTest {
        combineScreenStates(
            content(1),
            content(2),
            content(3),
            content(4),
            content(5),
            content(6),
            content(7),
            content(8),
            content(9),
            content(10),
        ).test {
            val item = awaitItem()
            assertIs<ScreenState.Content<List<Any?>>>(item)
            assertEquals(10, item.data.size)
            assertEquals((1..10).toList(), item.data)
            awaitComplete()
        }
    }

    // ─── N-arity list ────────────────────────────────────────────────────────

    @Test
    fun `list overload empty input emits Content with empty list`() = runTest {
        combineScreenStates(emptyList()).test {
            val item = awaitItem()
            assertIs<ScreenState.Content<List<Any?>>>(item)
            assertEquals(emptyList<Any?>(), item.data)
            // Empty-input Content uses default FreshnessSignal.initial() — band=Initial,
            // not Fresh. There are no source Contents to derive a band from.
            assertEquals(FreshnessBand.Initial, item.freshnessSignal.band)
            assertFalse(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun `list overload single source matches Content`() = runTest {
        combineScreenStates(listOf(content("alpha"))).test {
            val item = awaitItem()
            assertIs<ScreenState.Content<List<Any?>>>(item)
            assertEquals(listOf<Any?>("alpha"), item.data)
            awaitComplete()
        }
    }

    @Test
    fun `list overload aggregates 4 sources with UPDATING worst-case`() = runTest {
        val flows: List<kotlinx.coroutines.flow.Flow<ScreenState<*>>> = listOf(
            content(1),
            content(2, isRefreshing = true),
            content(3),
            content(4),
        )
        combineScreenStates(flows).test {
            val item = awaitItem()
            assertIs<ScreenState.Content<List<Any?>>>(item)
            assertEquals(listOf<Any?>(1, 2, 3, 4), item.data)
            assertTrue(item.freshnessSignal.isRefreshing)
            awaitComplete()
        }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    /**
     * Test helper for `Flow<ScreenState.Content>`. Lets per-call tests vary the
     * freshness band + isRefreshing flag carried on the resulting Content's signal.
     */
    private fun <T> content(
        value: T,
        band: FreshnessBand = FreshnessBand.Fresh,
        isRefreshing: Boolean = false,
    ) = flowOf<ScreenState<T>>(
        ScreenState.Content(
            data = value,
            freshnessSignal = FreshnessSignal.initial().copy(band = band, isRefreshing = isRefreshing),
        ),
    )

    private fun <T> noNetwork() =
        flowOf<ScreenState<T>>(ScreenState.NoNetwork())

    private fun <T> loading() =
        flowOf<ScreenState<T>>(ScreenState.Loading)

    private fun <T> error(err: Throwable) =
        flowOf<ScreenState<T>>(ScreenState.Error(err))

    private fun <T> empty() =
        flowOf<ScreenState<T>>(ScreenState.Empty)
}
