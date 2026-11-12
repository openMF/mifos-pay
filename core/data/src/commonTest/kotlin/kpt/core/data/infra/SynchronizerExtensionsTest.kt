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
import kpt.core.base.data.infra.NetworkChange
import kpt.core.base.data.infra.Synchronizer
import kpt.core.base.data.infra.changeListSync
import kpt.core.base.datastore.infra.ChangeListVersions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `changeListSync` is the NiA-port delta algorithm (server
 * returns `[{id, version, isDelete}]`). Kept available for the upstream Mifos
 * Fineract PR conversation; verified here even though the v1 adopters use
 * `snapshotSync`.
 */
class SynchronizerExtensionsTest {

    private class InMemorySynchronizer(initial: ChangeListVersions = ChangeListVersions()) : Synchronizer {
        var stored = initial
            private set

        override suspend fun getChangeListVersions(): ChangeListVersions = stored

        override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
            stored = stored.update()
        }
    }

    private data class FakeChange(
        override val id: String,
        override val changeListVersion: Int,
        override val isDelete: Boolean,
    ) : NetworkChange

    @Test
    fun changeListSync_partitions_deletes_and_updates_and_advances_the_version() = runTest {
        val sync = InMemorySynchronizer()
        val deleted = mutableListOf<String>()
        val updated = mutableListOf<String>()
        val changes = listOf(
            FakeChange("a", changeListVersion = 1, isDelete = false),
            FakeChange("b", changeListVersion = 2, isDelete = true),
            FakeChange("c", changeListVersion = 3, isDelete = false),
        )

        val result = sync.changeListSync(
            versionReader = { versions["loans"]?.toInt() ?: 0 },
            changeListFetcher = { changes },
            versionUpdater = { v -> set("loans", v.toLong()) },
            modelDeleter = { deleted += it },
            modelUpdater = { updated += it },
        )

        assertTrue(result)
        assertEquals(listOf("b"), deleted, "deletes partitioned by isDelete")
        assertEquals(listOf("a", "c"), updated, "updates partitioned by isDelete")
        assertEquals(3L, sync.stored.versions["loans"], "version advanced to last change")
    }

    @Test
    fun changeListSync_no_ops_on_an_empty_change_list() = runTest {
        val sync = InMemorySynchronizer()
        var deleterCalled = false

        val result = sync.changeListSync<FakeChange>(
            versionReader = { 0 },
            changeListFetcher = { emptyList() },
            versionUpdater = { set("loans", it.toLong()) },
            modelDeleter = { deleterCalled = true },
            modelUpdater = { },
        )

        assertTrue(result)
        assertTrue(sync.stored.versions.isEmpty(), "no version stamped when nothing changed")
        assertFalse(deleterCalled, "deleter not invoked on empty change list")
    }
}
