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
import kpt.core.model.savedcards.CardPayload
import kpt.core.model.savedcards.SavedCard
interface SavedCardRepository {
    // Phase-3 cutover — reads on ScreenState.
    fun getSavedCards(clientId: Long): Flow<ScreenState<List<SavedCard>>>

    /**
     * Phase-5 Batch-1 **LEDGER read** for the `savedCards` archetype (GOAL D13) —
     * returns an offline-first `Flow<ScreenState<List<SavedCard>>>` consumed
     * through the `savedCards` Store5
     * [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + atomic
     * [`replacePage`][kpt.core.database.wallet.savedcards.SavedCardDao.replacePage]
     * writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream] — the
     * Store5-native bridge, distinct from the transitional
     * [`asScreenStateFlow`][kpt.core.common.asScreenStateFlow] the
     * legacy [getSavedCards] path uses.
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default) — subscribers see the cached page instantly and a
     * background revalidation fires on the Stale/VeryStale band edge.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. Card CRUD writes ([addSavedCard], [updateCard],
     * [deleteCard]) continue to flow through their existing online paths and
     * appear here on the next refresh cycle — the LEDGER cache is server-truth.
     *
     * @param clientId owning client id — the store's page key AND the API path
     *   parameter.
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun getSavedCardsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<SavedCard>>

    fun getSavedCard(clientId: Long, cardId: Long): Flow<ScreenState<SavedCard>>

    // Writes complete normally on success and throw on failure; the caller's
    // SubmitHandler maps success/exception to SubmitState. The user-facing success
    // message is a feature StringResource surfaced by the ViewModel, not repo copy.
    suspend fun addSavedCard(clientId: Long, card: CardPayload)

    suspend fun deleteCard(clientId: Long, cardId: Long)

    suspend fun updateCard(clientId: Long, cardId: Long, card: CardPayload)
}
