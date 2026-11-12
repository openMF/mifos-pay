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
import kpt.core.model.account.Account
import kpt.core.model.account.AccountTransferPayload
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.model.search.AccountResult
/** Repository for account transfers, transactions and account lookups. */
interface AccountRepository {
    // Phase-3 cutover — reads on ScreenState.
    /** Streams a single transaction of an account. */
    fun getTransaction(accountId: Long, transactionId: Long): Flow<ScreenState<Transaction>>

    /** Streams the details of the account transfer identified by [transferId]. */
    fun getAccountTransfer(transferId: Long): Flow<ScreenState<TransferDetail>>

    /**
     * transfer-detail Store5 vertical — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
     *
     * Store-backed, offline-first alternative to [getAccountTransfer] — reads
     * through the `AppStoreRegistry.TransferDetail` Store5 (offline-first via
     * `wallet_transfer_details` Room SoT, SWR revalidation once the TTL elapses).
     * Mirrors [getSelfAccountsStream].
     *
     * @param transferId the store's page key AND the API path parameter.
     * @param scope the caller's [CoroutineScope] (typically `viewModelScope`)
     *   — Store5 subscribes its internal refresh trigger to this scope.
     */
    fun getAccountTransferStream(
        transferId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<TransferDetail>

    /** Streams accounts matching the search [query]. */
    fun searchAccounts(query: String): Flow<ScreenState<List<AccountResult>>>

    /** Streams the self-service accounts of the given client. */
    fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>>

    /**
     * Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
     *
     * Store-backed alternative to [getSelfAccounts] — reads through the
     * `AppStoreRegistry.SelfAccounts` Store5 (offline-first via
     * `wallet_self_accounts` Room SoT, SWR revalidation once the TTL elapses).
     *
     * @param clientId the store's page key AND the API path parameter.
     * @param scope the caller's [CoroutineScope] (typically `viewModelScope`)
     *   — Store5 subscribes its internal refresh trigger to this scope.
     */
    fun getSelfAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Account>>

    // Write migrated off legacy DataState — suspends and throws on failure.
    /** Submits an account-to-account transfer. */
    suspend fun makeTransfer(payload: AccountTransferPayload)
}
