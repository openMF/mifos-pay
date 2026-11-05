/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.savedcards

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.savedcards.SavedCardDao
import kpt.core.database.wallet.savedcards.toDomain
import kpt.core.database.wallet.savedcards.toEntity
import org.mifospay.core.model.savedcards.SavedCard
import org.mifospay.core.network.FineractApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `SavedCardEntity`. Shared with the DAO's writes. */
private const val SAVED_CARDS_TABLE = "wallet_saved_cards"

/**
 * Build the LEDGER read [Store] for saved payment cards
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_saved_cards` [SourceOfTruth]; the network fetcher pulls
 * from `savedCardApi.getSavedCards(clientId)` and hands the mapped list to
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
 * `SavedCardRepositoryImpl`. The datatable endpoint lives on the fineract
 * base URL, not the self-service mount.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [SavedCardDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [SavedCardDao.observeByClient] subscribers never see an empty page mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_saved_cards") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing card
 * management flows (`addSavedCard`, `updateCard`, `deleteCard`) do NOT call
 * `store.write(...)` and do NOT touch [SavedCardDao] directly — they call
 * their existing online repository methods and the server-echoed row appears
 * here on the next refresh cycle. There is no `Bookkeeper`.
 */
@StoreProvider(id = "savedCards")
@CacheKey(fn = "of", key = "wallet_saved_cards-{clientId}", params = ["clientId:Long"])
fun provideSavedCardStore(
    apiManager: FineractApiManager,
    dao: SavedCardDao,
): Store<SavedCardKey, List<SavedCard>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: SavedCardKey ->
        // Ktorfit returns Flow<List<SavedCard>> — take the first (and only) emission.
        apiManager.savedCardApi.getSavedCards(key.clientId).first()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: SavedCardKey ->
            run { dao.observeByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: SavedCardKey, cards: List<SavedCard> ->
            val stamp = clock()
            // ATOMIC page replacement — see [SavedCardDao.replacePage] KDoc for
            // the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = cards.map { it.toEntity(fetchedAtEpochMs = stamp) },
            )
        },
        delete = { key: SavedCardKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)
