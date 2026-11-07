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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.data.mapper.toAccount
import kpt.core.data.mapper.toModel
import kpt.core.data.repository.AccountRepository
import kpt.core.data.util.Constants
import kpt.core.database.wallet.transaction.TransactionDao
import kpt.core.database.wallet.transaction.toDomain
import kpt.core.model.account.Account
import kpt.core.model.account.AccountTransferPayload
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.model.search.AccountResult
import kpt.core.network.FineractApiManager
import kpt.core.network.SelfServiceApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.selfaccounts.SelfAccountsKey
import kpt.core.store.wallet.transferdetail.TransferDetailKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

// TODO use self api for account operations later
/** Default [AccountRepository] backed by the network data sources. */
class AccountRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-3 LEDGER wiring — injected by RepositoryModule so the
    // store-backed getSelfAccountsScreen(...) can consume Store5 via
    // asScreenStream(...). Nullable-default so existing unit tests without the
    // store harness continue to compile; getSelfAccounts(...) — the legacy
    // asScreenStateFlow path — is unaffected.
    private val selfAccountsStore: Store<SelfAccountsKey, List<Account>>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
    // transfer-detail Store5 vertical (GOAL D13) — makes the transaction-detail
    // drill-down offline-first. Nullable-default so existing unit tests without the
    // store harness continue to compile; the legacy getAccountTransfer(...) —
    // asScreenStateFlow shim over the raw Ktorfit flow — is unaffected.
    private val transferDetailStore: Store<TransferDetailKey, TransferDetail>? = null,
    // Existing wallet_transactions LEDGER DAO (populated by provideHistoryStore) —
    // injected so getTransaction(...) can read the cached row offline-first before
    // hitting the network. Nullable-default for the same test-harness reason.
    private val transactionDao: TransactionDao? = null,
) : AccountRepository {

    // Offline-first single-transaction read — prefers the cached `wallet_transactions`
    // LEDGER row (populated by provideHistoryStore) and falls back to the network
    // `getTransaction` endpoint on a cold cache. The Room flow re-emits once the
    // history store later populates the row, so the screen upgrades from network to
    // cache transparently. If the DAO is not wired (test harness), the legacy
    // network-only path is used unchanged.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransaction(
        accountId: Long,
        transactionId: Long,
    ): Flow<ScreenState<Transaction>> {
        val dao = transactionDao ?: return selfManager.accountTransfersApi
            .getTransaction(accountId, transactionId)
            .map { it.toModel() }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)

        return dao.observeByTransactionId(transactionId)
            .flatMapLatest { cached ->
                if (cached != null) {
                    flowOf(cached.toDomain()).asScreenStateFlow()
                } else {
                    selfManager.accountTransfersApi
                        .getTransaction(accountId, transactionId)
                        .map { it.toModel() }
                        .asScreenStateFlow()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun getAccountTransfer(transferId: Long): Flow<ScreenState<TransferDetail>> {
        return selfManager.accountTransfersApi
            .getAccountTransfer(transferId.toInt())
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override fun searchAccounts(query: String): Flow<ScreenState<List<AccountResult>>> {
        return selfManager.accountTransfersApi
            .searchAccounts(query, "savings")
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>> {
        return selfManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { it.toAccount() }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three
    // store-adapter dependencies (selfAccountsStore + NetworkMonitor +
    // FetchedAtRepository). If any is null (test wiring), we IllegalState —
    // production DI in RepositoryModule wires all three unconditionally.
    override fun getSelfAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Account>> {
        val store = checkNotNull(selfAccountsStore) {
            "getSelfAccountsStream requires the `selfAccounts` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.SelfAccounts and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getSelfAccountsStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getSelfAccountsStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = SelfAccountsKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.SelfAccounts.of(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.SELF_ACCOUNTS,
        )
    }

    // transfer-detail Store5 vertical — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three
    // store-adapter dependencies (transferDetailStore + NetworkMonitor +
    // FetchedAtRepository). If any is null (test wiring), we IllegalState —
    // production DI in RepositoryModule wires all three unconditionally. Mirrors
    // getSelfAccountsStream + SavingsAccountRepositoryImpl.getAccountDetailStream.
    override fun getAccountTransferStream(
        transferId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<TransferDetail> {
        val store = checkNotNull(transferDetailStore) {
            "getAccountTransferStream requires the `transferDetail` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.TransferDetail and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getAccountTransferStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getAccountTransferStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = TransferDetailKey(transferId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.TransferDetail.of(transferId),
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.TRANSFER_DETAIL,
        )
    }

    override suspend fun makeTransfer(payload: AccountTransferPayload) {
        withContext(ioDispatcher) {
            selfManager.accountTransfersApi.makeTransfer(payload)
        }
    }
}
