/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.account

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_saving_account_details` SINGLE-ROW-PER-KEY table.
 *
 * ## Access pattern
 *
 * - **Reads** are reactive [Flow]s per account — [observeById].
 * - **Writes** are one-shot [upsert]s driven by the store's `writer = { … }` lambda.
 *   PK-on-[id] means an incoming fresh detail replaces the cached one atomically
 *   with no separate delete step; there is no "page window" to preserve because
 *   the payload is always a single record.
 *
 * ## Sort order
 *
 * N/A — [observeById] returns 0 or 1 row.
 */
@DbDao
@Dao
interface SavingAccountDetailDao {

    /**
     * Observe the cached savings-account-detail row for the given [id], or `null` on
     * cold cache.
     *
     * The returned [Flow] re-emits on every write to
     * `wallet_saving_account_details`. On wasmJs the Store5 factory wraps this in
     * `daoFlow("wallet_saving_account_details") { … }` — see
     * `SavingAccountDetailStore.kt` — so re-emission fires even when Room 3 alpha's
     * async InvalidationTracker fails.
     */
    @Query("SELECT * FROM wallet_saving_account_details WHERE id = :id")
    fun observeById(id: Long): Flow<SavingAccountDetailEntity?>

    /** Suspend point-lookup. */
    @Query("SELECT * FROM wallet_saving_account_details WHERE id = :id")
    suspend fun getById(id: Long): SavingAccountDetailEntity?

    /** Insert-or-replace the single row for one account. Sole sanctioned writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavingAccountDetailEntity)

    /** Delete the cached row for one account. */
    @Query("DELETE FROM wallet_saving_account_details WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout). */
    @Query("DELETE FROM wallet_saving_account_details")
    suspend fun deleteAll()
}
