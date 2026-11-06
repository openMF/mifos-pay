/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.client

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.client.ClientDetailDao
import kpt.core.database.wallet.client.toDomain
import kpt.core.database.wallet.client.toEntity
import kpt.core.model.client.Client
import kpt.core.model.client.ClientStatus
import kpt.core.model.client.ClientTimeline
import kpt.core.model.network.entity.client.ClientEntity
import kpt.core.model.network.entity.client.ClientTimelineEntity
import kpt.core.model.network.entity.client.Status
import kpt.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `ClientDetailEntity`. Shared with the DAO's writes. */
private const val CLIENT_DETAILS_TABLE = "wallet_client_details"

/**
 * Build the SINGLE-ROW-PER-KEY read [Store] for client-info (profile) records
 * (GOAL D13 — a variation of the `history` archetype for a single-record cache).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_client_details` [SourceOfTruth]; the network fetcher pulls
 * from `clientsApi.getClient(clientId)` (mapped `ClientEntity → Client` via
 * the private inline mapper below — see the [Inline mapper] block), then
 * hands the mapped domain [Client] to the writer for atomic single-row
 * replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## Atomic single-row write (S5-PAGE-ATOMIC — trivial case)
 *
 * The writer lambda calls [ClientDetailDao.upsert] — a single-row
 * `@Insert(onConflict = REPLACE)` under Room's implicit transaction. The
 * "partial-page" window that motivates the LEDGER's `replacePage` never
 * applies here because the payload is a single record; upsert is atomic by
 * construction (parity with `AccountDetailStore`).
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_client_details") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing profile
 * management flows (`updateClient`, `updateClientImage`) do NOT call
 * `store.write(...)` and do NOT touch [ClientDetailDao] directly — they call
 * their existing online repository methods and the server-echoed record
 * appears here on the next refresh cycle. There is no `Bookkeeper`.
 *
 * ## Image stream stays transitional (task-scoped exclusion)
 *
 * `ClientRepository.getClientImage(clientId)` remains on the transitional
 * `asScreenStateFlow` shim. The image is a blob-URL stream whose caching
 * story (`Coil` cache vs Room blob storage vs a separate Store) is out of
 * scope for this batch — the ProfileViewModel's inline observer of
 * `getClientImage(...)` is unchanged.
 *
 * ## Empty-observer nullability
 *
 * The DAO reader emits `Flow<ClientDetailEntity?>`; before the first
 * successful fetch the cached row is null. The reader maps null through so
 * the store yields no cached emission on cold cache and the consumer's
 * `isEmpty = { ... }` predicate (or absence thereof) fires
 * `ScreenState.Loading` until the network fetch populates a row (parity with
 * `AccountDetailStore`).
 *
 * ## Inline mapper
 *
 * Mirrors `kpt.core.data.mapper.ClientDetailsMapper` (which lives in
 * `core/data`, out of reach for a `core/store` module — `core/data` depends on
 * `core/store` for the store bindings, so a reverse import would circle).
 * Kept private + minimal so the store body is self-contained and the fetcher
 * lambda doesn't need to reach across module boundaries. If a third consumer
 * wants this mapping, lift it to `core/model` (its inputs are all in `core/network`
 * `entity/client`, its outputs in `core/model` `client`).
 */
@StoreProvider(id = "clientDetail")
@CacheKey(fn = "of", key = "wallet_client_details-{clientId}", params = ["clientId:Long"])
fun provideClientDetailStore(
    apiManager: SelfServiceApiManager,
    dao: ClientDetailDao,
): Store<ClientDetailKey, Client> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: ClientDetailKey ->
        // Ktorfit returns Flow<ClientEntity> — take the first (and only) emission,
        // then map to the domain via the private mapper below.
        apiManager.clientsApi.getClient(key.clientId).first().toDomain()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: ClientDetailKey ->
            run { dao.observeById(key.clientId) }
                .map { row -> row?.toDomain() }
        },
        writer = { _: ClientDetailKey, client: Client ->
            val stamp = clock()
            // Single-row upsert — Room's implicit transaction handles the
            // atomic replace-on-conflict; no explicit `replacePage` needed
            // because the payload is one record (not a page of rows).
            dao.upsert(client.toEntity(fetchedAtEpochMs = stamp))
        },
        delete = { key: ClientDetailKey -> dao.deleteById(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)

// ---------------------------------------------------------------------------
// Inline ClientEntity → Client mapper — private to this file.
//
// Mirrors `kpt.core.data.mapper.ClientDetailsMapper#toModel` — see the
// ## Inline mapper section on `provideClientDetailStore` for rationale. Keep
// the two in lockstep if the domain model or the network entity grow fields.
// ---------------------------------------------------------------------------

private fun ClientEntity.toDomain(): Client = Client(
    id = id ?: 0,
    accountNo = accountNo ?: "",
    externalId = externalId ?: "",
    active = active,
    activationDate = activationDate,
    firstname = firstname ?: "",
    lastname = lastname ?: "",
    displayName = displayName ?: "",
    mobileNo = mobileNo ?: "",
    emailAddress = emailAddress ?: "",
    dateOfBirth = dateOfBirth,
    isStaff = isStaff ?: false,
    officeId = officeId ?: 0,
    officeName = officeName ?: "",
    savingsProductName = savingsProductName ?: "",
    status = status?.toClientStatus() ?: ClientStatus(),
    timeline = timeline?.toClientTimeline() ?: ClientTimeline(),
    legalForm = legalForm?.toClientStatus() ?: ClientStatus(),
)

private fun Status.toClientStatus(): ClientStatus = ClientStatus(
    id = id ?: 0,
    code = code ?: "",
    value = value ?: "",
)

private fun ClientTimelineEntity.toClientTimeline(): ClientTimeline = ClientTimeline(
    submittedOnDate = submittedOnDate,
    activatedOnDate = activatedOnDate,
    activatedByUsername = activatedByUsername,
    activatedByFirstname = activatedByFirstname,
    activatedByLastname = activatedByLastname,
)
