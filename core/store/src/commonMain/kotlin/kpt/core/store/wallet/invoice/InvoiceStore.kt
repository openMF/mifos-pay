/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.invoice

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.invoice.InvoiceDao
import kpt.core.database.wallet.invoice.toDomain
import kpt.core.database.wallet.invoice.toEntity
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.network.FineractApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `InvoiceEntity`. Shared with the DAO's writes. */
private const val INVOICES_TABLE = "wallet_invoices"

/**
 * Build the LEDGER read [Store] for invoices
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_invoices` [SourceOfTruth]; the network fetcher pulls
 * from `invoiceApi.getInvoices(clientId)` and hands the mapped list to
 * the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## API manager choice
 *
 * `FineractApiManager` (not `SelfServiceApiManager`) — parity with
 * `InvoiceRepositoryImpl`. The invoice datatable endpoint lives on the
 * fineract base URL, not the self-service mount.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [InvoiceDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [InvoiceDao.observeByClient] subscribers never see an empty page mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_invoices") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing invoice
 * management flows (`createInvoice`, `updateInvoice`, `deleteInvoice`) do NOT
 * call `store.write(...)` and do NOT touch [InvoiceDao] directly — they call
 * their existing online repository methods and the server-echoed row appears
 * here on the next refresh cycle. There is no `Bookkeeper`.
 *
 * ## Detail-screen note
 *
 * `InvoiceDetailViewModel` still consumes the transitional
 * `InvoiceRepository.getInvoice(clientId, invoiceId)` streaming shim — no
 * SINGLE-ROW-PER-KEY detail store is emitted in this batch (the endpoint
 * returns the WHOLE row via the same datatable, so a detail-only fetch
 * against a warm invoices cache adds negligible caching value; if desired
 * later, add an `InvoiceDetailStore` mirroring `AccountDetailStore`'s shape).
 */
@StoreProvider(id = "invoice")
@CacheKey(fn = "of", key = "wallet_invoices-{clientId}", params = ["clientId:Long"])
fun provideInvoiceStore(
    apiManager: FineractApiManager,
    dao: InvoiceDao,
): Store<InvoiceKey, List<Invoice>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: InvoiceKey ->
        // Ktorfit returns Flow<List<Invoice>> — take the first (and only) emission.
        apiManager.invoiceApi.getInvoices(key.clientId).first()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: InvoiceKey ->
            run { dao.observeByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: InvoiceKey, invoices: List<Invoice> ->
            val stamp = clock()
            // ATOMIC page replacement — see [InvoiceDao.replacePage] KDoc for
            // the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = invoices.map { it.toEntity(fetchedAtEpochMs = stamp) },
            )
        },
        delete = { key: InvoiceKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)
