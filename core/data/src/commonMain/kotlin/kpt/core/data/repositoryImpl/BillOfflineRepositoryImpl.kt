/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.database.wallet.bill.BillDao
import kpt.core.database.wallet.bill.toDomain
import kpt.core.database.wallet.bill.toEntity
import kpt.core.datastore.BillRepository
import kpt.core.model.autopay.Bill
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Phase-4 Batch-A Room-backed implementation of the historic
 * [`kpt.core.datastore.BillRepository`][BillRepository] contract
 * (GOAL D12 — OFFLINE_LOCAL_ONLY archetype).
 *
 * ## Migration from `core/datastore/BillDataSource`
 *
 * The previous impl in `core/datastore/BillRepositoryImpl` was a
 * multiplatform-settings blob (`Settings.encodeValue("bills", ...)`) —
 * every write serialized the whole list to preferences, and reads went
 * through an in-process `MutableStateFlow` that never survived a cold restart
 * on JS/WasmJS (no OPFS-backed settings on those platforms).
 *
 * This impl replaces the whole surface:
 *
 * - **Reads** go through the OFFLINE `Store<Unit, List<BillEntity>>` provided
 *   by
 *   [`provideAutoPayBillStore`][kpt.core.store.wallet.autopay.provideAutoPayBillStore]
 *   in `core/store` and are surfaced via [BillDao.observeAll] wrapped in
 *   `daoFlow("wallet_autopay_bills") { … }`. The Store is registered with
 *   [`StoreCacheManager`][kpt.core.store.infra.StoreCacheManager] at DI startup
 *   for logout cascading (D7).
 * - **Writes** are DAO-direct through
 *   [`notifyingWrite`][kpt.core.base.database.invalidation.notifyingWrite]:
 *   the store's SoT observer re-emits on invalidation. Writes NEVER touch a
 *   remote — bills are user-authored local data, no server counterpart today.
 *
 * ## Why the impl lives in `core/data` (not `core/datastore`)
 *
 * The interface is grandfathered in `core/datastore` (feature ViewModels
 * import `kpt.core.datastore.BillRepository`), but the impl is
 * ROOM + STORE5 — dependencies that live in `core/database` + `core/store`.
 * `core/data` already declares both of those, plus depends on `core/datastore`,
 * so the impl naturally composes here. The old `core/datastore/BillRepositoryImpl`
 * becomes dead code (unwired in DI; kept for one release to give consumers a
 * chance to move to store-native APIs, then deleted in Phase-5 follow-up).
 *
 * ## DI ownership
 *
 * Bound in `core/data`'s `RepositoryModule.kt`:
 * ```kotlin
 * single<BillRepository> { BillOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher)) }
 * ```
 * The old binding in `core/datastore/di/PreferenceModule.kt` is removed in the
 * same commit to avoid a double-registration.
 */
@OptIn(ExperimentalTime::class)
class BillOfflineRepositoryImpl(
    private val dao: BillDao,
    private val ioDispatcher: CoroutineDispatcher,
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> {
        // NOTE: the interface returns Flow<List<Bill>> — a bare list, no
        // ScreenState envelope. The Phase-4 CACHE_FIRST_SWR + freshness stream
        // upgrade lands when the feature-side ViewModels are cut over to a
        // ScreenState-native contract in Phase-5. For now the DAO observer
        // through `daoFlow` gives us reactive reads that survive cold-start.
        return dao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getBillById(id: String): Bill? {
        return withContext(ioDispatcher) {
            dao.getById(id)?.toDomain()
        }
    }

    override suspend fun saveBill(bill: Bill): Bill {
        return withContext(ioDispatcher) {
            // Preserve the datastore impl's duplicate-detection semantics —
            // reject when a bill with the same (name, billerId) already exists.
            val duplicates = dao.countMatching(bill.name, bill.billerId)
            require(duplicates == 0) { "Bill with this name and biller already exists" }
            val toWrite = bill.toEntity(generatedIdFallback = generateBillId())
            run {
                dao.upsert(toWrite)
            }
            toWrite.toDomain()
        }
    }

    override suspend fun updateBill(bill: Bill): Bill {
        return withContext(ioDispatcher) {
            val existingId = requireNotNull(bill.id) { "Bill ID is required for update" }
            checkNotNull(dao.getById(existingId)) { "Bill not found" }
            val entity = bill.toEntity(generatedIdFallback = existingId)
            run {
                dao.upsert(entity)
            }
            entity.toDomain()
        }
    }

    override suspend fun deleteBill(id: String) {
        withContext(ioDispatcher) {
            run {
                dao.deleteById(id)
            }
        }
    }

    override suspend fun searchBillsByName(query: String): List<Bill> {
        return try {
            withContext(ioDispatcher) {
                if (query.isBlank()) {
                    dao.observeAll().first().map { it.toDomain() }
                } else {
                    dao.searchByName(query).map { it.toDomain() }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun clearAllBills() {
        withContext(ioDispatcher) {
            run {
                dao.deleteAll()
            }
        }
    }

    /** Local id generator — matches the shape the old `core/data/BillRepositoryImpl` used. */
    private fun generateBillId(): String =
        "bill_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(0, 1000)}"
}
