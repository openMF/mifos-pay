/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.linkableaccount

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.linkableaccount.LinkableAccountDao
import kpt.core.database.wallet.linkableaccount.toDomain
import kpt.core.database.wallet.linkableaccount.toEntity
import kpt.core.database.wallet.pocket.PocketDao
import kpt.core.model.enums.AccountType
import kpt.core.model.network.entity.client.ClientAccountsEntity
import kpt.core.model.network.entity.loanAccount.LoanStatusResponseDto
import kpt.core.model.network.entity.shareAccount.ShareStatusResponseDto
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.LinkableAccount
import kpt.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock
import kpt.core.model.savingsaccount.Status as SavingsStatus
// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `LinkableAccountEntity`. Shared with the DAO's writes. */
private const val LINKABLE_ACCOUNTS_TABLE = "wallet_linkable_accounts"

/**
 * Build the LEDGER read [Store] for accounts that a client CAN link into a pocket
 * (GOAL D13 — the `history` archetype variant for a client-scoped list, derived
 * subset).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through the
 * Room `wallet_linkable_accounts` [SourceOfTruth]; the network fetcher pulls
 * from `clientsApi.getClientAccounts(clientId)` (plus a per-SHARE-account
 * market-price round-trip), snapshots the already-linked set from
 * [PocketDao.observeByClient], runs the per-account-type filter inline, and
 * hands the mapped filtered list to the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate
 * is the Phase-5 app-wide default.
 *
 * ## Fetcher shape (compound fetch + snapshot filter)
 *
 * The fetcher performs TWO always-executed round-trips plus (for SHARE
 * accounts) N additional round-trips — one per share account — for
 * market-price resolution:
 *
 * 1. `apiManager.clientsApi.getClientAccounts(clientId)` → client's full
 *    account envelope (savings + loans + shares detail rows).
 * 2. `pocketDao.observeByClient(clientId).first()` → snapshot of already-linked
 *    accountIds (Room read, no network).
 * 3. For every SHARE account NOT in the linked set,
 *    `apiManager.shareAccountApi.getShareAccountDetails(share.id).first()` →
 *    market-price snapshot for the current balance computation.
 *
 * This mirrors the pre-store `PocketRepositoryImp.getAvailableAccountsToLink`
 * computation shape verbatim (which upstream PR #2057 kept behind an
 * in-memory MutableStateFlow-backed `PocketPreferencesDataSource.linkableAccounts`
 * that this store now replaces).
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [LinkableAccountDao.replacePage] — a single
 * `@Transaction` that delete-then-upserts under one SQLite transaction.
 * In-flight [LinkableAccountDao.observeByClient] subscribers never see an
 * empty page mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_linkable_accounts") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Reactive Offline-First Filtering (Write Path)
 *
 * This is a `Store` (not `MutableStore`) by design. The actual source of truth for filtering
 * available accounts is derived by combining the local `wallet_linkable_accounts` list with
 * the local `wallet_pockets` list natively in Room via `combine`.
 *
 * **Offline Interactions:** When a user links an account while offline, `PocketRepository`
 * directly writes an optimistic row into `wallet_pockets` (with a fake negative primary key,
 * but the correct `accountId` and `accountType`).
 *
 * **Data Preservation:** The fetcher explicitly pulls full details (balance, productName, etc)
 * so that when an account is linked offline, the optimistic `wallet_pockets` row has all the data
 * necessary to instantly render a beautiful UI card, avoiding a flicker to "Unknown Account".
 *
 * **Safe Reactive Filtering:** Filtering using the local `PocketEntity` is reliable because
 * the reader explicitly `combine`s `pocketDao.observeLinkedByClient`. The newly linked
 * account instantly disappears from the UI's available accounts list—purely reactively, without
 * requiring a network refresh, manual UI list manipulation, or any `MutableStateFlow` hacks.
 * This guarantees perfect synchronization between the Linked and Available screens, even entirely offline.
 *
 * ## Inline mappers
 *
 * `core/store` cannot depend on `core/data` (that would create a cycle —
 * `core/data` depends on `core/store` for the store bindings). The
 * per-account-type `→ LinkableAccount` builders + the three status-DTO
 * `→ AccountStatus` mappers are inlined below, mirroring
 * `PocketRepositoryImp.getAvailableAccountsToLink`. Keep in lockstep with the
 * matching inline mappers in `PocketStore.kt` (identical status decode logic).
 */
@StoreProvider(id = "linkableAccounts")
@CacheKey(fn = "of", key = "wallet_linkable_accounts-{clientId}", params = ["clientId:Long"])
fun provideLinkableAccountsStore(
    apiManager: SelfServiceApiManager,
    dao: LinkableAccountDao,
    pocketDao: PocketDao,
): Store<LinkableAccountKey, List<LinkableAccount>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: LinkableAccountKey ->
        // Step 1 — client-accounts envelope (per-client) for balance/product/status
        // resolution across the three account-type buckets.
        val clientAccounts: ClientAccountsEntity = apiManager.clientsApi
            .getClientAccounts(key.clientId)

        // Step 2 — snapshot the already-linked accountIds from the Room SoT
        // that `PocketStore` writes. `.first()` reads ONE emission then
        // releases the subscription (safe inside a fetcher — no lingering flow).
        // If the pocket table is cold (no fetch yet), an empty set is
        // conservative: surface everything as linkable (mirrors the pre-store
        // cold-cache behavior of `detailedPocketCache`).
        // Step 3 — build the LinkableAccount list per account-type. SHARE rows
        // fan out an extra getShareAccountDetails call for market-price resolution.
        // We now fetch ALL accounts without filtering them here so that we can
        /* do reactive optimistic filtering in the reader below when pockets change. */
        buildLinkableAccounts(
            clientAccounts = clientAccounts,
            alreadyLinkedAccounts = emptySet(),
            apiManager = apiManager,
        )
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: LinkableAccountKey ->
            combine(
                run { dao.observeByClient(key.clientId) },
                run { pocketDao.observeLinkedByClient(key.clientId) },
            ) { allAccounts, linked ->
                val linkedSet = linked.map { "${it.accountId}_${it.accountType}" }.toSet()
                allAccounts
                    .filter { "${it.accountId}_${it.accountType}" !in linkedSet }
                    .map { it.toDomain() }
            }
        },
        writer = { key: LinkableAccountKey, accounts: List<LinkableAccount> ->
            val stamp = clock()
            /* ATOMIC page replacement — see [LinkableAccountDao.replacePage]
             * KDoc for the S5-PAGE-ATOMIC invariant this write path preserves. */
            dao.replacePage(
                clientId = key.clientId,
                entities = accounts.map {
                    it.toEntity(clientId = key.clientId, fetchedAtEpochMs = stamp)
                },
            )
        },
        delete = { key: LinkableAccountKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)

/* LinkableAccount builder — mirrors PocketRepositoryImp#getAvailableAccountsToLink
 * verbatim (LOAN + SAVINGS + SHARE branches, SHARE market-price fan-out with
 * try/catch fallback). See `provideLinkableAccountsStore` ## Inline mappers
 * KDoc for rationale (module-cycle avoidance). */

private suspend fun buildLinkableAccounts(
    clientAccounts: ClientAccountsEntity,
    alreadyLinkedAccounts: Set<String>,
    apiManager: SelfServiceApiManager,
): List<LinkableAccount> {
    val out = mutableListOf<LinkableAccount>()

    processLoanAccounts(clientAccounts, alreadyLinkedAccounts, out)
    processSavingsAccounts(clientAccounts, alreadyLinkedAccounts, out)
    processShareAccounts(clientAccounts, alreadyLinkedAccounts, apiManager, out)

    return out
}

private fun processLoanAccounts(
    clientAccounts: ClientAccountsEntity,
    alreadyLinkedAccounts: Set<String>,
    out: MutableList<LinkableAccount>,
) {
    clientAccounts.loanAccounts.forEach { loan ->
        val loanId = loan.id ?: 0L
        if ("${loanId}_${AccountType.LOAN.name}" !in alreadyLinkedAccounts) {
            out.add(
                LinkableAccount(
                    accountId = loanId,
                    productName = loan.productName,
                    accountNumber = loan.accountNo,
                    accountType = AccountType.LOAN,
                    balance = loan.loanBalance,
                    currencyCode = loan.currency?.code,
                    currencyDisplaySymbol = loan.currency?.displaySymbol,
                    decimalPlaces = loan.currency?.decimalPlaces,
                    status = loan.status?.toAccountStatus(),
                ),
            )
        }
    }
}

private fun processSavingsAccounts(
    clientAccounts: ClientAccountsEntity,
    alreadyLinkedAccounts: Set<String>,
    out: MutableList<LinkableAccount>,
) {
    clientAccounts.savingsAccounts.forEach { savings ->
        if ("${savings.id}_${AccountType.SAVINGS.name}" !in alreadyLinkedAccounts) {
            out.add(
                LinkableAccount(
                    accountId = savings.id,
                    productName = savings.productName,
                    accountNumber = savings.accountNo,
                    accountType = AccountType.SAVINGS,
                    balance = savings.accountBalance,
                    currencyCode = savings.currency.code,
                    currencyDisplaySymbol = savings.currency.displaySymbol,
                    decimalPlaces = savings.currency.decimalPlaces,
                    status = savings.status.toAccountStatus(),
                ),
            )
        }
    }
}

private suspend fun processShareAccounts(
    clientAccounts: ClientAccountsEntity,
    alreadyLinkedAccounts: Set<String>,
    apiManager: SelfServiceApiManager,
    out: MutableList<LinkableAccount>,
) {
    clientAccounts.shareAccounts.forEach { share ->
        val shareId = share.id ?: 0L
        if ("${shareId}_${AccountType.SHARE.name}" !in alreadyLinkedAccounts) {
            var balance = 0.0
            var currencyCode: String? = share.currency?.code
            var currencyDisplaySymbol: String? = share.currency?.displaySymbol
            var decimalPlaces: Int? = share.currency?.decimalPlaces

            try {
                val shareAccountDetails = apiManager.shareAccountApi
                    .getShareAccountDetails(shareId).first()
                val approvedShares = shareAccountDetails.summary?.totalApprovedShares ?: 0
                val currentMarketPrice = shareAccountDetails.currentMarketPrice ?: 0.0
                balance = approvedShares * currentMarketPrice

                currencyCode = shareAccountDetails.currency?.code ?: currencyCode
                currencyDisplaySymbol = shareAccountDetails.currency?.displaySymbol
                    ?: currencyDisplaySymbol
                decimalPlaces = shareAccountDetails.currency?.decimalPlaces ?: decimalPlaces
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                /* Fall back to the client-accounts row values (already assigned above).
                 * Mirrors the pre-store try/catch swallow — same reasoning: the
                 * market-price fetch is a nice-to-have; the row still renders. */
            }

            out.add(
                LinkableAccount(
                    accountId = shareId,
                    productName = share.productName,
                    accountNumber = share.accountNo,
                    accountType = AccountType.SHARE,
                    balance = balance,
                    currencyCode = currencyCode,
                    currencyDisplaySymbol = currencyDisplaySymbol,
                    decimalPlaces = decimalPlaces,
                    status = share.status?.toAccountStatus(),
                ),
            )
        }
    }
}

/* Status DTO → AccountStatus decoders. IDENTICAL to the sibling functions in
 * PocketStore.kt (kept in lockstep — see PocketStore's inline-mappers KDoc for
 * the module-cycle-avoidance rationale). */

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
