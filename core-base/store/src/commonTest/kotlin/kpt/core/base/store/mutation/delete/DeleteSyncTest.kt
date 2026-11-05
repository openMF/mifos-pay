/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation.delete

import kotlinx.coroutines.test.runTest
import org.mobilenativefoundation.store.store5.Bookkeeper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeBookkeeper<K : Any> : Bookkeeper<K> {
    val failed = mutableMapOf<K, Long>()
    override suspend fun getLastFailedSync(key: K): Long? = failed[key]
    override suspend fun setLastFailedSync(key: K, timestamp: Long): Boolean {
        failed[key] = timestamp
        return true
    }
    override suspend fun clear(key: K): Boolean = failed.remove(key) != null
    override suspend fun clearAll(): Boolean {
        failed.clear()
        return true
    }
}

/** Tests the [DeleteSync] network-DELETE-with-sync primitive (SP-1/T3). */
class DeleteSyncTest {

    @Test
    fun onlineDelete_clearsLocal_hitsEndpoint_noTombstone() = runTest {
        var cleared: Int? = null
        var deleted: Int? = null
        val bk = FakeBookkeeper<Int>()
        val sync = DeleteSync<Int>(
            clearLocal = { cleared = it },
            deleteEndpoint = { deleted = it },
            bookkeeper = bk,
            isOnline = { true },
            now = { 1L },
        )
        assertTrue(sync.delete(7))
        assertEquals(7, cleared)
        assertEquals(7, deleted)
        assertFalse(sync.isPending(7))
    }

    @Test
    fun offlineDelete_clearsLocal_skipsEndpoint_tombstones() = runTest {
        var deleted: Int? = null
        val bk = FakeBookkeeper<Int>()
        val sync = DeleteSync<Int>(
            clearLocal = { },
            deleteEndpoint = { deleted = it },
            bookkeeper = bk,
            isOnline = { false },
            now = { 42L },
        )
        assertFalse(sync.delete(9))
        assertEquals(null, deleted, "offline delete must not touch the network")
        assertTrue(sync.isPending(9))
    }

    @Test
    fun retryOnReconnect_landsDelete_clearsTombstone() = runTest {
        var online = false
        var deleted: Int? = null
        val bk = FakeBookkeeper<Int>()
        val sync = DeleteSync<Int>(
            clearLocal = { },
            deleteEndpoint = { deleted = it },
            bookkeeper = bk,
            isOnline = { online },
            now = { 1L },
        )
        sync.delete(5) // offline → tombstoned
        assertTrue(sync.isPending(5))
        online = true
        assertTrue(sync.retry(5))
        assertEquals(5, deleted)
        assertFalse(sync.isPending(5))
    }

    @Test
    fun onlineEndpointFailure_tombstonesForRetry() = runTest {
        val bk = FakeBookkeeper<Int>()
        val sync = DeleteSync<Int>(
            clearLocal = { },
            deleteEndpoint = { throw IllegalStateException("500") },
            bookkeeper = bk,
            isOnline = { true },
            now = { 1L },
        )
        assertFalse(sync.delete(3))
        assertTrue(sync.isPending(3), "a failed online DELETE must be tombstoned for retry")
    }
}
