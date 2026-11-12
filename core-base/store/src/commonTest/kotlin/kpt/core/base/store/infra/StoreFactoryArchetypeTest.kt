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

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.screen.streamData
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behavioural contract for the [StoreFactory] archetype factories.
 *
 * This file previously contained a single `assertTrue(true, "createOfflineStore function exists")`
 * whose comment claimed "compile-time verification: if createOfflineStore doesn't exist, this file
 * won't compile" — but it never referenced the function, so nothing was verified at compile time OR
 * runtime. It reported green while an archetype showcase was being deleted elsewhere. These tests
 * actually CALL each factory (so the compile-time claim is now true) and assert the behaviour that
 * distinguishes the archetypes.
 *
 * Archetype ↔ showcase coverage is enforced separately by
 * `scripts/product-health/checks/store-archetype-coverage.sh` against `core/store/STORE_ARCHETYPES.yaml`.
 */
class StoreFactoryArchetypeTest {

    @Test
    fun createMemoryStore_servesFromFetcher_andHasNoSourceOfTruth() = runTest {
        // MEMORY_ONLY: a fetcher, no SourceOfTruth. The defining property is that nothing is
        // persisted — the cache lives only as long as the store instance.
        var fetchCount = 0
        val store = StoreFactory.createMemoryStore<String, String>(
            fetcher = Fetcher.of { key ->
                fetchCount++
                "fetched:$key"
            },
        )

        store.streamData("k").test {
            val first = awaitItem()
            assertEquals("fetched:k", first.data)
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(fetchCount >= 1, "memory store must reach the fetcher; fetchCount=$fetchCount")

        // A fresh instance shares no state with the previous one — that IS "no SourceOfTruth".
        var secondFetchCount = 0
        val fresh = StoreFactory.createMemoryStore<String, String>(
            fetcher = Fetcher.of { key ->
                secondFetchCount++
                "fetched:$key"
            },
        )
        fresh.streamData("k").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(
            secondFetchCount >= 1,
            "a new memory store cannot inherit the previous instance's cache — if this ever passes " +
                "with 0 fetches, the archetype has silently gained persistence",
        )
    }

    @Test
    fun createOfflineStore_readsLocalOnly_withNoFetcher() = runTest {
        // OFFLINE_LOCAL_ONLY / CACHE_ONLY: a SourceOfTruth and NO fetcher. There is no network leg
        // to run, so the store can only ever emit what local storage holds.
        val local = MutableStateFlow<String?>("local-value")
        val store = StoreFactory.createOfflineStore<String, String>(
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: String -> local },
                writer = { _: String, value: String -> local.value = value },
            ),
        )

        store.streamData("k").test {
            assertEquals("local-value", awaitItem().data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createOfflineStore_reflectsSourceOfTruthWrites() = runTest {
        // The read stream observes the SoT, so a write behind the store's back still surfaces —
        // this is what lets a MutableStore + read Store pair share one Room table.
        val local = MutableStateFlow<String?>("before")
        val store = StoreFactory.createOfflineStore<String, String>(
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: String -> local },
                writer = { _: String, value: String -> local.value = value },
            ),
        )

        assertEquals("before", store.streamData("k").first { !it.isEmpty }.data)
        local.value = "after"
        assertEquals(
            "after",
            store.streamData("k").first { it.data == "after" }.data,
            "an offline store must re-emit when its SourceOfTruth changes",
        )
    }
}
