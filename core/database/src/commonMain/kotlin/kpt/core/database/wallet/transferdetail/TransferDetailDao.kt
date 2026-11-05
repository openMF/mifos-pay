/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.transferdetail

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_transfer_details` SINGLE-ROW-PER-KEY table.
 *
 * ## Access pattern
 *
 * - **Reads** are reactive [Flow]s per transfer — [observeById].
 * - **Writes** are one-shot [upsert]s driven by the store's `writer = { … }` lambda.
 *   PK-on-`transferId` means an incoming fresh detail replaces the cached one
 *   atomically with no separate delete step; there is no "page window" to preserve
 *   because the payload is always a single record.
 *
 * Mirrors `SavingAccountDetailDao`.
 *
 * ## Sort order
 *
 * N/A — [observeById] returns 0 or 1 row.
 */
@DbDao
@Dao
interface TransferDetailDao {

    /**
     * Observe the cached transfer-detail row for the given [transferId], or `null`
     * on cold cache.
     *
     * The returned [Flow] re-emits on every write to `wallet_transfer_details`. On
     * wasmJs the Store5 factory wraps this in
     * `daoFlow("wallet_transfer_details") { … }` — see `TransferDetailStore.kt` — so
     * re-emission fires even when Room 3 alpha's async InvalidationTracker fails.
     */
    @Query("SELECT * FROM wallet_transfer_details WHERE transferId = :transferId")
    fun observeById(transferId: Long): Flow<TransferDetailEntity?>

    /** Insert-or-replace the single row for one transfer. Sole sanctioned writer. */
    @Upsert
    suspend fun upsert(entity: TransferDetailEntity)

    /** Delete the cached row for one transfer. */
    @Query("DELETE FROM wallet_transfer_details WHERE transferId = :transferId")
    suspend fun deleteById(transferId: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout). */
    @Query("DELETE FROM wallet_transfer_details")
    suspend fun deleteAll()
}
