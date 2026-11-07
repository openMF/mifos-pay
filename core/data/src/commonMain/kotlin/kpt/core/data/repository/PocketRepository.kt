/*
 * Copyright 2026 Mifos Initiative
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
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
/** Repository for pocket accounts, which group a client's loan, savings and share accounts. */
interface PocketRepository {

    /** Streams the accounts currently linked to the client's pocket. */
    fun getLinkedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>>

    /**
     * Phase-5 Batch-2 **LEDGER read** for the `pocket` archetype (GOAL D13) —
     * returns an offline-first `Flow<ScreenState<List<DetailedPocketAccount>>>`
     * consumed through the `pocket` Store5
     * [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + atomic
     * [`replacePage`][kpt.core.database.wallet.pocket.PocketDao.replacePage]
     * writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream].
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default) — subscribers see the cached page instantly and a
     * background revalidation fires on the Stale/VeryStale band edge.
     *
     * ### Migration from the in-memory cache
     *
     * This method REPLACES the pre-store
     * `getDetailedPocketAccounts(clientId, forceRefresh): Flow<DataState<List<DetailedPocketAccount>>>`
     * that was Phase-4-deferred out of a Store5 migration. The `forceRefresh`
     * parameter is gone — callers refresh by RE-SUBSCRIBING (i.e. wrapping this
     * call in a `MutableSharedFlow<Unit>().flatMapLatest { ... }` in the VM,
     * the same pattern `BeneficiaryListViewModel` uses). The in-memory
     * `MutableStateFlow<DataState<...>>` cache in `PocketRepositoryImp` is
     * REMOVED — Room is the SoT.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. Pocket CRUD writes ([linkAccounts], [delinkAccounts])
     * continue to flow through their existing online paths and appear here on
     * the next refresh cycle. The pre-store optimistic in-memory cache update
     * inside those writes is REMOVED — the momentary "already-updated"
     * impression is replaced by an SWR / manual re-subscribe pull through
     * this store.
     *
     * @param clientId owning client id — the store's page key AND the API path
     *   parameter for the per-client accounts join.
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun getDetailedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>>

    /** Observes the client's linked pocket accounts from the local cache. */
    fun observeLinkedPocketAccounts(clientId: Long): Flow<List<PocketAccount>>

    /** Streams the accounts the client can link to a pocket. */
    fun getAvailableAccountsToLinkStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<List<LinkableAccount>>

    // Writes complete normally on success and throw on failure; the caller's
    // SubmitHandler maps success/exception to SubmitState. The user-facing dialog
    // copy is a feature StringResource surfaced by the ViewModel, not repo copy.
    /** Links accounts to the client's pocket. */
    suspend fun linkAccounts(
        explicitlyAddedAccounts: List<DetailedPocketAccount>,
        clientId: Long,
    )

    /** Delinks accounts from the client's pocket. */
    suspend fun delinkAccounts(
        pocketAccountMappingIds: List<Long>,
        clientId: Long,
    )
}
