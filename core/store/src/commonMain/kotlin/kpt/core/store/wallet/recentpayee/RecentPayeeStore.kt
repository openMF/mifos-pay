/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.recentpayee

import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.recentpayee.RecentPayeeDao
import kpt.core.database.wallet.recentpayee.RecentPayeeEntity
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/** Room `@Entity(tableName = …)` for [RecentPayeeEntity]. Shared with the DAO's writes. */
private const val RECENT_PAYEES_TABLE = "wallet_recent_payees"

/**
 * Build the LOCAL-DERIVED [Store] for recent-payee lists (GOAL D12 —
 * persist-on-derive variant of the OFFLINE_LOCAL_ONLY archetype).
 *
 * ## Shape
 *
 * `createOfflineStore` — backed exclusively by [RecentPayeeDao]. There is NO
 * remote fetcher because recent-payees are DERIVED from the user's own
 * transaction history + transfer-detail walk (an N+1 fan-out in
 * `RecentPayeeRepositoryImpl`); the derive step lives at the repository layer
 * and writes the resulting list into this store via
 * `notifyingWrite("wallet_recent_payees") { dao.replacePage(sourceAccountId, entities) }`.
 *
 * ## Store5 shape
 *
 * Key: [RecentPayeeKey] wrapping the source `savingsAccountId`. The DAO's
 * `observeBySourceAccount` reader scopes rows per source-account so a user
 * with multiple own accounts sees a distinct cache slot per source.
 *
 * The DAO reader is wrapped with `daoFlow("wallet_recent_payees") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Why not `createStore` with a fetcher?
 *
 * The N+1 derive walk cannot be modeled as a Store5 `Fetcher.of { key -> … }`
 * without introducing multi-arg coordination on the repository side (the walk
 * needs the `SelfServiceApiManager` + kermit logging and returns a list of
 * intermediates before folding into `RecentPayee` rows). Keeping the derive at
 * the repository layer and using the store purely as a Room-backed offline
 * cache preserves the existing derive semantics while adding offline visibility
 * + logout-cleared eviction.
 *
 * ## Logout wiring (GOAL D7)
 *
 * The store is registered with `StoreCacheManager` at DI startup —
 * `mgr.register(get(AppStoreRegistry.RecentPayee))` in `appStoreModule`. That
 * pins it into the logout drain so a user-B session never sees user-A's
 * derived recent-payees. The cache manager's `Store.clear()` cascades to the
 * SoT's `deleteAll` lambda, which invokes [RecentPayeeDao.deleteAll].
 */
@StoreProvider(id = "recentPayee")
fun provideRecentPayeeStore(dao: RecentPayeeDao): Store<RecentPayeeKey, List<RecentPayeeEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: RecentPayeeKey ->
            run { dao.observeBySourceAccount(key.sourceAccountId) }
        },
        writer = { key: RecentPayeeKey, rows: List<RecentPayeeEntity> ->
            // Atomic page replacement — see [RecentPayeeDao.replacePage] KDoc
            // for the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(sourceAccountId = key.sourceAccountId, entities = rows)
        },
        delete = { key: RecentPayeeKey -> dao.deleteBySourceAccount(key.sourceAccountId) },
        deleteAll = { dao.deleteAll() },
    ),
)
