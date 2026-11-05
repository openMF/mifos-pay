/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.transaction

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_transactions` LEDGER table.
 *
 * ## Access pattern (GOAL D13 — the LEDGER archetype)
 *
 * - **Reads** are reactive [Flow]s per account — [observeByAccount].
 * - **Writes** are one-shot suspend fns; the store's `writer = { … }` lambda
 *   invokes exactly ONE of:
 *   - [replacePage] — the atomic default. Deletes-then-upserts under a single
 *     SQLite transaction so in-flight [observeByAccount] collectors never see
 *     an empty page mid-write (S5-PAGE-ATOMIC anti-partial-read invariant).
 *   - [upsertAll] — append/merge only. Sanctioned for future incremental delta
 *     fetches (see [latestDateForAccount] below): once Fineract exposes a
 *     `?since=<ms>` transactions filter, the store's fetcher can pull only the
 *     new tail and its writer switches to `upsertAll(delta)` — no server-truth
 *     re-delete needed. Today the API returns the full account list, so the
 *     writer stays on [replacePage].
 *
 * ## Delta cursor
 *
 * [latestDateForAccount] returns the newest cached `dateEpochMs` for a given
 * `accountId` (or `null` when the page is empty). This is the SINCE cursor the
 * store's fetcher reads *before* calling the network — it lets the fetcher choose
 * "full pull" (cold cache) vs "delta" (warm cache) even while today's API only
 * supports the former. Wiring the cursor now keeps the store body stable for the
 * eventual server-side `?since=` upgrade.
 *
 * ## Sort order
 *
 * Newest-first on `dateEpochMs` (nulls last), tiebreaker on `transactionId`.
 * Matches the existing UI's chronological rendering in `HistoryScreen`.
 */
@DbDao
@Dao
interface TransactionDao {

    /**
     * Observe every cached transaction for the given [accountId], newest-first.
     *
     * The returned [Flow] re-emits on every write to `wallet_transactions`. On
     * wasmJs the Store5 factory wraps this in `daoFlow("wallet_transactions") { … }`
     * — see `HistoryStore.kt` — so re-emission fires even when Room 3 alpha's
     * async InvalidationTracker fails.
     */
    @Query(
        "SELECT * FROM wallet_transactions " +
            "WHERE accountId = :accountId " +
            "ORDER BY dateEpochMs DESC, transactionId DESC",
    )
    fun observeByAccount(accountId: Long): Flow<List<TransactionEntity>>

    /**
     * Observe the single cached transaction with the given [transactionId], or
     * `null` on cold cache. Keys on the PK, so it is account-agnostic — the
     * transaction-detail drill-down reads a specific row offline-first via
     * `AccountRepositoryImpl.getTransaction(...)`.
     *
     * The returned [Flow] re-emits on every write to `wallet_transactions` (e.g.
     * when `provideHistoryStore` populates the ledger), letting the detail screen
     * upgrade from a network read to the cached row transparently.
     */
    @Query("SELECT * FROM wallet_transactions WHERE transactionId = :transactionId")
    fun observeByTransactionId(transactionId: Long): Flow<TransactionEntity?>

    /**
     * SINCE cursor for delta fetches — the newest cached `dateEpochMs` for the
     * given [accountId], or `null` when the page is empty (cold cache).
     *
     * Suspend not Flow: the store's fetcher reads this ONCE per fetch decision;
     * observation happens through [observeByAccount].
     */
    @Query(
        "SELECT MAX(dateEpochMs) FROM wallet_transactions " +
            "WHERE accountId = :accountId",
    )
    suspend fun latestDateForAccount(accountId: Long): Long?

    /** Insert-or-replace a single row. Kept for tests + spot-writes; production writes go via [replacePage]/[upsertAll]. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)

    /** Insert-or-replace a batch of rows. Sanctioned merge writer for future delta fetches. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TransactionEntity>)

    /**
     * Delete every cached row for the given [accountId]. Called from [replacePage]
     * inside a `@Transaction` so external callers don't need to worry about the
     * empty-page window; there is no external caller of this method today.
     */
    @Query("DELETE FROM wallet_transactions WHERE accountId = :accountId")
    suspend fun deleteByAccount(accountId: Long)

    /** Delete every cached row (StoreCacheManager `clear()` on logout). */
    @Query("DELETE FROM wallet_transactions")
    suspend fun deleteAll()

    /**
     * ATOMIC page replacement — delete-all-for-account + upsert-batch, under a
     * single SQLite transaction. Guarantees that an in-flight
     * [observeByAccount] subscriber cannot observe an empty page mid-write
     * (S5-PAGE-ATOMIC — `training-layer/instructions/stream-first/latest/CORE_DATABASE.md`
     * "Atomic paged writer recipe").
     *
     * The store's `writer = { key, items -> dao.replacePage(key.accountId, items) }`
     * lambda is the ONE call site.
     */
    @Transaction
    suspend fun replacePage(accountId: Long, entities: List<TransactionEntity>) {
        deleteByAccount(accountId)
        upsertAll(entities)
    }
}
