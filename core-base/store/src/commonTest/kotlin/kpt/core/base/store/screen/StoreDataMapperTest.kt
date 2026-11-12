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
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// --- Network + Cache mode tests ---

class StoreDataMapperCachedTest {

    @Test
    fun cacheDataEmitsWithCacheOrigin() = runTest {
        val flow = flowOf(
            StoreReadResponse.Data("cached", StoreReadResponseOrigin.SourceOfTruth),
        )
        flow.mapToStoreData().test {
            val item = awaitItem()
            assertEquals("cached", item.data)
            assertEquals(DataOrigin.CACHE, item.origin)
            assertFalse(item.isRefreshing)
            // CACHE fallback sets fetchedAt so staleness UI has a timestamp
            assertNotNull(item.fetchedAt)
            awaitComplete()
        }
    }

    @Test
    fun loadingThenCacheSetsRefreshing() = runTest {
        val flow = flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()),
            StoreReadResponse.Data("cached", StoreReadResponseOrigin.SourceOfTruth),
        )
        flow.mapToStoreData().test {
            val item = awaitItem()
            assertEquals("cached", item.data)
            assertTrue(item.isRefreshing)
            assertEquals(DataOrigin.CACHE, item.origin)
            awaitComplete()
        }
    }

    @Test
    fun cacheThenNetworkSequence() = runTest {
        val flow = flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()),
            StoreReadResponse.Data("cached", StoreReadResponseOrigin.SourceOfTruth),
            StoreReadResponse.Data("fresh", StoreReadResponseOrigin.Fetcher()),
        )
        flow.mapToStoreData().test {
            val cached = awaitItem()
            assertEquals("cached", cached.data)
            assertTrue(cached.isRefreshing)
            // CACHE fallback sets fetchedAt for staleness UI timestamp
            assertEquals(DataOrigin.CACHE, cached.origin)

            val fresh = awaitItem()
            assertEquals("fresh", fresh.data)
            assertFalse(fresh.isRefreshing)
            assertFalse(fresh.isStale)
            assertNotNull(fresh.fetchedAt)
            assertEquals(DataOrigin.NETWORK, fresh.origin)
            awaitComplete()
        }
    }

    @Test
    fun errorAfterCacheKeepsLastData() = runTest {
        val flow = flowOf(
            StoreReadResponse.Data("cached", StoreReadResponseOrigin.SourceOfTruth),
            StoreReadResponse.Error.Exception(
                error = RuntimeException("timeout"),
                origin = StoreReadResponseOrigin.Fetcher(),
            ),
        )
        flow.mapToStoreDataWithErrors(fallback = "unused").test {
            val cached = awaitItem()
            assertEquals("cached", cached.data)
            assertNull(cached.error)
            assertTrue(cached.isSuccess)

            val errored = awaitItem()
            assertEquals("cached", errored.data)
            assertNotNull(errored.error)
            assertFalse(errored.isEmpty)
            assertFalse(errored.isError)
            awaitComplete()
        }
    }
}

// --- Network-only mode tests ---

class StoreDataMapperNetworkOnlyTest {

    @Test
    fun networkOnlySuccessEmitsWithNetworkOrigin() = runTest {
        val flow = flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()),
            StoreReadResponse.Data("fresh", StoreReadResponseOrigin.Fetcher()),
        )
        flow.mapToStoreData().test {
            val item = awaitItem()
            assertEquals("fresh", item.data)
            assertEquals(DataOrigin.NETWORK, item.origin)
            assertFalse(item.isRefreshing)
            assertFalse(item.isStale)
            assertNotNull(item.fetchedAt)
            awaitComplete()
        }
    }

    @Test
    fun networkOnlyErrorBeforeAnyData() = runTest {
        val flow = flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()),
            StoreReadResponse.Error.Message(
                message = "Network unavailable",
                origin = StoreReadResponseOrigin.Fetcher(),
            ),
        )
        flow.mapToStoreDataWithErrors(fallback = "none").test {
            val item = awaitItem()
            assertEquals("none", item.data)
            assertTrue(item.isEmpty)
            assertTrue(item.isError)
            val error = assertNotNull(item.error)
            assertEquals("Network unavailable", error.message)
            awaitComplete()
        }
    }

    @Test
    fun networkOnlyEmptyResult() = runTest {
        val flow = flowOf(
            StoreReadResponse.Data(emptyList<String>(), StoreReadResponseOrigin.Fetcher()),
        )
        flow.mapToStoreData(isEmpty = { it.isEmpty() }).test {
            val item = awaitItem()
            assertTrue(item.isEmpty)
            assertTrue(item.data.isEmpty())
            assertEquals(DataOrigin.NETWORK, item.origin)
            awaitComplete()
        }
    }
}

// --- Shared behavior tests ---

class StoreDataMapperSharedTest {

    @Test
    fun initialResponseProducesNoEmissions() = runTest {
        flowOf(StoreReadResponse.Initial)
            .mapToStoreData().test { awaitComplete() }
    }

    @Test
    fun loadingAloneProducesNoEmissions() = runTest {
        flowOf(StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()))
            .mapToStoreData().test { awaitComplete() }
    }

    @Test
    fun noNewDataProducesNoEmissions() = runTest {
        flowOf(StoreReadResponse.NoNewData(StoreReadResponseOrigin.Fetcher()))
            .mapToStoreData().test { awaitComplete() }
    }

    @Test
    fun memoryOriginMapsCorrectly() = runTest {
        flowOf(StoreReadResponse.Data("memory", StoreReadResponseOrigin.Cache))
            .mapToStoreData().test {
                assertEquals(DataOrigin.MEMORY, awaitItem().origin)
                awaitComplete()
            }
    }
}

// --- StoreData property tests ---

class StoreDataTest {

    @Test
    fun isStaleWhenNeverFetched() {
        val data = StoreData("test", DataOrigin.CACHE, fetchedAt = null)
        assertTrue(data.isStale)
        assertNull(data.staleDuration)
    }

    @Test
    fun notStaleWhenFetched() {
        val mark = kotlin.time.TimeSource.Monotonic.markNow()
        val data = StoreData("test", DataOrigin.NETWORK, fetchedAt = mark)
        assertFalse(data.isStale)
        assertNotNull(data.staleDuration)
    }

    @Test
    fun networkOriginNeverStale() {
        val data = StoreData("test", DataOrigin.NETWORK, fetchedAt = null)
        assertFalse(data.isStale)
    }

    @Test
    fun isSuccessWhenNoErrorAndNotEmpty() {
        val data = StoreData("test", DataOrigin.NETWORK)
        assertTrue(data.isSuccess)
        assertFalse(data.isError)
    }

    @Test
    fun isErrorWhenErrorAndEmpty() {
        val data = StoreData(
            "",
            DataOrigin.NETWORK,
            error = RuntimeException("fail"),
            isEmpty = true,
        )
        assertTrue(data.isError)
        assertFalse(data.isSuccess)
    }

    @Test
    fun mapPreservesMetadata() {
        val mark = kotlin.time.TimeSource.Monotonic.markNow()
        val original = StoreData(
            "42",
            DataOrigin.NETWORK,
            isRefreshing = true,
            fetchedAt = mark,
        )
        val mapped = original.map { it.toInt() }
        assertEquals(42, mapped.data)
        assertEquals(DataOrigin.NETWORK, mapped.origin)
        assertTrue(mapped.isRefreshing)
        assertEquals(mark, mapped.fetchedAt)
    }
}

// --- mapToStoreDataNoFallback tests ---

class StoreDataMapperNoFallbackTest {

    @Test
    fun nullDataFromSotIsSkipped() = runTest {
        @Suppress("UNCHECKED_CAST")
        val nullData = StoreReadResponse.Data(
            null as String?,
            StoreReadResponseOrigin.SourceOfTruth,
        ) as StoreReadResponse<String>

        flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.SourceOfTruth),
            nullData,
            StoreReadResponse.Data("fetched", StoreReadResponseOrigin.Fetcher()),
        ).mapToStoreDataNoFallback().test {
            // First: Loading with no cache → empty-sentinel placeholder so DecisionEngine
            // can act on networkStatus immediately (starvation fix).
            val loading = awaitItem()
            assertTrue(loading.isEmpty)
            assertTrue(loading.isRefreshing)
            // Then: actual data (null SOT data was skipped)
            val item = awaitItem()
            assertEquals("fetched", item.data)
            assertEquals(DataOrigin.NETWORK, item.origin)
            assertFalse(item.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun errorBeforeData_emitsEmptyWithError() = runTest {
        flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.SourceOfTruth),
            StoreReadResponse.Error.Exception(
                RuntimeException("network"),
                StoreReadResponseOrigin.Fetcher(),
            ),
        ).mapToStoreDataNoFallback<String>().test {
            // First: Loading-without-cache empty sentinel (starvation fix).
            val loading = awaitItem()
            assertTrue(loading.isEmpty)
            assertTrue(loading.isRefreshing)
            assertNull(loading.error)
            // Then: error
            val item = awaitItem()
            assertTrue(item.isEmpty)
            assertFalse(item.isRefreshing)
            assertNotNull(item.error)
            assertEquals("network", item.error?.message)
            awaitComplete()
        }
    }

    @Test
    fun loadingWithoutCache_emitsEmptySentinelImmediately() = runTest {
        // Starvation-fix contract: Loading with no prior data must produce a StoreData
        // emission (isEmpty=true, isRefreshing=true) so combine() in asScreenStream
        // can run DecisionEngine on networkStatus immediately. Without this, the
        // downstream UI is stuck on its initial Loading until Ktor times out (~30s).
        flowOf(
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()),
        ).mapToStoreDataNoFallback<String>().test {
            val item = awaitItem()
            assertTrue(item.isEmpty, "Loading-without-cache must emit isEmpty=true sentinel")
            assertTrue(item.isRefreshing, "Loading-without-cache must mark isRefreshing=true")
            assertNull(item.error)
            assertNull(item.fetchedAtInstant)
            awaitComplete()
        }
    }

    @Test
    fun errorAfterData_emitsDataWithError() = runTest {
        flowOf(
            StoreReadResponse.Data("cached", StoreReadResponseOrigin.SourceOfTruth),
            StoreReadResponse.Error.Exception(
                RuntimeException("refresh failed"),
                StoreReadResponseOrigin.Fetcher(),
            ),
        ).mapToStoreDataNoFallback().test {
            val first = awaitItem()
            assertEquals("cached", first.data)
            val second = awaitItem()
            assertEquals("cached", second.data)
            assertNotNull(second.error)
            assertFalse(second.isEmpty)
            awaitComplete()
        }
    }

    @Test
    fun loadingWithPreviousData_emitsRefreshing() = runTest {
        flowOf(
            StoreReadResponse.Data("initial", StoreReadResponseOrigin.Fetcher()),
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()),
        ).mapToStoreDataNoFallback().test {
            val first = awaitItem()
            assertEquals("initial", first.data)
            assertFalse(first.isRefreshing)
            val second = awaitItem()
            assertEquals("initial", second.data)
            assertTrue(second.isRefreshing)
            awaitComplete()
        }
    }

    @Test
    fun cacheData_setsFetchedAtInstant() = runTest {
        flowOf(
            StoreReadResponse.Data("cached", StoreReadResponseOrigin.SourceOfTruth),
        ).mapToStoreDataNoFallback().test {
            val item = awaitItem()
            assertNotNull(item.fetchedAtInstant)
            awaitComplete()
        }
    }

    @Test
    fun networkData_setsFetchedAtInstant() = runTest {
        flowOf(
            StoreReadResponse.Data("fresh", StoreReadResponseOrigin.Fetcher()),
        ).mapToStoreDataNoFallback().test {
            val item = awaitItem()
            assertNotNull(item.fetchedAtInstant)
            assertEquals(DataOrigin.NETWORK, item.origin)
            awaitComplete()
        }
    }
}
