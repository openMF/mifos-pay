/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.office

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.office.OfficeDao
import kpt.core.database.wallet.office.toDomain
import kpt.core.database.wallet.office.toEntity
import kpt.core.model.office.Office
import kpt.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `OfficeEntity`. Shared with the DAO's writes. */
private const val OFFICES_TABLE = "wallet_offices"

/**
 * Build the LEDGER read [Store] for the global Fineract office list
 * (GOAL D13 — global-reference-data variant of the `history` archetype).
 *
 * ## Singleton-key deviation
 *
 * Unlike the per-client Phase-5 stores, this store keys on the value-less
 * [OfficesKey] `data object` — the office list is global reference data (not
 * client-scoped), so there is exactly ONE cache slot. See [OfficesKey]'s
 * KDoc for the full rationale. The DAO's [OfficeDao.observeAll] reader ignores
 * the key entirely; [OfficeDao.replaceAll] is the atomic full-table writer.
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_offices` [SourceOfTruth]; the network fetcher pulls from
 * `officeApi.getOffices()` and hands the mapped list to the writer for atomic
 * full-table replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## API manager choice
 *
 * `SelfServiceApiManager` (not `FineractApiManager`) — parity with
 * `OfficeRepositoryImpl`. The office endpoint is available on both mounts, but
 * the self-service mount is what the pre-store `OfficeRepository` bound.
 *
 * ## Atomic full-table write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [OfficeDao.replaceAll] — a single `@Transaction`
 * that delete-all + upserts-all under one SQLite transaction. In-flight
 * [OfficeDao.observeAll] subscribers never see an empty table mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_offices") { … }` so wasmJs
 * collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. Office CRUD is not a
 * wallet-app surface (offices are managed by Fineract admin), so there is no
 * app-side write path here. There is no `Bookkeeper`.
 */
@StoreProvider(id = "offices")
@CacheKey(name = "ALL", key = "wallet_offices")
fun provideOfficeStore(
    apiManager: SelfServiceApiManager,
    dao: OfficeDao,
): Store<OfficesKey, List<Office>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: OfficesKey ->
        // Ktorfit returns Flow<List<Office>> — take the first (and only) emission.
        apiManager.officeApi.getOffices().first()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: OfficesKey ->
            run { dao.observeAll() }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { _: OfficesKey, offices: List<Office> ->
            val stamp = clock()
            // ATOMIC full-table replacement — see [OfficeDao.replaceAll] KDoc
            // for the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replaceAll(
                entities = offices.map { it.toEntity(fetchedAtEpochMs = stamp) },
            )
        },
        delete = { _: OfficesKey -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)
