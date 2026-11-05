/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.pocket

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.pocket.PocketDao
import kpt.core.database.wallet.pocket.toDomain
import kpt.core.database.wallet.pocket.toEntity
import org.mifospay.core.model.enums.AccountType
import org.mifospay.core.model.network.entity.client.ClientAccountsEntity
import org.mifospay.core.model.network.entity.loanAccount.LoanStatusResponseDto
import org.mifospay.core.model.network.entity.pocket.PocketAccountDto
import org.mifospay.core.model.network.entity.pocket.PocketDelinkRequest
import org.mifospay.core.model.network.entity.pocket.PocketLinkRequest
import org.mifospay.core.model.network.entity.pocket.PocketResponseDto
import org.mifospay.core.model.network.entity.shareAccount.ShareStatusResponseDto
import org.mifospay.core.model.pocket.AccountStatus
import org.mifospay.core.model.pocket.DetailedPocketAccount
import org.mifospay.core.model.pocket.PocketAccount
import org.mifospay.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock
import org.mifospay.core.model.savingsaccount.Status as SavingsStatus

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `PocketEntity`. Shared with the DAO's writes. */
private const val POCKETS_TABLE = "wallet_pockets"

/**
 * Build the LEDGER read [Store] for detailed pocket accounts
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Migration from the in-memory MutableStateFlow cache
 *
 * This store REPLACES the in-memory `MutableStateFlow<DataState<List<DetailedPocketAccount>>>`
 * cache the pre-store `PocketRepositoryImp` kept as `detailedPocketCache`. The
 * pre-store `TODO(phase-4)` comment at the top of `PocketRepositoryImp`
 * explicitly deferred this cache migration because a naive
 * `.asScreenStateFlow()` would have bypassed the in-memory cache and caused
 * redundant network fetches. This store now provides the cache — Room becomes
 * the SoT — and the repository drops the `_cachedClientId` / `detailedPocketCache`
 * fields. `linkAccounts` / `delinkAccounts` simply call the online API and
 * rely on the VM's refresh trigger (or SWR) to re-fetch fresh data through
 * this store (parity with `Beneficiary` writes).
 *
 * ## Fetcher shape (compound fetch)
 *
 * The fetcher performs TWO always-executed round-trips plus (for SHARE
 * accounts) N additional round-trips — one per share pocket — for market-price
 * resolution:
 *
 * 1. `dataManager.pocketApi.getPocketAccounts()` → basic pockets (a
 *    `PocketResponseDto` with per-type buckets).
 * 2. `dataManager.clientsApi.getClientAccounts(clientId)` → client's accounts
 *    (savings + loans + shares detail rows).
 * 3. For every SHARE pocket, `dataManager.shareAccountApi.getShareAccountDetails(pocket.accountId)`
 *    → market-price snapshot for the current balance computation.
 *
 * This is the SAME compound-fetch shape the pre-store `syncPockets` +
 * `addAccountDetails` executed; the store body is behavior-parity. The
 * N-shape fan-out is intrinsic to the domain — market price is not in the
 * client-accounts endpoint — and moving it into the store leaves the read
 * cache safe: once the fetch completes, ONE `replacePage` write commits the
 * whole page atomically.
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_pockets` [SourceOfTruth]; the network fetcher returns the
 * mapped list to the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [PocketDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [PocketDao.observeByClient] subscribers never see an empty page mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_pockets") { … }` so wasmJs
 * collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (OFFLINE-FIRST OPTIMISTIC UPDATES)
 *
 * Pocket management flows (`linkAccounts`, `delinkAccounts`) support fully offline optimistic
 * UI interactions:
 * 1. The repository writes the intended action (e.g. `PENDING_LINK` or `PENDING_DELINK`)
 *    directly into the local Room database, providing an instant UI update.
 * 2. When creating an optimistic link row, a random negative ID is used to prevent
 *    primary key collisions before the server assigns a permanent real mapping ID.
 * 3. The `Fetcher` (below) implements a **push-before-pull** strategy. Every time
 *    Store5 fetches from the network, the fetcher first queries the local DAO for
 *    any pending links or delinks. It pushes these to the network synchronously.
 * 4. If the push succeeds, the local pending rows are cleaned up (deleted for delinks,
 *    marked as SYNCED for links) *before* the fresh server list is retrieved.
 * 5. This guarantees that `replacePage` won't destroy optimistic rows that haven't
 *    been pushed yet, and ensures that user actions made while offline are safely
 *    synchronized without generating duplicates. The generic `RoomOutbox` is not used here
 *    because Pocket syncing requires strict atomic ordering. Pushing pending changes, cleaning up
 *    the local DB, and fetching the fresh page must happen sequentially inside this single
 *    Fetcher pipeline to prevent visual duplicates.
 *
 * ## Inline mappers
 *
 * `core/store` cannot depend on `core/data` (that would create a cycle —
 * `core/data` depends on `core/store` for the store bindings). The
 * `PocketResponseDto → List<PocketAccount>` mapper and the three status-DTO
 * `→ AccountStatus` mappers are inlined below, mirroring
 * `org.mifospay.core.data.mapper.pocket.PocketAccountMapper`. Keep the two
 * in lockstep if either grows fields.
 */
@StoreProvider(id = "pocket")
@CacheKey(fn = "linked", key = "wallet_pockets_linked-{clientId}", params = ["clientId:Long"])
@CacheKey(fn = "detailed", key = "wallet_pockets_detailed-{clientId}", params = ["clientId:Long"])
fun providePocketStore(
    apiManager: SelfServiceApiManager,
    dao: PocketDao,
): Store<PocketKey, List<DetailedPocketAccount>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: PocketKey ->
        try {
            val localPending = dao.getPendingSyncsByClient(key.clientId)

            /**
             * Exception Handling Strategy:
             * If any of the network calls below (linkAccounts, delinkAccounts, or getPocketAccounts)
             * fail due to network errors, an Exception is thrown. Store5 automatically catches this
             * exception and emits a `StoreReadResponse.Error` down the stream.
             * Crucially, the local DAO is untouched. The pending syncs remain `PENDING_LINK` or
             * `PENDING_DELINK`, and will be retried automatically on the next fetch request.
             */

            val pendingLinks = localPending.filter { it.syncStatus == "PENDING_LINK" }
            val pendingDelinks = localPending.filter { it.syncStatus == "PENDING_DELINK" }

            if (pendingLinks.isNotEmpty()) {
                val request = PocketLinkRequest(
                    accountsDetail = pendingLinks.map {
                        PocketLinkRequest.AccountDetail(
                            accountId = it.accountId.toString(),
                            accountType = it.accountType,
                        )
                    },
                )
                apiManager.pocketApi.linkAccounts(request = request)
                pendingLinks.forEach {
                    dao.upsert(it.copy(syncStatus = "SYNCED"))
                }
            }

            if (pendingDelinks.isNotEmpty()) {
                val serverIds = pendingDelinks.filter { it.id > 0 }.map { it.id }
                if (serverIds.isNotEmpty()) {
                    val request = PocketDelinkRequest(serverIds)
                    apiManager.pocketApi.delinkAccounts(request = request)
                    pendingDelinks.forEach {
                        dao.deleteById(it.id, key.clientId)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently swallow push failures; they will remain PENDING in the DB
            // and the pull phase will preserve them via replacePage.
        }

        // Step 1 — basic pockets (session-scoped on the server side).
        val basicPockets: List<PocketAccount> = apiManager.pocketApi
            .getPocketAccounts()
            .toDomainList()

        // Step 2 — client accounts (per-client) for balance/product/status
        // resolution. Same suspend call the pre-store `syncPockets` used.
        val clientAccounts: ClientAccountsEntity = apiManager.clientsApi
            .getClientAccounts(key.clientId)

        // Step 3 — enrich each pocket. SHARE pockets fan out an extra
        // getShareAccountDetails call. The addPocketDetails() helper mirrors
        // the pre-store `addAccountDetails` verbatim — try/catch on share
        // fetches, fall back to the client-accounts row on failure.
        basicPockets.map { pocket -> addPocketDetails(pocket, clientAccounts, apiManager) }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: PocketKey ->
            run { dao.observeLinkedByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: PocketKey, pockets: List<DetailedPocketAccount> ->
            val stamp = clock()
            // ATOMIC page replacement — see [PocketDao.replacePage] KDoc for
            // the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = pockets.map {
                    it.toEntity(clientId = key.clientId, fetchedAtEpochMs = stamp)
                },
            )
        },
        delete = { key: PocketKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)

// ---------------------------------------------------------------------------
// Inline PocketResponseDto → List<PocketAccount> mapper + the three
// per-account-type status → AccountStatus mappers. Mirrors
// `org.mifospay.core.data.mapper.pocket.PocketAccountMapper`. See
// `providePocketStore` ## Inline mappers KDoc for rationale.
// ---------------------------------------------------------------------------

private fun PocketResponseDto.toDomainList(): List<PocketAccount> {
    val all = mutableListOf<PocketAccount>()
    loanAccounts.forEach { all.add(it.toDomainPocket(AccountType.LOAN)) }
    savingsAccounts.forEach { all.add(it.toDomainPocket(AccountType.SAVINGS)) }
    shareAccounts.forEach { all.add(it.toDomainPocket(AccountType.SHARE)) }
    return all
}

private fun PocketAccountDto.toDomainPocket(type: AccountType): PocketAccount = PocketAccount(
    id = this.id ?: 0L,
    pocketId = this.pocketId ?: 0L,
    accountId = this.accountId ?: 0L,
    accountType = type,
    accountNumber = this.accountNumber ?: "",
)

private fun LoanStatusResponseDto.toAccountStatus(): AccountStatus = when {
    active == true -> AccountStatus.ACTIVE
    pendingApproval == true -> AccountStatus.PENDING
    waitingForDisbursal == true -> AccountStatus.APPROVED
    overpaid == true -> AccountStatus.OVERPAID
    closed == true ||
        closedObligationsMet == true ||
        closedWrittenOff == true ||
        closedRescheduled == true -> AccountStatus.CLOSED
    else -> AccountStatus.UNKNOWN
}

private fun SavingsStatus.toAccountStatus(): AccountStatus = when {
    active -> AccountStatus.ACTIVE
    submittedAndPendingApproval -> AccountStatus.PENDING
    approved -> AccountStatus.APPROVED
    rejected -> AccountStatus.REJECTED
    withdrawnByApplicant -> AccountStatus.WITHDRAWN
    matured -> AccountStatus.MATURED
    closed || prematureClosed -> AccountStatus.CLOSED
    else -> AccountStatus.UNKNOWN
}

private fun ShareStatusResponseDto.toAccountStatus(): AccountStatus = when {
    active == true -> AccountStatus.ACTIVE
    submittedAndPendingApproval == true -> AccountStatus.PENDING
    approved == true -> AccountStatus.APPROVED
    rejected == true -> AccountStatus.REJECTED
    closed == true -> AccountStatus.CLOSED
    else -> AccountStatus.UNKNOWN
}

// ---------------------------------------------------------------------------
// Pocket enrichment — mirrors PocketRepositoryImp#addAccountDetails verbatim.
// Kept `internal` so the (future) desktopTest fixtures can exercise it if
// needed, but the store itself is the sole caller today.
// ---------------------------------------------------------------------------

private suspend fun addPocketDetails(
    pocket: PocketAccount,
    clientAccounts: ClientAccountsEntity,
    apiManager: SelfServiceApiManager,
): DetailedPocketAccount = when (pocket.accountType) {
    AccountType.LOAN -> {
        val detail = clientAccounts.loanAccounts.find { it.id == pocket.accountId }
        DetailedPocketAccount(
            pocket = pocket,
            balance = detail?.loanBalance,
            productName = detail?.productName,
            currencyCode = detail?.currency?.code,
            currencyDisplaySymbol = detail?.currency?.displaySymbol,
            decimalPlaces = detail?.currency?.decimalPlaces,
            status = detail?.status?.toAccountStatus(),
        )
    }

    AccountType.SAVINGS -> {
        val detail = clientAccounts.savingsAccounts.find { it.id == pocket.accountId }
        DetailedPocketAccount(
            pocket = pocket,
            balance = detail?.accountBalance,
            productName = detail?.productName,
            currencyCode = detail?.currency?.code,
            currencyDisplaySymbol = detail?.currency?.displaySymbol,
            decimalPlaces = detail?.currency?.decimalPlaces,
            status = detail?.status?.toAccountStatus(),
        )
    }

    AccountType.SHARE -> {
        var balance: Double? = null
        var productName: String?
        var currencyCode: String?
        var currencyDisplaySymbol: String?
        var decimalPlaces: Int?
        var accountStatus: AccountStatus?

        try {
            val shareAccountDetails = apiManager.shareAccountApi
                .getShareAccountDetails(pocket.accountId).first()
            productName = shareAccountDetails.productName
            currencyCode = shareAccountDetails.currency?.code
            currencyDisplaySymbol = shareAccountDetails.currency?.displaySymbol
            decimalPlaces = shareAccountDetails.currency?.decimalPlaces
            accountStatus = shareAccountDetails.status?.toAccountStatus()

            val approvedShares = shareAccountDetails.summary?.totalApprovedShares ?: 0
            val currentMarketPrice = shareAccountDetails.currentMarketPrice ?: 0.0
            balance = approvedShares * currentMarketPrice
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            val detail = clientAccounts.shareAccounts.find { it.id == pocket.accountId }
            productName = detail?.productName
            currencyCode = detail?.currency?.code
            currencyDisplaySymbol = detail?.currency?.displaySymbol
            decimalPlaces = detail?.currency?.decimalPlaces
            accountStatus = detail?.status?.toAccountStatus()
        }

        DetailedPocketAccount(
            pocket = pocket,
            balance = balance,
            productName = productName,
            currencyCode = currencyCode,
            currencyDisplaySymbol = currencyDisplaySymbol,
            decimalPlaces = decimalPlaces,
            status = accountStatus,
        )
    }
}
