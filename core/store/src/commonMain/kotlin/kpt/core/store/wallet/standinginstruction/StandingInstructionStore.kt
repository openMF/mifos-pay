/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.standinginstruction

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.standinginstruction.StandingInstructionDao
import kpt.core.database.wallet.standinginstruction.toDomain
import kpt.core.database.wallet.standinginstruction.toEntity
import org.mifospay.core.model.standinginstruction.StandingInstruction
import org.mifospay.core.network.FineractApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `StandingInstructionEntity`. Shared with the DAO's writes. */
private const val STANDING_INSTRUCTIONS_TABLE = "wallet_standing_instructions"

/**
 * Build the LEDGER read [Store] for standing instructions
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_standing_instructions` [SourceOfTruth]; the network fetcher
 * pulls from `standingInstructionApi.getAllStandingInstructions(clientId)`
 * (which returns `Flow<Page<StandingInstruction>>`), strips the
 * `pageItems` list out of the Fineract [org.mifospay.core.model.network.entity.Page]
 * envelope, and hands the mapped list to the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## API manager choice
 *
 * `FineractApiManager` (not `SelfServiceApiManager`) — parity with
 * `StandingInstructionRepositoryImpl`. The standing-instruction endpoint lives
 * on the fineract base URL.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [StandingInstructionDao.replacePage] — a single
 * `@Transaction` that delete-then-upserts under one SQLite transaction. In-flight
 * [StandingInstructionDao.observeByClient] subscribers never see an empty page
 * mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_standing_instructions") { … }`
 * so wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing standing-
 * instruction management flows (`createStandingInstruction`,
 * `updateStandingInstruction`, `deleteStandingInstruction`) do NOT call
 * `store.write(...)` and do NOT touch [StandingInstructionDao] directly — they
 * call their existing online repository methods and the server-echoed row
 * appears here on the next refresh cycle. There is no `Bookkeeper`.
 *
 * ## Detail-screen note
 *
 * `SIDetailViewModel` still consumes the transitional
 * `StandingInstructionRepository.getStandingInstruction(instructionId)`
 * streaming shim — no SINGLE-ROW-PER-KEY detail store is emitted in this batch.
 * The list cache above is enough to make the SI list screen offline-first;
 * detail views hit the network directly.
 */
@StoreProvider(id = "standingInstruction")
@CacheKey(fn = "of", key = "wallet_standing_instructions-{clientId}", params = ["clientId:Long"])
fun provideStandingInstructionStore(
    apiManager: FineractApiManager,
    dao: StandingInstructionDao,
): Store<StandingInstructionKey, List<StandingInstruction>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: StandingInstructionKey ->
        // Ktorfit returns Flow<Page<StandingInstruction>> — take the first (and
        // only) emission and strip the Page envelope to List (parity with
        // StandingInstructionRepositoryImpl.getAllStandingInstructions).
        apiManager.standingInstructionApi
            .getAllStandingInstructions(key.clientId)
            .first()
            .pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: StandingInstructionKey ->
            run { dao.observeByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: StandingInstructionKey, instructions: List<StandingInstruction> ->
            val stamp = clock()
            // ATOMIC page replacement — see [StandingInstructionDao.replacePage]
            // KDoc for the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = instructions.map {
                    it.toEntity(clientId = key.clientId, fetchedAtEpochMs = stamp)
                },
            )
        },
        delete = { key: StandingInstructionKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)
