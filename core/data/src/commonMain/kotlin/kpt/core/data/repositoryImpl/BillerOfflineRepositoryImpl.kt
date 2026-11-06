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
import kpt.core.database.wallet.biller.BillerDao
import kpt.core.database.wallet.biller.toDomain
import kpt.core.database.wallet.biller.toEntity
import kpt.core.datastore.BillerRepository
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Room `@Entity(tableName = …)` string — must match [kpt.core.database.wallet.biller.BillerEntity]'s. */
private const val BILLERS_TABLE = "wallet_autopay_billers"

/**
 * Phase-5 Batch-4 Room-backed implementation of the historic
 * [`kpt.core.datastore.BillerRepository`][BillerRepository] contract
 * (GOAL D12 — OFFLINE_LOCAL_ONLY archetype).
 *
 * ## Migration from `core/datastore/BillerDataSource`
 *
 * The previous impl in
 * [`kpt.core.datastore.BillerRepositoryImpl`][kpt.core.datastore.BillerRepositoryImpl]
 * was a multiplatform-settings blob (`Settings.encodeValue("billers", ...)`) —
 * every write serialized the whole list to preferences, and reads went through
 * an in-process `MutableStateFlow` that never survived a cold restart on
 * JS/wasmJs (no OPFS-backed settings on those platforms).
 *
 * This impl replaces the whole surface (parallel to the earlier
 * [`BillOfflineRepositoryImpl`] cutover for `BillRepository`):
 *
 * - **Reads** go through the OFFLINE `Store<Unit, List<BillerEntity>>` provided
 *   by
 *   [`provideAutoPayBillerStore`][kpt.core.store.wallet.biller.provideAutoPayBillerStore]
 *   in `core/store` and are surfaced via [BillerDao.observeAll] wrapped in
 *   `daoFlow("wallet_autopay_billers") { … }`. The Store is registered with
 *   [`StoreCacheManager`][kpt.core.store.infra.StoreCacheManager] at DI startup
 *   for logout cascading (D7).
 * - **Writes** are DAO-direct through
 *   [`notifyingWrite`][kpt.core.base.database.invalidation.notifyingWrite]:
 *   the store's SoT observer re-emits on invalidation. Writes NEVER touch a
 *   remote — billers are user-authored local data, no server counterpart today
 *   (the network path is stubbed in the parallel `core/data/BillerRepositoryImpl`
 *   with a `TODO` for when the Fineract biller-management endpoints materialize).
 *
 * ## Why the impl lives in `core/data` (not `core/datastore`)
 *
 * The interface is grandfathered in `core/datastore` (feature ViewModels import
 * `kpt.core.datastore.BillerRepository`), but the impl is
 * ROOM + STORE5 — dependencies that live in `core/database` + `core/store`.
 * `core/data` already declares both of those, plus depends on `core/datastore`,
 * so the impl naturally composes here — the same host module the earlier
 * `BillOfflineRepositoryImpl` chose for the same reasons. The old
 * `core/datastore/BillerRepositoryImpl` becomes dead code (unbound in DI; kept
 * for one release to give consumers a chance to move to store-native APIs,
 * then deleted in Phase-6 follow-up).
 *
 * ## DI ownership
 *
 * Bound in `core/data`'s `RepositoryModule.kt`:
 * ```kotlin
 * single<BillerRepository> { BillerOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher)) }
 * ```
 * The old binding in `core/datastore/di/PreferenceModule.kt` is REMOVED in the
 * same commit to avoid a double-registration on `BillerRepository`.
 *
 * ## Deviation from the interface contract
 *
 * [BillerRepository] returns bare `Flow<List<Biller>>` from [getAllBillers] —
 * NOT a ScreenState envelope. The Phase-5 CACHE_FIRST_SWR + freshness stream
 * upgrade lands when the feature-side ViewModels are cut over to a
 * ScreenState-native contract in a subsequent follow-up (biller/bill VMs
 * currently maintain their own local `isLoading`/`error` state fields). For
 * now the DAO observer through `daoFlow` gives us reactive reads that survive
 * cold-start on every target.
 */
@OptIn(ExperimentalTime::class)
class BillerOfflineRepositoryImpl(
    private val dao: BillerDao,
    private val ioDispatcher: CoroutineDispatcher,
) : BillerRepository {

    override fun getAllBillers(): Flow<List<Biller>> {
        return dao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getBillerById(id: String): Biller? {
        return withContext(ioDispatcher) {
            dao.getById(id)?.toDomain()
        }
    }

    override suspend fun saveBiller(biller: Biller): Biller {
        return withContext(ioDispatcher) {
            // Preserve the datastore impl's duplicate-detection semantics —
            // reject when a biller with the same (name, accountNumber) already exists.
            val duplicates = dao.countMatching(biller.name, biller.accountNumber)
            require(duplicates == 0) { "Biller with this name and account number already exists" }
            val toWrite = biller.toEntity(generatedIdFallback = generateBillerId())
            run {
                dao.upsert(toWrite)
            }
            toWrite.toDomain()
        }
    }

    override suspend fun updateBiller(biller: Biller): Biller {
        return withContext(ioDispatcher) {
            val existingId = requireNotNull(biller.id) { "Biller ID is required for update" }
            checkNotNull(dao.getById(existingId)) { "Biller not found" }
            val entity = biller.toEntity(generatedIdFallback = existingId)
            run {
                dao.upsert(entity)
            }
            entity.toDomain()
        }
    }

    override suspend fun deleteBiller(id: String) {
        withContext(ioDispatcher) {
            run {
                dao.deleteById(id)
            }
        }
    }

    override suspend fun getBillersByCategory(category: BillerCategory): List<Biller> {
        return try {
            withContext(ioDispatcher) {
                dao.getByCategory(category.name).map { it.toDomain() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchBillersByName(query: String): List<Biller> {
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

    override suspend fun isBillerExists(name: String, accountNumber: String): Boolean {
        return try {
            withContext(ioDispatcher) {
                dao.countMatching(name, accountNumber) > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearAllBillers() {
        withContext(ioDispatcher) {
            run {
                dao.deleteAll()
            }
        }
    }

    /** Local id generator — matches the shape the old `core/datastore/BillerRepositoryImpl` used. */
    private fun generateBillerId(): String =
        "biller_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(0, 1000)}"
}
