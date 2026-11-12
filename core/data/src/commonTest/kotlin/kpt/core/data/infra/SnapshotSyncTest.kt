/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.infra
import kotlinx.coroutines.test.runTest
import kpt.core.base.data.infra.Synchronizer
import kpt.core.base.data.infra.snapshotSync
import kpt.core.base.datastore.infra.ChangeListVersions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `snapshotSync` is the algorithm both v1 adopters
 * (Frankfurter + World Bank are snapshot APIs) drive from `syncWith`: run the
 * fetcher, then stamp a fresh timestamp under the feature `name`.
 */
class SnapshotSyncTest {

    /** Minimal in-memory [Synchronizer] — no DataStore, no persistence. */
    private class InMemorySynchronizer : Synchronizer {
        var stored = ChangeListVersions()
            private set

        override suspend fun getChangeListVersions(): ChangeListVersions = stored

        override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
            stored = stored.update()
        }
    }

    @Test
    fun snapshotSync_runs_the_fetcher_once_and_stamps_a_version() = runTest {
        val sync = InMemorySynchronizer()
        var fetchCount = 0

        val result = sync.snapshotSync(name = "currency-rates") { fetchCount++ }

        assertTrue(result)
        assertEquals(1, fetchCount, "fetcher must run exactly once")
        assertTrue(sync.stored.versions.containsKey("currency-rates"), "version key must be stamped")
    }

    @Test
    fun snapshotSync_stamps_distinct_keys_for_distinct_names() = runTest {
        val sync = InMemorySynchronizer()

        sync.snapshotSync(name = "currency-rates") {}
        sync.snapshotSync(name = "macro-indicators") {}

        assertEquals(setOf("currency-rates", "macro-indicators"), sync.stored.versions.keys)
    }

    @Test
    fun snapshotSync_does_not_stamp_when_the_fetcher_throws() = runTest {
        val sync = InMemorySynchronizer()
        var threw = false
        try {
            sync.snapshotSync(name = "currency-rates") { error("network down") }
        } catch (expected: IllegalStateException) {
            threw = true
        }
        assertTrue(threw, "the fetcher failure must propagate")
        assertFalse(sync.stored.versions.containsKey("currency-rates"), "no version stamped on failure")
    }
}
