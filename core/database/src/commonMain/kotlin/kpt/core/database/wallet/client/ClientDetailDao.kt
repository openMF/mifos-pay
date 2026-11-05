/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.client

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_client_details` SINGLE-ROW-PER-KEY table.
 *
 * ## Access pattern
 *
 * - **Reads** are reactive [Flow]s per client — [observeById].
 * - **Writes** are one-shot [upsert]s driven by the store's `writer = { … }` lambda.
 *   PK-on-[id] means an incoming fresh client info replaces the cached one
 *   atomically with no separate delete step; there is no "page window" to
 *   preserve because the payload is always a single record.
 *
 * ## Sort order
 *
 * N/A — [observeById] returns 0 or 1 row.
 */
@DbDao
@Dao
interface ClientDetailDao {

    /**
     * Observe the cached client-detail row for the given [id], or `null` on
     * cold cache.
     *
     * The returned [Flow] re-emits on every write to `wallet_client_details`.
     * On wasmJs the Store5 factory wraps this in
     * `daoFlow("wallet_client_details") { … }` — see `ClientDetailStore.kt` —
     * so re-emission fires even when Room 3 alpha's async InvalidationTracker
     * fails.
     */
    @Query("SELECT * FROM wallet_client_details WHERE id = :id")
    fun observeById(id: Long): Flow<ClientDetailEntity?>

    /** Suspend point-lookup. */
    @Query("SELECT * FROM wallet_client_details WHERE id = :id")
    suspend fun getById(id: Long): ClientDetailEntity?

    /** Insert-or-replace the single row for one client. Sole sanctioned writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ClientDetailEntity)

    /** Delete the cached row for one client. */
    @Query("DELETE FROM wallet_client_details WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout). */
    @Query("DELETE FROM wallet_client_details")
    suspend fun deleteAll()
}
