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
import kpt.core.data.repository.SavedCardRepository
import kpt.core.model.savedcards.CardPayload
import kpt.core.model.savedcards.SavedCard
import kpt.core.network.FineractApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.savedcards.SavedCardKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

/** Default [SavedCardRepository] backed by the saved-card API. */
class SavedCardRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-1 LEDGER wiring — injected by RepositoryModule so the
    // store-backed getSavedCardsScreen(...) can consume Store5 via
    // asScreenStream(...). Nullable-default so existing unit tests without
    // the store harness continue to compile; getSavedCards(...) — the legacy
    // asScreenStateFlow path — is unaffected.
    private val savedCardStore: Store<SavedCardKey, List<SavedCard>>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
) : SavedCardRepository {
    override fun getSavedCards(clientId: Long): Flow<ScreenState<List<SavedCard>>> {
        return apiManager.savedCardApi
            .getSavedCards(clientId)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-1 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three store-adapter
    // dependencies (savedCardStore + NetworkMonitor + FetchedAtRepository).
    // If any is null (test wiring), we IllegalState — production DI in
    // RepositoryModule wires all three unconditionally.
    override fun getSavedCardsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<SavedCard>> {
        val store = checkNotNull(savedCardStore) {
            "getSavedCardsStream requires the `savedCards` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.SavedCards and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getSavedCardsStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getSavedCardsStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = SavedCardKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.SavedCards.of(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.SAVED_CARDS,
        )
    }

    override fun getSavedCard(clientId: Long, cardId: Long): Flow<ScreenState<SavedCard>> {
        return apiManager.savedCardApi
            .getSavedCard(clientId, cardId)
            .map { it.first() }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun addSavedCard(clientId: Long, card: CardPayload) {
        withContext(ioDispatcher) {
            apiManager.savedCardApi.addSavedCard(clientId, card)
        }
    }

    override suspend fun deleteCard(clientId: Long, cardId: Long) {
        withContext(ioDispatcher) {
            apiManager.savedCardApi.deleteCard(clientId, cardId)
        }
    }

    override suspend fun updateCard(
        clientId: Long,
        cardId: Long,
        card: CardPayload,
    ) {
        withContext(ioDispatcher) {
            apiManager.savedCardApi.updateCard(clientId, cardId, card)
        }
    }
}
