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

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.RecentPayeeRepository
import kpt.core.database.wallet.recentpayee.RecentPayeeDao
import kpt.core.database.wallet.recentpayee.toDomain
import kpt.core.database.wallet.recentpayee.toEntity
import kpt.core.model.account.RecentPayee
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.network.SelfServiceApiManager
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Implementation of [RecentPayeeRepository] that fetches recent payees from
 * transaction history and caches the DERIVED result in Room via the Store5
 * `recentPayee` offline store (Phase-5 Batch-4, GOAL D12/D13 hybrid).
 *
 * ## Derive walk (unchanged from pre-store)
 *
 * 1. Fetch savings account with transactions (`getSavingsWithAssociations`)
 * 2. Filter DEBIT (withdrawal) transactions that have a `transferId`
 * 3. Fetch `TransferDetail` for each unique `transferId` in parallel (N+1 fan-out)
 * 4. Group by recipient (`toClient.id`) and keep most recent
 * 5. Sort by transfer date descending
 *
 * ## Cache path (Phase-5 Batch-4)
 *
 * The derived list is now WRITTEN to `wallet_recent_payees` via
 * `notifyingWrite("wallet_recent_payees") { dao.replacePage(accountId, rows) }`.
 * Reads through [getRecentPayeesScreen] come from the DAO directly — cache
 * hits skip the N+1 walk entirely (the walk still fires in the background on
 * every screen entry so the cache stays fresh).
 *
 * The sole read surface is the store-backed [getRecentPayeesScreen] — callers
 * consume an offline-first `Flow<ScreenState<...>>` sourced from the Room
 * `wallet_recent_payees` cache.
 */
@OptIn(ExperimentalTime::class)
class RecentPayeeRepositoryImpl(
    private val selfServiceApiManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-4 store wiring. Nullable-default so unit tests that build
    // a RecentPayeeRepositoryImpl without the store harness don't have to
    // construct it. Production DI in `RepositoryModule` wires this
    // unconditionally.
    private val recentPayeeDao: RecentPayeeDao? = null,
) : RecentPayeeRepository {

    // Phase-5 Batch-4 store-backed read — GOAL D12/D13 (Room SoT + background
    // derive-and-persist via notifyingWrite).
    //
    // See the interface KDoc for the shape contract. The derive walk fires in
    // the background on every subscription (fresh derive on every screen
    // entry keeps the cache warm), but the Room reader emits IMMEDIATELY if
    // the table has cached rows — cache hits skip the N+1 wait on the UI.
    override fun getRecentPayeesScreen(
        accountId: Long,
        limit: Int,
        scope: CoroutineScope,
    ): Flow<ScreenState<List<RecentPayee>>> {
        val dao = checkNotNull(recentPayeeDao) {
            "getRecentPayeesScreen requires the `recentPayee` Store5 wiring (RecentPayeeDao). " +
                "Verify RepositoryModule bound AppStoreRegistry.RecentPayee and injected it here."
        }

        // Kick a background derive-and-persist on every subscription. This is
        // idempotent (`replacePage` is a full-table swap for this source
        // account under one `@Transaction`), so concurrent subscribers cannot
        // corrupt the cache. Errors during derive are logged and swallowed —
        // if we already have cached rows they stay visible; if we don't, the
        // reader shows Empty and the user can pull-to-refresh.
        scope.launch(ioDispatcher) {
            try {
                val derived = deriveRecentPayees(accountId)
                persistDerived(accountId, derived, dao)
            } catch (e: Exception) {
                Logger.w(e) { "RecentPayee: background derive failed for account $accountId" }
            }
        }

        return dao.observeBySourceAccount(accountId)
            .map { rows ->
                rows.asSequence()
                    .map { it.toDomain() }
                    .take(limit)
                    .toList()
            }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    // ------------------------------------------------------------------
    // Internals — the shared derive walk + the persist step, both consumed
    // by the `getRecentPayeesScreen(...)` path (derive + persist).
    // ------------------------------------------------------------------

    /**
     * The unchanged N+1 derive walk. Returns the full derived list (unlimited);
     * callers apply their own `.take(limit)`.
     */
    private suspend fun deriveRecentPayees(accountId: Long): List<RecentPayee> = withContext(ioDispatcher) {
        val savingsApi = selfServiceApiManager.savingAccountsListApi
        val accountTransfersApi = selfServiceApiManager.accountTransfersApi

        Logger.d { "RecentPayee: Fetching transactions for account $accountId" }

        // Collect the flow to get the savings account data
        var savingsAccount: kpt.core.model.savingsaccount.SavingsWithAssociationsEntity? = null
        savingsApi.getSavingsWithAssociations(accountId, ASSOCIATIONS_TRANSACTIONS)
            .collect { savingsAccount = it }

        if (savingsAccount == null) {
            return@withContext emptyList<RecentPayee>()
        }

        // Filter withdrawal transactions with transferId, newest first, capped.
        val withdrawalTransactions = savingsAccount!!.transactions
            .filter { it.transactionType.withdrawal && it.transfer != null }
            .sortedByDescending { it.submittedOnDate.toDateLong() }
            .take(MAX_TRANSACTIONS_TO_PROCESS)

        Logger.d { "RecentPayee: Found ${withdrawalTransactions.size} withdrawal transactions" }

        if (withdrawalTransactions.isEmpty()) {
            return@withContext emptyList<RecentPayee>()
        }

        // Get unique transfer IDs.
        val uniqueTransferIds = withdrawalTransactions
            .mapNotNull { it.transfer?.id }
            .distinct()

        Logger.d { "RecentPayee: Fetching details for ${uniqueTransferIds.size} transfers" }

        // Fetch transfer details in parallel.
        val transferDetails = coroutineScope {
            uniqueTransferIds.map { transferId ->
                async {
                    try {
                        var detail: TransferDetail? = null
                        accountTransfersApi.getAccountTransfer(transferId.toInt())
                            .collect { detail = it }
                        detail
                    } catch (e: Exception) {
                        Logger.w(e) { "RecentPayee: Failed to fetch transfer $transferId" }
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        Logger.d { "RecentPayee: Retrieved ${transferDetails.size} transfer details" }

        // Group by recipient and keep most recent.
        val recentPayees = transferDetails
            .groupBy { it.toClient.id }
            .mapNotNull { (_, transfers) ->
                val mostRecent = transfers.maxByOrNull { it.transferDate }
                    ?: return@mapNotNull null

                RecentPayee(
                    clientId = mostRecent.toClient.id,
                    clientName = mostRecent.toClient.displayName,
                    accountId = mostRecent.toAccount.id,
                    accountNo = mostRecent.toAccount.accountNo,
                    officeId = mostRecent.toClient.officeId,
                    officeName = mostRecent.toClient.officeName,
                    lastTransferDate = mostRecent.transferDate,
                    lastAmount = mostRecent.transferAmount,
                    currency = mostRecent.currency.code,
                )
            }
            .sortedByDescending { it.lastTransferDate }

        Logger.d { "RecentPayee: Returning ${recentPayees.size} derived recent payees" }

        recentPayees
    }

    /**
     * Persist a freshly-derived list into the `wallet_recent_payees` cache
     * under a single `@Transaction` (via `replacePage`). Wrapped in
     * `notifyingWrite` so wasmJs `daoFlow` collectors re-emit on write.
     *
     * The sort-key column is left at `0L` for now (the DAO's `ORDER BY` then
     * falls back to the lexical order of `lastTransferDate` — which is the
     * same behaviour the pre-cache derive relied on via
     * `.sortedByDescending { it.lastTransferDate }`).
     */
    private suspend fun persistDerived(
        accountId: Long,
        derived: List<RecentPayee>,
        dao: RecentPayeeDao,
    ) = withContext(ioDispatcher) {
        val stamp = Clock.System.now().toEpochMilliseconds()
        val rows = derived.map {
            it.toEntity(
                sourceAccountId = accountId,
                sortKey = 0L,
                fetchedAtEpochMs = stamp,
            )
        }
        run {
            dao.replacePage(sourceAccountId = accountId, entities = rows)
        }
    }

    /**
     * Converts date list [year, month, day] to comparable Long.
     */
    private fun List<Int>.toDateLong(): Long {
        return if (size >= 3) {
            this[0] * 10000L + this[1] * 100L + this[2]
        } else {
            0L
        }
    }

    /** Limits and keys used when deriving recent payees from account transactions. */
    companion object {
        private const val ASSOCIATIONS_TRANSACTIONS = "transactions"
        private const val MAX_TRANSACTIONS_TO_PROCESS = 50
    }
}
