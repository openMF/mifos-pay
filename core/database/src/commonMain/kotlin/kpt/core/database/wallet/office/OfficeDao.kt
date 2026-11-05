/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.office

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_offices` global-reference table.
 *
 * ## Access pattern (GOAL D13 — global LEDGER variant, singleton key)
 *
 * - **Reads** are reactive [Flow]s over the WHOLE table — [observeAll].
 * - **Writes** are one-shot suspend fns; the store's `writer = { … }` lambda
 *   invokes exactly ONE of:
 *   - [replaceAll] — the atomic default. Deletes-all + upserts-all under a
 *     single SQLite transaction so in-flight [observeAll] collectors never see
 *     an empty table mid-write (S5-PAGE-ATOMIC anti-partial-read invariant).
 *   - [upsertAll] — append/merge only. Reserved for a future incremental
 *     writer if the API grows a delta filter.
 *
 * The DAO does NOT expose a `deleteByClient` variant — the office list is
 * global (not client-scoped), so [deleteAll] IS the only sanctioned pruner.
 *
 * ## Sort order
 *
 * Alphabetical by [OfficeEntity.name] (case-insensitive) with `id ASC` as
 * tiebreaker. The FastMpayProcessor lookup (`find { it.id == qrData.officeId }`)
 * is O(N) at the caller anyway, so a name-sorted read matches the expected
 * pull-to-refresh list rendering order.
 */
@DbDao
@Dao
interface OfficeDao {

    /**
     * Observe every cached office (the whole table), alphabetical by name.
     *
     * The returned [Flow] re-emits on every write to `wallet_offices`. On
     * wasmJs the Store5 factory wraps this in `daoFlow("wallet_offices") { … }`
     * — see `OfficeStore.kt` — so re-emission fires even when Room 3 alpha's
     * async InvalidationTracker fails.
     */
    @Query(
        "SELECT * FROM wallet_offices " +
            "ORDER BY name COLLATE NOCASE ASC, id ASC",
    )
    fun observeAll(): Flow<List<OfficeEntity>>

    /** Insert-or-replace a batch of rows. Sanctioned merge writer for future delta fetches. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<OfficeEntity>)

    /** Delete every cached row (StoreCacheManager `clear()` on logout AND the [replaceAll] first-half). */
    @Query("DELETE FROM wallet_offices")
    suspend fun deleteAll()

    /**
     * ATOMIC full-table replacement — delete-all + upsert-batch, under a single
     * SQLite transaction. Guarantees that an in-flight [observeAll] subscriber
     * cannot observe an empty table mid-write (S5-PAGE-ATOMIC).
     *
     * The store's `writer = { _, items -> dao.replaceAll(items) }` lambda is
     * the ONE call site.
     */
    @Transaction
    suspend fun replaceAll(entities: List<OfficeEntity>) {
        deleteAll()
        upsertAll(entities)
    }
}
