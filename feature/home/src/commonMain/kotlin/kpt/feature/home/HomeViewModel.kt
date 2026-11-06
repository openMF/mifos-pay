/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.ScreenState
import kpt.core.data.repository.AccountRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.data.util.toForkScreenStateFlow
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.Account
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_account_error
import kpt.feature.home.generated.resources.feature_home_account_success
import kpt.feature.home.generated.resources.feature_home_failed_to_load_accounts
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
private const val TRANSACTION_LIMIT = 5

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val preferencesRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val accountRepository: AccountRepository,
) : BaseViewModel<HomeState, HomeEvent, HomeAction>(
    initialState = run {
        val client = requireNotNull(preferencesRepository.client.value)
        val defaultAccount = preferencesRepository.defaultAccountId.value

        HomeState(
            client = client,
            defaultAccountId = defaultAccount,
        )
    },
) {

    // Template idiom (core-base/store): the one-shot "mark account as default" write
    // goes through a SubmitHandler instead of a hand-folded DataState result. The
    // handler owns the Submitting/Submitted/Failed lifecycle; we observe it below to
    // drive this screen's existing state update + success/error toast. The result
    // carried by Submitted is the account id that was made default.
    private val submitDefaultAccount = viewModelScope.submitHandler<Long>()

    init {
        submitDefaultAccount.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> Unit

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            it.copy(defaultAccountId = submitState.result)
                        }
                        sendEvent(HomeEvent.ShowToast(Res.string.feature_home_account_success))
                        submitDefaultAccount.reset()
                    }

                    is SubmitState.Failed -> {
                        sendEvent(HomeEvent.ShowToast(Res.string.feature_home_account_error))
                        submitDefaultAccount.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun getAccounts() {
        launchIO {
            // Offline-first: read the self-accounts through the Store5 SelfAccounts
            // store (Room SoT + CACHE_FIRST_SWR) so cached accounts render when the
            // device is offline. The store returns ALL savings accounts, so we
            // preserve the previous `getActiveAccounts` semantics by filtering
            // `status.active` client-side in the Content branch.
            accountRepository.getSelfAccountsStream(state.client.id, viewModelScope)
                .state.toForkScreenStateFlow()
                .collect { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {
                            mutableStateFlow.update { it.copy(viewState = ViewState.Loading) }
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isRefreshing = false,
                                    accounts = emptyList(),
                                    accountsWithTransactions = emptyMap(),
                                    viewState = ViewState.NoAccounts,
                                )
                            }
                        }

                        is ScreenState.Content -> {
                            val activeAccounts = screenState.data.filter { it.status.active }
                            if (activeAccounts.isEmpty()) {
                                mutableStateFlow.update {
                                    it.copy(
                                        isRefreshing = false,
                                        accounts = emptyList(),
                                        accountsWithTransactions = emptyMap(),
                                        viewState = ViewState.NoAccounts,
                                    )
                                }
                            } else {
                                val selected = activeAccounts.firstOrNull()

                                // Save account external IDs map
                                val accountExternalIds = activeAccounts
                                    .filter { !it.externalId.isNullOrBlank() }
                                    .associate { it.id to it.externalId!! }
                                preferencesRepository.updateAccountExternalIds(accountExternalIds)

                                if (selected != null) {
                                    mutableStateFlow.update {
                                        it.copy(
                                            isRefreshing = false,
                                            accounts = activeAccounts,
                                            accountsWithTransactions = emptyMap(),
                                            viewState = ViewState.Content,
                                            selectedAccount = selected,
                                            currentSelectedAccount = selected,
                                        )
                                    }
                                }

                                if (state.defaultAccountId == null && selected != null) {
                                    sendAction(HomeAction.MarkAsDefault(selected.id, selected.number))
                                }
                                if (selected != null) {
                                    getAccountBasedOnId(selected)
                                }
                            }
                        }

                        is ScreenState.Error -> {
                            val errorMessage = screenState.error.message
                                ?.takeIf { it != "null" && it.isNotBlank() }
                                ?: getString(Res.string.feature_home_failed_to_load_accounts)
                            mutableStateFlow.update {
                                it.copy(
                                    isRefreshing = false,
                                    viewState = ViewState.Error(errorMessage),
                                )
                            }
                        }

                        is ScreenState.NoNetwork -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isRefreshing = false,
                                    viewState = ViewState.Error(
                                        "No network. Please check your connection.",
                                    ),
                                )
                            }
                        }

                        is ScreenState.Unauthenticated -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isRefreshing = false,
                                    viewState = ViewState.Error(
                                        "Session expired. Please log in again.",
                                    ),
                                )
                            }
                        }
                    }
                }
        }
    }

    private var loadTransactionsJob: Job? = null

    fun getAccountBasedOnId(account: Account) {
        if (state.accountsWithTransactions.containsKey(account)) {
            mutableStateFlow.update {
                it.copy(
                    transactions = state.accountsWithTransactions[account],
                    selectedAccount = account,
                    currentSelectedAccount = account,
                )
            }
            applyFilter()
        } else {
            // cancel the previous job if it's still active
            loadTransactionsJob?.cancel()

            // launch a new job
            loadTransactionsJob = launchIO {
                // Offline-first: read recent transactions through the Store5 ledger
                // store (Room SoT + CACHE_FIRST_SWR) so cached history renders offline.
                repository.getTransactionsStream(
                    account.id,
                    TRANSACTION_LIMIT,
                    viewModelScope,
                ).state.toForkScreenStateFlow().collect { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {
                            mutableStateFlow.update {
                                it.copy(
                                    transactions = emptyList(),
                                    transactionsLoading = true,
                                )
                            }
                        }

                        is ScreenState.Empty -> {
                            val emptyList = emptyList<Transaction>()
                            val newMap = state.accountsWithTransactions.toMutableMap()
                            newMap[account] = emptyList
                            mutableStateFlow.update {
                                it.copy(
                                    transactionsLoading = false,
                                    transactions = emptyList,
                                    selectedAccount = account,
                                    currentSelectedAccount = account,
                                    accountsWithTransactions = newMap,
                                )
                            }
                            applyFilter()
                        }

                        is ScreenState.Content -> {
                            // The new getTransactionsStream ignores its `limit`
                            // param (returns the full LEDGER list), so apply the
                            // TRANSACTION_LIMIT here to preserve the old behavior.
                            val limitedTransactions = screenState.data.take(TRANSACTION_LIMIT)
                            val newMap = state.accountsWithTransactions.toMutableMap()
                            newMap[account] = limitedTransactions
                            mutableStateFlow.update {
                                it.copy(
                                    transactionsLoading = false,
                                    transactions = limitedTransactions,
                                    selectedAccount = account,
                                    currentSelectedAccount = account,
                                    accountsWithTransactions = newMap,
                                )
                            }
                            applyFilter()
                        }

                        is ScreenState.Error,
                        is ScreenState.NoNetwork,
                        is ScreenState.Unauthenticated,
                        -> {
                            mutableStateFlow.update {
                                it.copy(
                                    transactionsLoading = false,
                                    transactions = emptyList(),
                                    selectedAccount = account,
                                    currentSelectedAccount = account,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.RequestClicked -> {
                val vpa = state.client.mobileNo.ifEmpty { state.client.externalId }
                sendEvent(HomeEvent.NavigateToRequestScreen(vpa))
            }

            is HomeAction.SendClicked -> {
                sendEvent(HomeEvent.NavigateToSendScreen)
            }

            is HomeAction.AutoPayClicked -> {
                sendEvent(HomeEvent.NavigateToAutoPayScreen)
            }

            is HomeAction.ClientDetailsClicked -> {
                sendEvent(HomeEvent.NavigateToClientDetailScreen)
            }

            is HomeAction.OnClickSeeAllTransactions -> {
                sendEvent(HomeEvent.NavigateToTransactionScreen)
            }

            is HomeAction.TransactionClicked -> {
                sendEvent(
                    HomeEvent.NavigateToTransactionDetail(
                        action.accountId,
                        action.transactionId,
                    ),
                )
            }

            is HomeAction.OnDismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is HomeAction.OnNavigateBack -> {
                sendEvent(HomeEvent.NavigateBack)
            }

            is HomeAction.AccountDetailsClicked -> {
                sendEvent(HomeEvent.NavigateToAccountDetail(action.accountId))
            }

            is HomeAction.MarkAsDefault -> {
                // Submit through the handler — it drives Submitting/Submitted/Failed,
                // observed in `init`. The repository write returns Unit and throws on
                // failure; the handler maps that to Submitted/Failed. On success the
                // block returns the account id so the observer can update
                // `defaultAccountId` and fire the success toast.
                submitDefaultAccount.submit {
                    preferencesRepository.updateDefaultAccount(
                        DefaultAccount(
                            accountId = action.accountId,
                            accountNo = action.accountNo,
                        ),
                    )
                    action.accountId
                }
            }

            is HomeAction.OnRetryClicked -> {
                getAccounts()
            }

            is HomeAction.OnPullToRefresh -> {
                mutableStateFlow.update {
                    it.copy(isRefreshing = true)
                }
                getAccounts()
            }

            is HomeAction.OnSelectedAccountChanged -> {
                getAccountBasedOnId(action.account)
            }

            HomeAction.DismissBottomSheet -> {
                mutableStateFlow.update {
                    it.copy(showBottomSheet = false)
                }
            }

            HomeAction.ShowBottomSheet -> {
                mutableStateFlow.update {
                    it.copy(showBottomSheet = true)
                }
            }

            HomeAction.ClearFilters -> {
                mutableStateFlow.update {
                    it.copy(
                        transactionType = TransactionType.OTHER,
                        currentSelectedTransactionType = TransactionType.OTHER,
                        showBottomSheet = false,
                        transactions = it.accountsWithTransactions[it.selectedAccount],
                    )
                }
            }

            HomeAction.OnApplyFilterClick -> {
                mutableStateFlow.update {
                    it.copy(
                        showBottomSheet = false,
                    )
                }
                if (state.currentSelectedAccount != null) {
                    getAccountBasedOnId(state.currentSelectedAccount!!)
                }
            }

            is HomeAction.SetFilter -> {
                mutableStateFlow.update {
                    it.copy(currentSelectedTransactionType = action.filter)
                }
            }

            is HomeAction.OnFilterAccountSelected -> {
                mutableStateFlow.update {
                    it.copy(
                        currentSelectedAccount = action.account,
                    )
                }
            }

            is HomeAction.OnFilterTransactionTypeSelected -> {
                mutableStateFlow.update {
                    it.copy(
                        currentSelectedTransactionType = action.transactionType,
                    )
                }
            }

            is HomeAction.PocketDashboardClicked -> {
                sendEvent(HomeEvent.NavigateToPocketDashboard)
            }
        }
    }

    private fun applyFilter() {
        val filter = state.currentSelectedTransactionType
        val transactions = state.accountsWithTransactions[state.currentSelectedAccount]
        val filteredTransactions = transactions?.filter {
            if (filter == TransactionType.OTHER) {
                true
            } else {
                it.transactionType == filter
            }
        }

        mutableStateFlow.update {
            it.copy(
                transactions = filteredTransactions,
                transactionType = filter,
                selectedAccount = it.currentSelectedAccount,
            )
        }
    }
}

@Serializable
data class HomeState(
    val client: Client,
    val defaultAccountId: Long?,
    val transactionType: TransactionType = TransactionType.OTHER,
    val currentTransactionType: TransactionType = TransactionType.OTHER,
    val selectedAccount: Account? = null,
    val currentSelectedAccount: Account? = null,
    val isRefreshing: Boolean = false,
    val dialogState: DialogState? = null,
    val accounts: List<Account> = emptyList(),
    val accountsWithTransactions: Map<Account, List<Transaction>> = emptyMap(),
    val viewState: ViewState = ViewState.Loading,
    val transactions: List<Transaction>? = null,
    val showBottomSheet: Boolean = false,
    val transactionsLoading: Boolean = true,
    val currentSelectedTransactionType: TransactionType = TransactionType.OTHER,
) {

    @Serializable
    sealed class DialogState {

        @Serializable
        data object Loading : DialogState()

        @Serializable
        data class Error(val message: String) : DialogState()
    }
}

sealed interface ViewState {
    data object Loading : ViewState

    data class Error(val message: String) : ViewState

    data object Content : ViewState

    data object NoAccounts : ViewState
}

sealed interface HomeEvent {
    data object NavigateBack : HomeEvent
    data object NavigateToSendScreen : HomeEvent
    data object NavigateToAutoPayScreen : HomeEvent
    data object NavigateToPocketDashboard : HomeEvent
    data object NavigateToTransactionScreen : HomeEvent
    data object NavigateToClientDetailScreen : HomeEvent
    data class NavigateToRequestScreen(val vpa: String) : HomeEvent
    data class NavigateToTransactionDetail(val accountId: Long, val transactionId: Long) : HomeEvent
    data class NavigateToAccountDetail(val accountId: Long) : HomeEvent

    data class ShowToast(val message: StringResource) : HomeEvent
}

sealed interface HomeAction {
    data object RequestClicked : HomeAction
    data object SendClicked : HomeAction
    data object AutoPayClicked : HomeAction
    data object PocketDashboardClicked : HomeAction
    data object ClientDetailsClicked : HomeAction
    data object OnClickSeeAllTransactions : HomeAction
    data object OnDismissDialog : HomeAction
    data object OnNavigateBack : HomeAction
    data object OnRetryClicked : HomeAction
    data object OnPullToRefresh : HomeAction

    data class MarkAsDefault(val accountId: Long, val accountNo: String) : HomeAction
    data class AccountDetailsClicked(val accountId: Long) : HomeAction
    data class TransactionClicked(val accountId: Long, val transactionId: Long) : HomeAction

    data class OnSelectedAccountChanged(val account: Account) : HomeAction
    data object ShowBottomSheet : HomeAction
    data object DismissBottomSheet : HomeAction
    data object OnApplyFilterClick : HomeAction
    data object ClearFilters : HomeAction
    data class SetFilter(val filter: TransactionType) : HomeAction

    data class OnFilterTransactionTypeSelected(val transactionType: TransactionType) : HomeAction
    data class OnFilterAccountSelected(val account: Account) : HomeAction
}
