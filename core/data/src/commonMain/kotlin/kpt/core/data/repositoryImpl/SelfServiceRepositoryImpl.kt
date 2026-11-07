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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.withContext
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.common.DateHelper
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.data.fork.util.Constants
import kpt.core.data.fork.util.parseFineractError
import kpt.core.data.fork.util.toForkScreenStateFlow
import kpt.core.data.mapper.toAccount
import kpt.core.data.mapper.toModel
import kpt.core.data.mapper.toModelAccountType
import kpt.core.data.mapper.toTransactionList
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.model.account.Account
import kpt.core.model.account.AccountContent
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.client.Client
import kpt.core.model.network.entity.Page
import kpt.core.model.network.entity.authentication.AuthenticationPayload
import kpt.core.model.network.entity.user.User
import kpt.core.model.savingsaccount.Transaction
import kpt.core.network.SelfServiceApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.beneficiary.BeneficiaryKey
import kpt.core.store.wallet.history.TransactionKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

/** Default [SelfServiceRepository] backed by the self-service API. */
@OptIn(ExperimentalCoroutinesApi::class)
@RepositoryBinding(binds = SelfServiceRepository::class)
class SelfServiceRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    @FromQualifier("IO") private val dispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.History) private val historyStore: Store<TransactionKey, List<Transaction>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Beneficiary) private val beneficiaryStore: Store<BeneficiaryKey, List<Beneficiary>>?,
) : SelfServiceRepository {
    override suspend fun loginSelf(payload: AuthenticationPayload): User {
        return withContext(dispatcher) {
            apiManager.authenticationApi.authenticate(payload)
        }
    }

    override fun getSelfClientDetails(clientId: Long): Flow<ScreenState<Client>> {
        return apiManager.clientsApi
            .getClient(clientId)
            .map { it.toModel() }
            .asScreenStateFlow()
            .flowOn(dispatcher)
    }

    override suspend fun getSelfClientDetails(): Flow<ScreenState<Page<Client>>> {
        return apiManager.clientsApi.clients()
            .map { it.toModel() }
            .asScreenStateFlow(isEmpty = { it.pageItems.isEmpty() })
            .flowOn(dispatcher)
    }

    override fun getSelfAccountTransactions(
        accountId: Long,
    ): Flow<List<Transaction>> {
        return apiManager.savingAccountsListApi
            .getSavingsWithAssociations(accountId, Constants.TRANSACTIONS)
            .map { it.toTransactionList() }
            .flowOn(dispatcher)
    }

    override suspend fun getSelfAccountTransactionFromId(
        accountId: Long,
        transactionId: Long,
    ): Flow<Transaction> {
        val result = withContext(dispatcher) {
            apiManager.savingAccountsListApi.getSavingAccountTransaction(
                accountId,
                transactionId,
            )
        }

        return result.map { it.toModel() }
    }

    override fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>> {
        return apiManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { it.toAccount() }
            .asScreenStateFlow(errorBodyParser = parseFineractError, isEmpty = { it.isEmpty() })
            .flowOn(dispatcher)
    }

    // Network-only aggregation lifted into ScreenState via `asScreenStateFlow`
    // — kept for BC while callers migrate to the store-native combinator
    // [getAccountAndBeneficiaryListScreen] (Phase-5 Batch-4), the sanctioned
    // offline-first path.
    override fun getAccountAndBeneficiaryList(clientId: Long): Flow<ScreenState<AccountContent>> {
        val accountList = apiManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { it.toAccount() }
            .flowOn(dispatcher)

        val beneficiaryList = apiManager.beneficiaryApi
            .beneficiaryList()
            .flowOn(dispatcher)

        return accountList.zip(beneficiaryList) { accounts, beneficiaries ->
            AccountContent(accounts = accounts, beneficiaries = beneficiaries)
        }
            .asScreenStateFlow()
            .flowOn(dispatcher)
    }

    override fun getActiveAccountsWithTransactionsPerAccount(
        clientId: Long,
        limit: Int?,
    ): Flow<ScreenState<Map<Account, List<Transaction>>>> {
        val accounts = apiManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { entity -> entity.savingsAccounts.filter { it.status.active } }
            .map { it.toAccount() }
            .flowOn(dispatcher)

        return accounts.flatMapLatest { accountList ->
            val flows = accountList.map { account ->
                getTransactions(listOf(account.id), limit)
                    .map { transactions -> account to transactions }
            }

            combine(flows) { pairs ->
                pairs.toMap()
            }
        }.asScreenStateFlow(isEmpty = { it.isEmpty() })
    }

    override fun getActiveAccounts(
        clientId: Long,
    ): Flow<ScreenState<List<Account>>> {
        return apiManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { entity -> entity.savingsAccounts.filter { it.status.active } }
            .map { it.toAccount() }
            .flowOn(dispatcher)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
    }

    override fun getActiveAccountsWithAccountTransferTemplate(
        clientId: Long,
    ): Flow<ScreenState<List<Account>>> {
        val accountsFlow = apiManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { entity -> entity.savingsAccounts.filter { it.status.active } }
            .flowOn(dispatcher)

        val templateFlow = apiManager.accountTransfersApi
            .getAccountTransferTemplate()
            .flowOn(dispatcher)

        return accountsFlow.zip(templateFlow) { accounts, template ->
            accounts
                .toAccount()
                .map { account ->
                    val templateAccount = template.fromAccountOptions?.firstOrNull {
                        it.accountNo == account.number
                    }
                    if (templateAccount == null) {
                        account
                    } else {
                        account.copy(
                            clientName = templateAccount.clientName ?: "",
                            accountType = templateAccount.accountType?.toModelAccountType(),
                            officeName = templateAccount.officeName,
                            officeId = templateAccount.officeId,
                        )
                    }
                }
        }.asScreenStateFlow(isEmpty = { it.isEmpty() })
    }

    override fun getTransactions(accountId: List<Long>, limit: Int?): Flow<List<Transaction>> {
        return accountId.asFlow().flatMapMerge { clientId ->
            getSelfAccountTransactions(clientId)
        }.scan(emptyList()) { acc, transactions ->
            (acc + transactions).sortedByDescending { transaction ->
                DateHelper.parseDateToMillis(transaction.date) ?: Long.MIN_VALUE
            }.let { sortedList ->
                limit?.let { sortedList.take(it) } ?: sortedList
            }
        }
    }

    override fun getTransactions(accountId: Long, limit: Int?): Flow<ScreenState<List<Transaction>>> {
        return apiManager.savingAccountsListApi
            .getSavingsWithAssociations(accountId, Constants.TRANSACTIONS)
            .map {
                if (limit != null) {
                    it.toTransactionList().take(limit)
                } else {
                    it.toTransactionList()
                }
            }
            .flowOn(dispatcher)
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
    }

    // Phase-4 Batch-A LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. The impl below opens ONE
    // ScreenDataStream per caller (each call gets its own refresh trigger, per
    // asScreenStream's documented concurrent-subscriber behavior) and forwards
    // the .state Flow through an optional client-side `take(limit)` map.
    //
    // Requires the three store-adapter dependencies (historyStore + NetworkMonitor
    // + FetchedAtRepository). If any is null (test wiring), we IllegalState —
    // production DI in RepositoryModule wires all three unconditionally.
    override fun getTransactionsStream(
        accountId: Long,
        limit: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Transaction>> {
        val store = checkNotNull(historyStore) {
            "getTransactionsStream requires the LEDGER Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.History and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getTransactionsStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getTransactionsStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        // TODO(limit): consumer applies take(limit) on Content
        return store.asScreenStream(
            key = TransactionKey(accountId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.History.of(accountId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.HISTORY,
        )
    }

    override fun getAccountsTransactions(
        clientId: Long,
    ): Flow<ScreenState<List<Transaction>>> {
        return apiManager.clientsApi
            .getAccounts(clientId, Constants.SAVINGS)
            .map { it.toAccount() }
            .map { list -> list.filter { it.status.active } }
            .map { list -> list.map { it.id } }
            .flatMapLatest { accountIds ->
                if (accountIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    getTransactions(accountId = accountIds, null)
                        .filter { transactions -> transactions.isNotEmpty() }
                }
            }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
    }

    override fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>> {
        return apiManager.beneficiaryApi.beneficiaryList()
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(dispatcher)
    }

    // Phase-5 Batch-1 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the store-adapter
    // dependencies (beneficiaryStore + NetworkMonitor + FetchedAtRepository).
    // If any is null (test wiring), we IllegalState — production DI in
    // RepositoryModule wires all three unconditionally.
    override fun getBeneficiaryListStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Beneficiary>> {
        val store = checkNotNull(beneficiaryStore) {
            "getBeneficiaryListStream requires the `beneficiary` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.Beneficiary and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getBeneficiaryListStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getBeneficiaryListStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = BeneficiaryKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.Beneficiary.of(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.BENEFICIARY,
        )
    }

    // Phase-5 Batch-4 combinator read — GOAL D13 (`combine` over two
    // upstream ScreenState streams).
    //
    // See the interface KDoc for the shape contract. The fold below is a
    // hand-written mirror of the template's `combineScreenStates` priority
    // ladder — the template helper cannot be used verbatim because it is
    // typed against `kpt.core.base.store.screen.ScreenState` (a distinct
    // type from the fork's `kpt.core.common.ScreenState`).
    //
    // Priority ladder — first match wins on each emission:
    //   NoNetwork > Loading > Unauthenticated > Error > Empty > Content
    //
    // Both sources must reach Content before the fold emits Content.
    override fun getAccountAndBeneficiaryListScreen(
        clientId: Long,
        scope: CoroutineScope,
    ): Flow<ScreenState<AccountContent>> {
        val accountsStream: Flow<ScreenState<List<Account>>> = getSelfAccounts(clientId)
        val beneficiariesStream: Flow<ScreenState<List<Beneficiary>>> =
            getBeneficiaryListStream(clientId, scope).state.toForkScreenStateFlow()
        return combine(accountsStream, beneficiariesStream) { accountsState, beneficiariesState ->
            foldAccountAndBeneficiary(accountsState, beneficiariesState)
        }.flowOn(dispatcher)
    }

    override suspend fun createBeneficiary(
        beneficiaryPayload: BeneficiaryPayload,
    ) {
        withContext(dispatcher) {
            apiManager.beneficiaryApi.createBeneficiary(beneficiaryPayload)
        }
    }

    override suspend fun updateBeneficiary(
        beneficiaryId: Long,
        payload: BeneficiaryUpdatePayload,
    ) {
        withContext(dispatcher) {
            apiManager.beneficiaryApi.updateBeneficiary(beneficiaryId, payload)
        }
    }

    override suspend fun deleteBeneficiary(beneficiaryId: Long) {
        withContext(dispatcher) {
            apiManager.beneficiaryApi.deleteBeneficiary(beneficiaryId)
        }
    }
}

// ---------------------------------------------------------------------------
// Private ScreenState combinator — folds two independent fork-side
// [`kpt.core.common.ScreenState`] streams into a single
// [`AccountContent`] payload state. The priority ladder mirrors the template's
// `combineScreenStates` helper in `core-base/store/screen/`
// (`NoNetwork > Loading > Unauthenticated > Error > Empty > Content`) but is
// written manually here because the template helper is typed against the
// TEMPLATE's `kpt.core.base.store.screen.ScreenState` (a distinct type from
// the fork's `kpt.core.common.ScreenState`); a direct call would be
// a type mismatch.
//
// The fold's `Content(AccountContent(...))` only fires when BOTH sources are
// `Content` — matches the template semantics ("both sources must reach
// Content before transform is called"). Everything else routes to the
// highest-priority error / loading branch across the two sources.
// ---------------------------------------------------------------------------

private fun foldAccountAndBeneficiary(
    accountsState: ScreenState<List<Account>>,
    beneficiariesState: ScreenState<List<Beneficiary>>,
): ScreenState<AccountContent> {
    // 1. NoNetwork — highest priority (either source shows a captive-portal-
    //    or offline-with-no-cache condition, propagate immediately).
    if (accountsState is ScreenState.NoNetwork) return accountsState
    if (beneficiariesState is ScreenState.NoNetwork) return beneficiariesState

    // 2. Loading — either source still fetching → hold the composite in Loading.
    if (accountsState is ScreenState.Loading || beneficiariesState is ScreenState.Loading) {
        return ScreenState.Loading
    }

    // 3. Unauthenticated — either source hit 401/403 → the composite is
    //    unauthenticated (screen should redirect to login).
    if (accountsState is ScreenState.Unauthenticated) return ScreenState.Unauthenticated
    if (beneficiariesState is ScreenState.Unauthenticated) return ScreenState.Unauthenticated

    // 4. Error — either source in Error → propagate the first one (accounts
    //    takes precedence as the primary payload).
    if (accountsState is ScreenState.Error) return accountsState
    if (beneficiariesState is ScreenState.Error) return beneficiariesState

    // 5. Empty — either source Empty. The composite's semantics: the account
    //    list drives visibility; an empty account list is a hard Empty, an
    //    empty beneficiary list is still a valid Content (account cards
    //    render, TPT beneficiary section stays empty).
    if (accountsState is ScreenState.Empty) return ScreenState.Empty
    // (Empty beneficiaries → treat as Content(emptyList) below.)

    // 6. Content — the happy path.
    val accounts: List<Account> = when (accountsState) {
        is ScreenState.Content -> accountsState.data
        else -> return ScreenState.Loading // defensive: types above should have covered every branch
    }
    val beneficiaries: List<Beneficiary> = when (beneficiariesState) {
        is ScreenState.Content -> beneficiariesState.data
        is ScreenState.Empty -> emptyList()
        else -> return ScreenState.Loading
    }
    return ScreenState.Content(AccountContent(accounts = accounts, beneficiaries = beneficiaries))
}
