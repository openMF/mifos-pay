/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.InvoiceRepository
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.model.datatables.invoice.InvoiceEntity
import kpt.core.network.FineractApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.invoice.InvoiceKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

class InvoiceRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-2 LEDGER wiring — injected by RepositoryModule so the
    // store-backed getInvoicesScreen(...) can consume Store5 via
    // asScreenStream(...). Nullable-default so existing unit tests without
    // the store harness continue to compile; getInvoices(...) — the legacy
    // asScreenStateFlow path — is unaffected.
    private val invoiceStore: Store<InvoiceKey, List<Invoice>>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
) : InvoiceRepository {
    override fun getInvoice(clientId: Long, invoiceId: Long): Flow<ScreenState<Invoice>> {
        return apiManager.invoiceApi
            .getInvoice(clientId, invoiceId)
            .map { it.first() }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override fun getInvoices(clientId: Long): Flow<ScreenState<List<Invoice>>> {
        return apiManager.invoiceApi
            .getInvoices(clientId)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-2 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three store-adapter
    // dependencies (invoiceStore + NetworkMonitor + FetchedAtRepository).
    // If any is null (test wiring), we IllegalState — production DI in
    // RepositoryModule wires all three unconditionally.
    override fun getInvoicesStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Invoice>> {
        val store = checkNotNull(invoiceStore) {
            "getInvoicesStream requires the `invoice` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.Invoice and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getInvoicesStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getInvoicesStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = InvoiceKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.Invoice.of(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.INVOICE,
        )
    }

    override suspend fun createInvoice(clientId: Long, invoice: InvoiceEntity) {
        withContext(ioDispatcher) {
            apiManager.invoiceApi.addInvoice(clientId, invoice)
        }
    }

    override suspend fun updateInvoice(
        clientId: Long,
        invoiceId: Long,
        invoice: InvoiceEntity,
    ) {
        withContext(ioDispatcher) {
            apiManager.invoiceApi.updateInvoice(clientId, invoiceId, invoice)
        }
    }

    override suspend fun deleteInvoice(clientId: Long, invoiceId: Long) {
        withContext(ioDispatcher) {
            apiManager.invoiceApi.deleteInvoice(clientId, invoiceId)
        }
    }
}
