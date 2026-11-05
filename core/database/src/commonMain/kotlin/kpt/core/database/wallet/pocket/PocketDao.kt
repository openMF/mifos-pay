/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.pocket

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_pockets` LEDGER table.
 *
 * ## Access pattern (GOAL D13 — the LEDGER archetype)
 *
 * - **Reads** are reactive [Flow]s per client — [observeByClient].
 * - **Writes** are one-shot suspend fns; the store's `writer = { … }` lambda
 *   invokes exactly ONE of:
 *   - [replacePage] — the atomic default. Deletes-then-upserts under a single
 *     SQLite transaction so in-flight [observeByClient] collectors never see
 *     an empty page mid-write (S5-PAGE-ATOMIC anti-partial-read invariant).
 *   - [upsertAll] — append/merge only. Reserved for a future incremental
 *     writer if the API grows a delta filter.
 *
 * ## Sort order
 *
 * Ascending by `accountType, accountNumber` — the pre-store VM grouped
 * detailed pockets into LOAN / SAVINGS / SHARE buckets for rendering; a
 * stable ordering by `accountType, accountNumber` keeps the fold to those
 * lists deterministic across refresh cycles.
 */
@DbDao
@Dao
interface PocketDao {

    /**
     * Observe every cached pocket for the given [clientId].
     *
     * The returned [Flow] re-emits on every write to `wallet_pockets`. On
     * wasmJs the Store5 factory wraps this in `daoFlow("wallet_pockets") { … }`
     * — see `PocketStore.kt` — so re-emission fires even when Room 3
     * alpha's async InvalidationTracker fails.
     */
    @Query(
        "SELECT * FROM wallet_pockets " +
            "WHERE clientId = :clientId " +
            "ORDER BY accountType ASC, accountNumber ASC, id ASC",
    )
    fun observeByClient(clientId: Long): Flow<List<PocketEntity>>

    /** Insert-or-replace a batch of rows. Sanctioned merge writer for future delta fetches. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PocketEntity>)

    /**
     * Delete every cached row for the given [clientId]. Called from [replacePage]
     * inside a `@Transaction`; there is no external caller of this method today.
     */
    @Query("DELETE FROM wallet_pockets WHERE clientId = :clientId")
    suspend fun deleteByClient(clientId: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout). */
    @Query("DELETE FROM wallet_pockets")
    suspend fun deleteAll()

    /**
     * ATOMIC page replacement — delete-all-for-client + upsert-batch, under a
     * single SQLite transaction. Guarantees that an in-flight [observeByClient]
     * subscriber cannot observe an empty page mid-write (S5-PAGE-ATOMIC).
     *
     * The store's `writer = { key, items -> dao.replacePage(key.clientId, items) }`
     * lambda is the ONE call site.
     */
    @Transaction
    suspend fun replacePage(clientId: Long, entities: List<PocketEntity>) {
        val pending = getPendingSyncsByClient(clientId)
        val pendingDelinkIds = pending.filter { it.syncStatus == "PENDING_DELINK" }.map { it.id }.toSet()

        val filteredNetworkEntities = entities.map {
            if (it.id in pendingDelinkIds) {
                it.copy(syncStatus = "PENDING_DELINK")
            } else {
                it
            }
        }

        deleteByClient(clientId)
        upsertAll(filteredNetworkEntities)

        val networkAccountKeys = entities.map { "${it.accountId}_${it.accountType}" }.toSet()
        val pendingLinks = pending.filter {
            it.syncStatus == "PENDING_LINK" && "${it.accountId}_${it.accountType}" !in networkAccountKeys
        }
        upsertAll(pendingLinks)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PocketEntity)

    @Query(
        """
        SELECT * FROM wallet_pockets 
        WHERE clientId = :clientId AND syncStatus != 'PENDING_DELINK' 
        ORDER BY accountType ASC, accountNumber ASC, id ASC
        """,
    )
    fun observeLinkedByClient(clientId: Long): Flow<List<PocketEntity>>

    @Query("SELECT * FROM wallet_pockets WHERE syncStatus = 'PENDING_LINK' OR syncStatus = 'PENDING_DELINK'")
    suspend fun getPendingSyncs(): List<PocketEntity>

    @Query(
        """
        SELECT * FROM wallet_pockets 
        WHERE clientId = :clientId AND (syncStatus = 'PENDING_LINK' OR syncStatus = 'PENDING_DELINK')
        """,
    )
    suspend fun getPendingSyncsByClient(clientId: Long): List<PocketEntity>

    @Query("DELETE FROM wallet_pockets WHERE id = :id AND clientId = :clientId")
    suspend fun deleteById(id: Long, clientId: Long)

    @Query("SELECT * FROM wallet_pockets WHERE clientId = :clientId AND syncStatus != 'PENDING_DELINK'")
    suspend fun getLinkedAccounts(clientId: Long): List<PocketEntity>
}
