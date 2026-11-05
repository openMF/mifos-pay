/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.biller

import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.biller.BillerDao
import kpt.core.database.wallet.biller.BillerEntity
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/** Room `@Entity(tableName = …)` for [BillerEntity]. Shared with the DAO's writes. */
private const val BILLERS_TABLE = "wallet_autopay_billers"

/**
 * Build the OFFLINE_LOCAL_ONLY [Store] for user-authored billers (GOAL D12 —
 * mirrors the batch-4 pilot's `AutoPayBillStore`).
 *
 * ## Shape
 *
 * `createOfflineStore` — backed exclusively by [BillerDao]. There is NO remote
 * fetcher: billers are local-origin data (user-authored), the server has no
 * counterpart today (see `TODO` in `core/data/BillerRepositoryImpl` which is
 * held ready for the future network path), and the store's reader is
 * Room-only.
 *
 * The paired write path lives in `BillerOfflineRepositoryImpl` and calls
 * `dao.upsert(...)` / `dao.deleteById(...)` inside
 * `notifyingWrite("wallet_autopay_billers") { … }` — the mirror of the pilot
 * pattern established for `wallet_autopay_bills`.
 *
 * ## Store5 shape
 *
 * Key: [Unit] — always returns all billers (no per-biller keyed filter here;
 * point lookups go through the repository directly via [BillerDao.observeById]
 * / [BillerDao.getById], and category / search queries go through
 * [BillerDao.getByCategory] / [BillerDao.searchByName]).
 *
 * The DAO reader is wrapped with `daoFlow("wallet_autopay_billers") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails to fan out. On Android/Desktop/iOS the wrap is a
 * microsecond no-op alongside Room's native invalidation.
 *
 * ## Logout wiring (GOAL D7)
 *
 * The store is registered with `StoreCacheManager` at DI startup —
 * `mgr.register(get(AppStoreRegistry.AutoPayBillers))` in `appStoreModule`. That
 * pins it into the logout drain so a user-B session never sees user-A's
 * billers. The cache manager's `Store.clear()` cascades to the SoT's
 * `deleteAll` lambda, which invokes [BillerDao.deleteAll].
 */
@StoreProvider(id = "autoPayBillers")
fun provideAutoPayBillerStore(dao: BillerDao): Store<Unit, List<BillerEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit ->
            run { dao.observeAll() }
        },
        writer = { _: Unit, billers: List<BillerEntity> -> dao.upsertAll(billers) },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)
