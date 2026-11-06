/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.beneficiary

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.beneficiary.BeneficiaryDao
import kpt.core.database.wallet.beneficiary.toDomain
import kpt.core.database.wallet.beneficiary.toEntity
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `BeneficiaryEntity`. Shared with the DAO's writes. */
private const val BENEFICIARIES_TABLE = "wallet_beneficiaries"

/**
 * Build the LEDGER read [Store] for beneficiary lists
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_beneficiaries` [SourceOfTruth]; the network fetcher pulls
 * from `beneficiaryApi.beneficiaryList()` and hands the mapped list to the
 * writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## Cache-key scoping (no server-side per-client filter)
 *
 * The Fineract `beneficiaryList()` endpoint returns the list for the
 * authenticated session (no `clientId` path parameter). This store still keys on
 * `BeneficiaryKey(clientId)` so the DAO's per-client page semantics prevent a
 * logged-out-then-logged-in-as-different-client session from surfacing the
 * previous user's cached list. The mapper stamps [BeneficiaryKey.clientId] into
 * each persisted row at write time (see `Beneficiary.toEntity(clientId, ...)`).
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [BeneficiaryDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [BeneficiaryDao.observeByClient] subscribers never see an empty page mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_beneficiaries") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing beneficiary
 * management flows (`createBeneficiary`, `updateBeneficiary`, `deleteBeneficiary`)
 * do NOT call `store.write(...)` and do NOT touch [BeneficiaryDao] directly —
 * they call their existing online repository methods and the server-echoed row
 * appears here on the next refresh cycle. There is no `Bookkeeper`.
 */
@StoreProvider(id = "beneficiary")
@CacheKey(fn = "of", key = "wallet_beneficiaries-{clientId}", params = ["clientId:Long"])
fun provideBeneficiaryStore(
    apiManager: SelfServiceApiManager,
    dao: BeneficiaryDao,
): Store<BeneficiaryKey, List<Beneficiary>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: BeneficiaryKey ->
        // Ktorfit returns Flow<List<Beneficiary>> — take the first (and only) emission.
        apiManager.beneficiaryApi.beneficiaryList().first()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: BeneficiaryKey ->
            run { dao.observeByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: BeneficiaryKey, beneficiaries: List<Beneficiary> ->
            val stamp = clock()
            // ATOMIC page replacement — see [BeneficiaryDao.replacePage] KDoc for
            // the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = beneficiaries.map { it.toEntity(clientId = key.clientId, fetchedAtEpochMs = stamp) },
            )
        },
        delete = { key: BeneficiaryKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)
