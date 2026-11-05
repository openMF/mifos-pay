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

import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * The network-DELETE-with-sync primitive Store5 lacks (its `Updater` is write-only).
 *
 * A delete clears the local row immediately — so it disappears from every read (Store stream AND Dao
 * reactive query on the same table) at once — then syncs the network DELETE. If the device is offline or
 * the DELETE fails, the key is recorded in the [Bookkeeper] as a pending delete and retried on reconnect
 * by the feature's `SyncOrchestrator` (the same drain that retries failed writes).
 *
 * Every dependency is a lambda / interface so this is exercised without a fake Store5 store.
 *
 * @param clearLocal    removes the local row (typically `store.clear(key)`).
 * @param deleteEndpoint the suspend network DELETE.
 * @param bookkeeper    records/clears the pending-delete tombstone for retry.
 * @param isOnline      connectivity probe.
 * @param now           monotonic millis provider (injected for deterministic tests).
 */
class DeleteSync<K : Any>(
    private val clearLocal: suspend (K) -> Unit,
    private val deleteEndpoint: suspend (K) -> Unit,
    private val bookkeeper: Bookkeeper<K>,
    private val isOnline: suspend () -> Boolean,
    private val now: () -> Long,
) {

    /**
     * Delete [key]: clear locally (row gone from reads), then sync the network DELETE. Offline or on
     * failure the delete is tombstoned for retry. Returns true when the network DELETE has landed.
     */
    suspend fun delete(key: K): Boolean {
        clearLocal(key)
        if (!isOnline()) {
            bookkeeper.setLastFailedSync(key, now())
            return false
        }
        return runCatching { deleteEndpoint(key) }.fold(
            onSuccess = {
                bookkeeper.clear(key)
                true
            },
            onFailure = {
                bookkeeper.setLastFailedSync(key, now())
                false
            },
        )
    }

    /** True when [key] has a pending (un-synced) delete awaiting retry. */
    suspend fun isPending(key: K): Boolean = bookkeeper.getLastFailedSync(key) != null

    /**
     * Retry the pending network DELETE for [key] (called by the feature's `SyncOrchestrator` on
     * reconnect). Clears the tombstone on success. Returns true when the DELETE landed.
     */
    suspend fun retry(key: K): Boolean {
        if (bookkeeper.getLastFailedSync(key) == null) return true
        return runCatching { deleteEndpoint(key) }.fold(
            onSuccess = {
                bookkeeper.clear(key)
                true
            },
            onFailure = { false },
        )
    }
}
