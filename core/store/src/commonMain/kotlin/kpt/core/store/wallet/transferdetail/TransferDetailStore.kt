/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.transferdetail

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.transferdetail.TransferDetailDao
import kpt.core.database.wallet.transferdetail.toDomain
import kpt.core.database.wallet.transferdetail.toEntity
import org.mifospay.core.model.savingsaccount.TransferDetail
import org.mifospay.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `TransferDetailEntity`. Shared with the DAO's writes. */
private const val TRANSFER_DETAIL_TABLE = "wallet_transfer_details"

/**
 * Build the SINGLE-ROW-PER-KEY read [Store] for account-transfer details
 * (GOAL D13 — a variation of the `history` archetype). Mirrors
 * `provideAccountDetailStore`.
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through the
 * Room `wallet_transfer_details` [SourceOfTruth]; the network fetcher pulls from
 * `accountTransfersApi.getAccountTransfer(transferId)` (Ktorfit `Flow<TransferDetail>`,
 * take the first emission), then hands the domain [TransferDetail] to the writer for
 * atomic single-row replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is the
 * Phase-5 app-wide default.
 *
 * ## Atomic single-row write (S5-PAGE-ATOMIC — trivial case)
 *
 * The writer lambda calls [TransferDetailDao.upsert] — a single-row `@Upsert` under
 * Room's implicit transaction. The "partial-page" window that motivates the LEDGER's
 * `replacePage` never applies here because the payload is a single record; upsert is
 * atomic by construction.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_transfer_details") { … }` so wasmJs
 * collectors re-emit after writes even when Room 3 alpha's async InvalidationTracker
 * fails. On Android/Desktop/iOS the wrap is a microsecond no-op alongside Room's
 * native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. Money-movement flows
 * (`makeTransfer`) do NOT call `store.write(...)` and do NOT touch
 * [TransferDetailDao] directly — they call their existing online repository methods
 * and the server-echoed detail appears here on the next refresh cycle. There is no
 * `Bookkeeper` — this store never queues local writes for retry.
 *
 * ## Empty-observer nullability
 *
 * The DAO reader emits `Flow<TransferDetailEntity?>`; before the first successful
 * fetch the cached row is null. The reader maps null through a `map { row -> row?.toDomain() }`
 * transform, so the Store yields no cached emission on cold cache and the
 * `asScreenStream` consumer stays `ScreenState.Loading` until the network fetch
 * populates a row.
 */
@StoreProvider(id = "transferDetail")
@CacheKey(fn = "of", key = "wallet_transfer_details-{transferId}", params = ["transferId:Long"])
fun provideTransferDetailStore(
    selfManager: SelfServiceApiManager,
    dao: TransferDetailDao,
): Store<TransferDetailKey, TransferDetail> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: TransferDetailKey ->
        // Ktorfit returns Flow<TransferDetail> for getAccountTransfer — take the
        // first (and only) emission. The API path parameter is an Int.
        selfManager.accountTransfersApi
            .getAccountTransfer(key.transferId.toInt())
            .first()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: TransferDetailKey ->
            run { dao.observeById(key.transferId) }
                .map { row -> row?.toDomain() }
        },
        writer = { _: TransferDetailKey, detail: TransferDetail ->
            val stamp = clock()
            // Single-row upsert — Room's implicit transaction handles the atomic
            // replace-on-conflict; no explicit `replacePage` needed because the
            // payload is one record (not a page of rows).
            dao.upsert(detail.toEntity(fetchedAtEpochMs = stamp))
        },
        delete = { key: TransferDetailKey -> dao.deleteById(key.transferId) },
        deleteAll = { dao.deleteAll() },
    ),
)
