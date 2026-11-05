/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.recentpayee

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_recent_payees` LOCAL-DERIVED table
 * (GOAL D12 — persist-on-derive variant of the OFFLINE_LOCAL_ONLY archetype).
 *
 * ## Access pattern
 *
 * - **Reads** are reactive [Flow]s per source-account — [observeBySourceAccount].
 *   Sort is newest-first by `lastTransferDateSortKey DESC` then by
 *   `lastTransferDate DESC` (lexical fallback when the sort key is `0L` for
 *   rows written by a derive path that didn't precompute one).
 * - **Writes** are one-shot suspend fns; the derive step in
 *   `RecentPayeeRepositoryImpl` invokes [replacePage] under a single
 *   `@Transaction` so in-flight [observeBySourceAccount] collectors never see
 *   an empty page mid-write (S5-PAGE-ATOMIC anti-partial-read invariant).
 *
 * Writes go through `notifyingWrite("wallet_recent_payees") { … }` at the
 * repository layer so `daoFlow("wallet_recent_payees") { … }` collectors
 * re-emit on wasmJs.
 */
@DbDao
@Dao
interface RecentPayeeDao {

    /**
     * Observe every cached recent-payee for the given [sourceAccountId], newest
     * transfer first.
     *
     * The returned [Flow] re-emits on every write to `wallet_recent_payees`.
     * The Store5 factory wraps this in `daoFlow("wallet_recent_payees") { … }`
     * (see `RecentPayeeStore.kt`) so re-emission fires even when Room 3 alpha's
     * async InvalidationTracker fails on wasmJs.
     */
    @Query(
        "SELECT * FROM wallet_recent_payees " +
            "WHERE sourceAccountId = :sourceAccountId " +
            "ORDER BY lastTransferDateSortKey DESC, lastTransferDate DESC, clientId ASC",
    )
    fun observeBySourceAccount(sourceAccountId: Long): Flow<List<RecentPayeeEntity>>

    /** Insert-or-replace a batch of rows. Called from [replacePage] under `@Transaction`. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<RecentPayeeEntity>)

    /**
     * Delete every cached row for the given [sourceAccountId]. Called from
     * [replacePage] inside a `@Transaction`; there is no external caller today.
     */
    @Query("DELETE FROM wallet_recent_payees WHERE sourceAccountId = :sourceAccountId")
    suspend fun deleteBySourceAccount(sourceAccountId: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout — D7). */
    @Query("DELETE FROM wallet_recent_payees")
    suspend fun deleteAll()

    /**
     * ATOMIC page replacement — delete-all-for-source + upsert-batch, under a
     * single SQLite transaction. Guarantees that an in-flight
     * [observeBySourceAccount] subscriber cannot observe an empty page
     * mid-write (S5-PAGE-ATOMIC).
     *
     * The repository's derive path is the ONE call site; it wraps the invocation
     * in `notifyingWrite("wallet_recent_payees") { … }` for wasmJs invalidation.
     */
    @Transaction
    suspend fun replacePage(sourceAccountId: Long, entities: List<RecentPayeeEntity>) {
        deleteBySourceAccount(sourceAccountId)
        upsertAll(entities)
    }
}
