/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.bill

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_autopay_bills` OFFLINE_LOCAL_ONLY table
 * (GOAL D12).
 *
 * All reads are reactive [Flow]s; all writes are `suspend` one-shots.
 * Writers go through `notifyingWrite("wallet_autopay_bills") { … }` at the
 * repository layer so `daoFlow("wallet_autopay_bills") { … }` collectors
 * re-emit on wasmJs (parity with Room's native invalidation on Android/Desktop/iOS).
 *
 * Natural sort order is newest-first ([BillEntity.createdAt] descending) so the
 * bills list surfaces user's most recent additions at the top.
 */
@DbDao
@Dao
interface BillDao {

    /** Observe all bills, ordered newest-first. */
    @Query("SELECT * FROM wallet_autopay_bills ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BillEntity>>

    /** Observe a single bill by id — nullable when not found. */
    @Query("SELECT * FROM wallet_autopay_bills WHERE id = :id")
    fun observeById(id: String): Flow<BillEntity?>

    /** Suspend point-lookup for a single bill by id. */
    @Query("SELECT * FROM wallet_autopay_bills WHERE id = :id")
    suspend fun getById(id: String): BillEntity?

    /** Search bills by name — case-insensitive substring. */
    @Query(
        "SELECT * FROM wallet_autopay_bills " +
            "WHERE name LIKE '%' || :query || '%' COLLATE NOCASE " +
            "ORDER BY createdAt DESC",
    )
    suspend fun searchByName(query: String): List<BillEntity>

    /** Insert or replace a single bill. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bill: BillEntity)

    /** Insert or replace a batch of bills. Sanctioned migration writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bills: List<BillEntity>)

    /** Delete a single bill by id — no-op if absent. */
    @Query("DELETE FROM wallet_autopay_bills WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Delete every bill (StoreCacheManager `clear()` on logout — D7). */
    @Query("DELETE FROM wallet_autopay_bills")
    suspend fun deleteAll()

    /** Point-check for a duplicate (name + billerId) before insert — matches the old datastore contract. */
    @Query(
        "SELECT COUNT(*) FROM wallet_autopay_bills " +
            "WHERE name = :name AND (billerId = :billerId OR (billerId IS NULL AND :billerId IS NULL))",
    )
    suspend fun countMatching(name: String, billerId: String?): Int
}
