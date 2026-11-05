/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.selfaccounts

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_self_accounts` LEDGER table.
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
 * Ascending by [SelfAccountEntity.number] (case-insensitive) with `id ASC` as
 * tiebreaker — matches the deterministic ordering `AccountRepositoryImpl.getSelfAccounts`
 * effectively yields (server response order is preserved through
 * `toAccount()`; a stable name/id order keeps the QR-generation-picker
 * rendering deterministic across refresh cycles).
 */
@DbDao
@Dao
interface SelfAccountDao {

    /**
     * Observe every cached self-account for the given [clientId], sorted by
     * account number.
     *
     * The returned [Flow] re-emits on every write to `wallet_self_accounts`.
     * On wasmJs the Store5 factory wraps this in
     * `daoFlow("wallet_self_accounts") { … }` — see `SelfAccountsStore.kt` —
     * so re-emission fires even when Room 3 alpha's async InvalidationTracker
     * fails.
     */
    @Query(
        "SELECT * FROM wallet_self_accounts " +
            "WHERE clientId = :clientId " +
            "ORDER BY number COLLATE NOCASE ASC, id ASC",
    )
    fun observeByClient(clientId: Long): Flow<List<SelfAccountEntity>>

    /** Insert-or-replace a batch of rows. Sanctioned merge writer for future delta fetches. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<SelfAccountEntity>)

    /**
     * Delete every cached row for the given [clientId]. Called from [replacePage]
     * inside a `@Transaction`; there is no external caller of this method today.
     */
    @Query("DELETE FROM wallet_self_accounts WHERE clientId = :clientId")
    suspend fun deleteByClient(clientId: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout). */
    @Query("DELETE FROM wallet_self_accounts")
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
    suspend fun replacePage(clientId: Long, entities: List<SelfAccountEntity>) {
        deleteByClient(clientId)
        upsertAll(entities)
    }
}
