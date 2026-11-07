/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.selfaccounts

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.selfaccounts.SelfAccountDao
import kpt.core.database.wallet.selfaccounts.toDomain
import kpt.core.database.wallet.selfaccounts.toEntity
import kpt.core.model.account.Account
import kpt.core.model.network.entity.client.ClientAccountsEntity
import kpt.core.model.savingsaccount.SavingAccountEntity
import kpt.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/**
 * `apiManager.clientsApi.getAccounts(clientId, "savingsAccounts")` `?fields=`
 * value — pulls back only the savings-accounts bucket of a client's account
 * envelope. This mirrors `AccountRepositoryImpl.getSelfAccounts`'s
 * `Constants.SAVINGS` argument (kept inlined here because `core/store` cannot
 * depend on `core/data` where the constant lives).
 */
private const val SAVINGS_FIELDS = "savingsAccounts"

/**
 * Build the LEDGER read [Store] for the authenticated user's own self-accounts
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_self_accounts` [SourceOfTruth]; the network fetcher pulls
 * from `clientsApi.getAccounts(clientId, "savingsAccounts")` and hands the
 * mapped list to the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## API manager choice
 *
 * `SelfServiceApiManager` (not `FineractApiManager`) — parity with
 * `AccountRepositoryImpl.getSelfAccounts`, which routes through the self-service
 * `SelfServiceApiManager.clientsApi`. The Fineract mount exposes the same
 * endpoint but the self-service mount enforces the session-scoped visibility
 * boundary the wallet app expects.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [SelfAccountDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [SelfAccountDao.observeByClient] subscribers never see an empty page mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_self_accounts") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. Savings-account CRUD flows
 * (`createSavingsAccount`, `updateSavingsAccount`, block/unblock) do NOT call
 * `store.write(...)` and do NOT touch [SelfAccountDao] directly — they call
 * their existing online repository methods and the server-echoed record appears
 * here on the next refresh cycle. There is no `Bookkeeper`.
 *
 * ## Inline `ClientAccountsEntity → List<Account>` mapper
 *
 * Mirrors `kpt.core.data.mapper.AccountMapper#toAccount` (which lives in
 * `core/data`, out of reach for a `core/store` module — `core/data` depends on
 * `core/store`, so a reverse import would circle). Kept private + minimal so
 * the store body is self-contained and the fetcher lambda doesn't need to reach
 * across module boundaries. Same pattern as `PocketStore`'s inline mappers.
 */
@StoreProvider(id = "selfAccounts")
@CacheKey(fn = "of", key = "wallet_self_accounts-{clientId}", params = ["clientId:Long"])
fun provideSelfAccountsStore(
    apiManager: SelfServiceApiManager,
    dao: SelfAccountDao,
): Store<SelfAccountsKey, List<Account>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: SelfAccountsKey ->
        // Ktorfit returns Flow<ClientAccountsEntity> for getAccounts — take the
        // first (and only) emission, then map via the inline mapper below.
        apiManager.clientsApi
            .getAccounts(key.clientId, SAVINGS_FIELDS)
            .first()
            .toAccountsDomain()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: SelfAccountsKey ->
            run { dao.observeByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: SelfAccountsKey, accounts: List<Account> ->
            val stamp = clock()
            // ATOMIC page replacement — see [SelfAccountDao.replacePage] KDoc
            // for the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = accounts.map {
                    it.toEntity(clientId = key.clientId, fetchedAtEpochMs = stamp)
                },
            )
        },
        delete = { key: SelfAccountsKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)

// ---------------------------------------------------------------------------
// Inline ClientAccountsEntity → List<Account> mapper — private to this file.
//
// Mirrors `kpt.core.data.mapper.AccountMapper#toAccount`. See the
// `provideSelfAccountsStore` ## Inline mapper section for rationale. Keep the
// two in lockstep if the domain model or the network entity grow fields.
// ---------------------------------------------------------------------------

private fun ClientAccountsEntity.toAccountsDomain(): List<Account> =
    this.savingsAccounts.map { it.toAccountDomain() }

private fun SavingAccountEntity.toAccountDomain(): Account = Account(
    name = productName,
    number = accountNo,
    id = id,
    externalId = externalId,
    balance = accountBalance,
    currency = currency,
    productId = productId,
    productName = productName,
    status = status,
    accountType = accountType,
)
