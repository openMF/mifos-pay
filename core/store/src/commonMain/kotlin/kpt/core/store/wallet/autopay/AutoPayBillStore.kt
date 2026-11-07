/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.autopay

import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.bill.BillDao
import kpt.core.database.wallet.bill.BillEntity
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Build the OFFLINE_LOCAL_ONLY [Store] for user-authored bills (GOAL D12 —
 * the `autopay-bills` archetype).
 *
 * ## Shape
 *
 * `createOfflineStore` — backed exclusively by [BillDao]. There is NO remote
 * fetcher: bills are local-origin data (user-authored), the server has no
 * counterpart, and the store's reader is Room-only.
 *
 * The paired write path lives in `BillOfflineRepositoryImpl` and calls
 * `dao.upsert(...)` / `dao.deleteById(...)` inside
 * `notifyingWrite("wallet_autopay_bills") { … }` — the mirror of the template's
 * `AlertsRepositoryImpl` pattern (see `provideAlertsStore` KDoc for the
 * reader-writer-notify contract).
 *
 * ## Store5 shape
 *
 * Key: [Unit] — always returns all bills (no per-bill keyed filter here;
 * point lookups go through the repository directly via [BillDao.observeById]
 * / [BillDao.getById]).
 *
 * The DAO reader is wrapped with `daoFlow("wallet_autopay_bills") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails to fan out. On Android/Desktop/iOS the wrap is a
 * microsecond no-op alongside Room's native invalidation.
 *
 * ## Logout wiring (GOAL D7)
 *
 * The store is registered with `StoreCacheManager` at DI startup —
 * `mgr.register(get(AppStoreRegistry.AutoPayBills))` in `appStoreModule`. That
 * pins it into the logout drain so a user-B session never sees user-A's bills.
 * The cache manager's `Store.clear()` cascades to the SoT's `deleteAll` lambda,
 * which invokes [BillDao.deleteAll].
 */
@StoreProvider(id = "autoPayBills")
fun provideAutoPayBillStore(dao: BillDao): Store<Unit, List<BillEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit ->
            run { dao.observeAll() }
        },
        writer = { _: Unit, bills: List<BillEntity> -> dao.upsertAll(bills) },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)
