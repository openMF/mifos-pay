/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.biller

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao

/**
 * Data-access object for the `wallet_autopay_billers` OFFLINE_LOCAL_ONLY table
 * (GOAL D12) — mirrors the shape of [`BillDao`][kpt.core.database.wallet.bill.BillDao].
 *
 * All reads are reactive [Flow]s; all writes are `suspend` one-shots.
 * Writers go through `notifyingWrite("wallet_autopay_billers") { … }` at the
 * repository layer so `daoFlow("wallet_autopay_billers") { … }` collectors
 * re-emit on wasmJs (parity with Room's native invalidation on Android/Desktop/iOS).
 *
 * Natural sort order is newest-first ([BillerEntity.createdAt] descending) so
 * the biller list surfaces user's most recent additions at the top — matches
 * the pre-migration ordering of `BillerDataSource._billers` (which was insertion
 * order via `addBiller`).
 */
@DbDao
@Dao
interface BillerDao {

    /** Observe all billers, ordered newest-first. */
    @Query("SELECT * FROM wallet_autopay_billers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BillerEntity>>

    /** Observe a single biller by id — nullable when not found. */
    @Query("SELECT * FROM wallet_autopay_billers WHERE id = :id")
    fun observeById(id: String): Flow<BillerEntity?>

    /** Suspend point-lookup for a single biller by id. */
    @Query("SELECT * FROM wallet_autopay_billers WHERE id = :id")
    suspend fun getById(id: String): BillerEntity?

    /**
     * Filter billers by category enum-name — case-sensitive exact match
     * (mirrors the pre-migration `billers.filter { it.category == category }`).
     */
    @Query(
        "SELECT * FROM wallet_autopay_billers " +
            "WHERE category = :category " +
            "ORDER BY createdAt DESC",
    )
    suspend fun getByCategory(category: String): List<BillerEntity>

    /** Search billers by name — case-insensitive substring. */
    @Query(
        "SELECT * FROM wallet_autopay_billers " +
            "WHERE name LIKE '%' || :query || '%' COLLATE NOCASE " +
            "ORDER BY createdAt DESC",
    )
    suspend fun searchByName(query: String): List<BillerEntity>

    /** Insert or replace a single biller. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(biller: BillerEntity)

    /** Insert or replace a batch of billers. Sanctioned migration writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(billers: List<BillerEntity>)

    /** Delete a single biller by id — no-op if absent. */
    @Query("DELETE FROM wallet_autopay_billers WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Delete every biller (StoreCacheManager `clear()` on logout — D7). */
    @Query("DELETE FROM wallet_autopay_billers")
    suspend fun deleteAll()

    /**
     * Point-check for a duplicate (name + accountNumber) before insert — matches
     * the old `BillerRepositoryImpl.saveBiller` duplicate-detection contract.
     */
    @Query(
        "SELECT COUNT(*) FROM wallet_autopay_billers " +
            "WHERE name = :name AND accountNumber = :accountNumber",
    )
    suspend fun countMatching(name: String, accountNumber: String): Int
}
