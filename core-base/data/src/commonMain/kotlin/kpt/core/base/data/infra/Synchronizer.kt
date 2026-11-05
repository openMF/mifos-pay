/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.data.infra

import kotlinx.coroutines.coroutineScope
import kpt.core.base.datastore.infra.ChangeListVersions
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Synchronization contract — ports Now in Android's `core/data/SyncUtilities.kt`.
 *
 * A [Synchronizer] bridges in-memory [ChangeListVersions] state with the
 * persisted store (see `SyncStatePersister`), so [Syncable] adopters can
 * read/write per-feature last-synced versions through a single seam.
 *
 * Adopters call the [Syncable.sync] extension which delegates to
 * [Syncable.syncWith], passing the [Synchronizer] as `this`. Inside `syncWith`
 * an adopter typically calls either [changeListSync] (delta APIs — server tells
 * you what changed) or [snapshotSync] (snapshot APIs — server returns the full
 * canonical set on every call).
 */
interface Synchronizer {
    /** The per-feature last-synced versions, so a sync resumes rather than re-reading everything. */
    suspend fun getChangeListVersions(): ChangeListVersions

    /** Updates the stored versions after a successful sync. */
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)

    /** Convenience: call `someSyncable.sync()` to run [Syncable.syncWith] against this. */
    suspend fun Syncable.sync(): Boolean = this.syncWith(this@Synchronizer)
}

/**
 * Identified network record. Used by [changeListSync] to partition deletes
 * from updates and to advance the per-feature version pointer.
 */
interface NetworkChange {
    /** Identity of the changed record. */
    val id: String

    /** Version this change was published at — the cursor a later sync resumes from. */
    val changeListVersion: Int

    /** Whether the change is a deletion, which must be applied rather than fetched. */
    val isDelete: Boolean
}

/**
 * Adopter contract. A [Syncable] knows how to bring its slice of local state
 * up to date with the network. At v1 the contract is intentionally minimal
 * (no payload arg) — the worker enqueues all-pinned-keys per adopter at fixed
 * defaults. Per-key payload extensibility is a follow-up.
 */
interface Syncable {
    /**
     * Syncs this entity using [synchronizer]. Returns false if the sync could not complete, so the caller leaves the
     * stored version untouched and retries.
     */
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

/**
 * Delta-API algorithm. NiA-port. Used when the server returns
 * `[{id, version, isDelete}, ...]` from `?since=N` semantics — partitions
 * deletes/updates, fans out body fetch, bumps the version pointer.
 *
 * Kept available for the upstream Mifos Fineract PR conversation (Fineract's
 * `lastModifiedSince` matches this shape). Not invoked at v1 because the two
 * canonical v1 adopters (Frankfurter, World Bank) are snapshot APIs.
 */
suspend fun <T : NetworkChange> Synchronizer.changeListSync(
    versionReader: suspend ChangeListVersions.() -> Int,
    changeListFetcher: suspend (Int) -> List<T>,
    versionUpdater: ChangeListVersions.(Int) -> ChangeListVersions,
    modelDeleter: suspend (List<String>) -> Unit,
    modelUpdater: suspend (List<String>) -> Unit,
): Boolean = coroutineScope {
    val currentVersion = getChangeListVersions().versionReader()
    val changeList = changeListFetcher(currentVersion)
    if (changeList.isEmpty()) return@coroutineScope true
    val (deletes, updates) = changeList.partition { it.isDelete }
    modelDeleter(deletes.map { it.id })
    modelUpdater(updates.map { it.id })
    updateChangeListVersions { versionUpdater(changeList.last().changeListVersion) }
    true
}

/**
 * Snapshot-API algorithm. Used by both canonical adopters
 * ([CurrencyRepository] over Frankfurter; [MacroIndicatorsRepository] over
 * World Bank). The fetcher block triggers the actual network work — typically
 * by re-collecting one emission from the underlying repo stream under
 * `FetchPolicy.NETWORK_ONLY` (the forced-refresh seam) — and on success
 * stamps a timestamp under the given [name] key into [ChangeListVersions].
 */
@OptIn(ExperimentalTime::class)
suspend fun Synchronizer.snapshotSync(name: String, fetcher: suspend () -> Unit): Boolean = coroutineScope {
    fetcher()
    updateChangeListVersions {
        copy(versions = versions + (name to Clock.System.now().epochSeconds))
    }
    true
}
