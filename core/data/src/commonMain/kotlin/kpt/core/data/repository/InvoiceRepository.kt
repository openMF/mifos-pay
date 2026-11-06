/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.common.ScreenState
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.model.datatables.invoice.InvoiceEntity
interface InvoiceRepository {
    // Phase-3 cutover — reads on ScreenState.
    fun getInvoice(clientId: Long, invoiceId: Long): Flow<ScreenState<Invoice>>

    fun getInvoices(clientId: Long): Flow<ScreenState<List<Invoice>>>

    /**
     * Phase-5 Batch-2 **LEDGER read** for the `invoice` archetype (GOAL D13) —
     * returns an offline-first `Flow<ScreenState<List<Invoice>>>` consumed
     * through the `invoice` Store5
     * [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + atomic
     * [`replacePage`][kpt.core.database.wallet.invoice.InvoiceDao.replacePage]
     * writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream] — the
     * Store5-native bridge, distinct from the transitional
     * [`asScreenStateFlow`][kpt.core.common.asScreenStateFlow] the
     * legacy [getInvoices] path uses.
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default) — subscribers see the cached page instantly and a
     * background revalidation fires on the Stale/VeryStale band edge.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. Invoice CRUD writes ([createInvoice], [updateInvoice],
     * [deleteInvoice]) continue to flow through their existing online paths and
     * appear here on the next refresh cycle — the LEDGER cache is server-truth.
     *
     * @param clientId owning client id — the store's page key AND the API path
     *   parameter (`GET /datatables/invoice/{clientId}`).
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun getInvoicesStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Invoice>>

    // Writes complete normally on success and throw on failure; the caller's
    // SubmitHandler maps success/exception to SubmitState. The user-facing success
    // message is a feature StringResource surfaced by the ViewModel, not repo copy.
    suspend fun createInvoice(clientId: Long, invoice: InvoiceEntity)

    suspend fun updateInvoice(
        clientId: Long,
        invoiceId: Long,
        invoice: InvoiceEntity,
    )

    suspend fun deleteInvoice(clientId: Long, invoiceId: Long)
}
