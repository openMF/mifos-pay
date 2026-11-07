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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.common.Constants
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.mapper.toModel
import kpt.core.data.mapper.toSavingDetail
import kpt.core.data.repository.SavingsAccountRepository
import kpt.core.model.network.entity.Page
import kpt.core.model.savingsaccount.CreateNewSavingEntity
import kpt.core.model.savingsaccount.SavingAccountDetail
import kpt.core.model.savingsaccount.SavingAccountTemplate
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionsEntity
import kpt.core.model.savingsaccount.UpdateSavingAccountEntity
import kpt.core.network.SelfServiceApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.account.AccountDetailKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

/** Default [SavingsAccountRepository] backed by the savings accounts API. */
@RepositoryBinding(binds = SavingsAccountRepository::class)
class SavingsAccountRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.AccountDetail) private val accountDetailStore: Store<AccountDetailKey, SavingAccountDetail>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : SavingsAccountRepository {
    override suspend fun getSavingsAccounts(
        limit: Int,
    ): ScreenStateStream<Page<SavingsWithAssociationsEntity>> {
        return apiManager.savingAccountsListApi
            .getSavingsAccounts(limit)
            .asScreenStateFlow(isEmpty = { it.pageItems.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun getSavingsWithAssociations(
        accountId: Long,
        associationType: String,
    ): ScreenStateStream<SavingsWithAssociationsEntity> {
        return apiManager.savingAccountsListApi
            .getSavingsWithAssociations(accountId, associationType)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override fun getAccountDetail(accountId: Long): ScreenStateStream<SavingAccountDetail> {
        return apiManager.savingAccountsListApi
            .getSavingsWithAssociations(accountId, Constants.TRANSACTIONS)
            .map(SavingsWithAssociationsEntity::toSavingDetail)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-1 SINGLE-ROW-PER-KEY read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three store-adapter
    // dependencies (accountDetailStore + NetworkMonitor + FetchedAtRepository).
    // If any is null (test wiring), we IllegalState — production DI in
    // RepositoryModule wires all three unconditionally.
    override fun getAccountDetailStream(
        accountId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<SavingAccountDetail> {
        val store = checkNotNull(accountDetailStore) {
            "getAccountDetailStream requires the `accountDetail` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.AccountDetail and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getAccountDetailStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getAccountDetailStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = AccountDetailKey(accountId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.AccountDetail.of(accountId),
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.ACCOUNT_DETAIL,
        )
    }

    override suspend fun createSavingsAccount(
        savingAccount: CreateNewSavingEntity,
    ) {
        withContext(ioDispatcher) {
            apiManager.savingAccountsListApi.createSavingsAccount(savingAccount)
        }
    }

    override suspend fun updateSavingsAccount(
        accountId: Long,
        savingAccount: UpdateSavingAccountEntity,
    ) {
        withContext(ioDispatcher) {
            apiManager.savingAccountsListApi.updateSavingsAccount(accountId, savingAccount)
        }
    }

    override suspend fun unblockAccount(
        accountId: Long,
    ) {
        withContext(ioDispatcher) {
            apiManager.savingAccountsListApi.blockUnblockAccount(accountId, "unblock")
        }
    }

    override suspend fun blockAccount(accountId: Long) {
        withContext(ioDispatcher) {
            apiManager.savingAccountsListApi.blockUnblockAccount(accountId, "block")
        }
    }

    override suspend fun getSavingAccountTransaction(
        accountId: Long,
        transactionId: Long,
    ): ScreenStateStream<Transaction> {
        return apiManager.savingAccountsListApi
            .getSavingAccountTransaction(accountId, transactionId)
            .map(TransactionsEntity::toModel)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override suspend fun payViaMobile(accountId: Long): ScreenStateStream<Transaction> {
        return apiManager.savingAccountsListApi
            .payViaMobile(accountId)
            .map(TransactionsEntity::toModel)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    override fun getSavingAccountTemplate(clientId: Long): ScreenStateStream<SavingAccountTemplate> {
        return apiManager.savingAccountsListApi
            .getSavingAccountTemplate(clientId)
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }
}
