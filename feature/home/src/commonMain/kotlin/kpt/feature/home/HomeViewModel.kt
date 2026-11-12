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
import kpt.core.data.fork.util.toForkScreenStateFlow
import kpt.core.data.repository.AccountRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.Account
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_account_error
import kpt.feature.home.generated.resources.feature_home_account_success
import kpt.feature.home.generated.resources.feature_home_failed_to_load_accounts
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
private const val TRANSACTION_LIMIT = 5

/** ViewModel for the home screen: client accounts, recent transactions, filters and navigation events. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val preferencesRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val accountRepository: AccountRepository,
) : BaseViewModel<HomeState, HomeEvent, HomeAction>(
    initialState = run {
        /** Signed-in client, read from preferences. */
        val client = requireNotNull(preferencesRepository.client.value)

        /** Id of the client's default account, if set. */
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

    /** Starts observing the client's active savings accounts. */
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

    /** Selects [account] and shows its cached transactions, loading them if not yet fetched. */
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

    /** The navigation event an action asks for, or null for an action that is not navigation. */
    private fun navigationEventFor(action: HomeAction): HomeEvent? = when (action) {
        is HomeAction.RequestClicked ->
            HomeEvent.NavigateToRequestScreen(state.client.mobileNo.ifEmpty { state.client.externalId })
        HomeAction.SendClicked -> HomeEvent.NavigateToSendScreen
        HomeAction.AutoPayClicked -> HomeEvent.NavigateToAutoPayScreen
        HomeAction.ClientDetailsClicked -> HomeEvent.NavigateToClientDetailScreen
        HomeAction.OnClickSeeAllTransactions -> HomeEvent.NavigateToTransactionScreen
        is HomeAction.TransactionClicked ->
            HomeEvent.NavigateToTransactionDetail(action.accountId, action.transactionId)
        HomeAction.OnNavigateBack -> HomeEvent.NavigateBack
        is HomeAction.AccountDetailsClicked -> HomeEvent.NavigateToAccountDetail(action.accountId)
        else -> null
    }

    /** The transaction-filter bottom sheet's actions. */
    private fun handleFilterAction(action: HomeAction) {
        when (action) {
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

            else -> Unit
        }
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.RequestClicked,
            HomeAction.SendClicked,
            HomeAction.AutoPayClicked,
            HomeAction.ClientDetailsClicked,
            HomeAction.OnClickSeeAllTransactions,
            is HomeAction.TransactionClicked,
            HomeAction.OnNavigateBack,
            is HomeAction.AccountDetailsClicked,
            -> navigationEventFor(action)?.let(::sendEvent)

            is HomeAction.OnDismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
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

            HomeAction.DismissBottomSheet, HomeAction.ShowBottomSheet -> {
                mutableStateFlow.update {
                    it.copy(showBottomSheet = action == HomeAction.ShowBottomSheet)
                }
            }

            HomeAction.ClearFilters,
            HomeAction.OnApplyFilterClick,
            is HomeAction.SetFilter,
            is HomeAction.OnFilterAccountSelected,
            is HomeAction.OnFilterTransactionTypeSelected,
            -> handleFilterAction(action)

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

/**
 * UI state of the home screen.
 * @property client Signed-in client.
 * @property defaultAccountId Id of the default account, or null.
 * @property transactionType Transaction type filter being edited.
 * @property currentTransactionType Applied transaction type filter.
 * @property selectedAccount Account selected in the filter being edited.
 * @property currentSelectedAccount Applied account filter.
 * @property isRefreshing Whether a pull-to-refresh is in progress.
 * @property dialogState Loading or error dialog to show, or null.
 * @property accounts Client's accounts.
 * @property accountsWithTransactions Transactions grouped by account.
 * @property viewState Loading, error, content or no-accounts state.
 * @property transactions Transactions currently shown, or null if not loaded.
 * @property showBottomSheet Whether the filter bottom sheet is visible.
 * @property transactionsLoading Whether transactions are loading.
 * @property currentSelectedTransactionType Applied transaction type selection.
 */
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

    /** Dialog shown over the home screen. */
    @Serializable
    sealed class DialogState {

        /** Blocking loading dialog. */
        @Serializable
        data object Loading : DialogState()

        /**
         * Error dialog.
         * @property message Error text to display.
         */
        @Serializable
        data class Error(val message: String) : DialogState()
    }
}

/** Display state of the home screen. */
sealed interface ViewState {
    /** Data is loading. */
    data object Loading : ViewState

    /**
     * Loading failed.
     * @property message Error text to display.
     */
    data class Error(val message: String) : ViewState

    /** Data loaded and ready to show. */
    data object Content : ViewState

    /** The client has no accounts. */
    data object NoAccounts : ViewState
}

/** One-off events emitted by [HomeViewModel]. */
sealed interface HomeEvent {
    /** Navigate back. */
    data object NavigateBack : HomeEvent

    /** Open the send screen. */
    data object NavigateToSendScreen : HomeEvent

    /** Open the auto-pay screen. */
    data object NavigateToAutoPayScreen : HomeEvent

    /** Open the pocket dashboard. */
    data object NavigateToPocketDashboard : HomeEvent

    /** Open the full transaction list. */
    data object NavigateToTransactionScreen : HomeEvent

    /** Open the client details screen. */
    data object NavigateToClientDetailScreen : HomeEvent

    /**
     * Open the request-money screen.
     * @property vpa Virtual payment address to request money to.
     */
    data class NavigateToRequestScreen(val vpa: String) : HomeEvent

    /**
     * Open a transaction's detail.
     * @property accountId Account owning the transaction.
     * @property transactionId Id of the transaction.
     */
    data class NavigateToTransactionDetail(val accountId: Long, val transactionId: Long) : HomeEvent

    /**
     * Open an account's detail.
     * @property accountId Id of the account.
     */
    data class NavigateToAccountDetail(val accountId: Long) : HomeEvent

    /**
     * Show a toast message.
     * @property message Localized string resource to display.
     */
    data class ShowToast(val message: StringResource) : HomeEvent
}

/** User actions handled by [HomeViewModel]. */
sealed interface HomeAction {
    /** User tapped request money. */
    data object RequestClicked : HomeAction

    /** User tapped send money. */
    data object SendClicked : HomeAction

    /** User tapped auto-pay. */
    data object AutoPayClicked : HomeAction

    /** User tapped the pocket dashboard. */
    data object PocketDashboardClicked : HomeAction

    /** User tapped client details. */
    data object ClientDetailsClicked : HomeAction

    /** User tapped to see all transactions. */
    data object OnClickSeeAllTransactions : HomeAction

    /** User dismissed the dialog. */
    data object OnDismissDialog : HomeAction

    /** User navigated back. */
    data object OnNavigateBack : HomeAction

    /** User tapped retry after an error. */
    data object OnRetryClicked : HomeAction

    /** User pulled to refresh. */
    data object OnPullToRefresh : HomeAction

    /**
     * Marks an account as the default.
     * @property accountId Id of the account.
     * @property accountNo Account number.
     */
    data class MarkAsDefault(val accountId: Long, val accountNo: String) : HomeAction

    /**
     * Opens an account's detail.
     * @property accountId Id of the account.
     */
    data class AccountDetailsClicked(val accountId: Long) : HomeAction

    /**
     * Opens a transaction's detail.
     * @property accountId Account owning the transaction.
     * @property transactionId Id of the transaction.
     */
    data class TransactionClicked(val accountId: Long, val transactionId: Long) : HomeAction

    /**
     * The selected account filter changed.
     * @property account Newly selected account.
     */
    data class OnSelectedAccountChanged(val account: Account) : HomeAction

    /** Show the filter bottom sheet. */
    data object ShowBottomSheet : HomeAction

    /** Dismiss the filter bottom sheet. */
    data object DismissBottomSheet : HomeAction

    /** User confirmed the selected filters. */
    data object OnApplyFilterClick : HomeAction

    /** User cleared all filters. */
    data object ClearFilters : HomeAction

    /**
     * Applies a transaction type filter.
     * @property filter Transaction type to filter by.
     */
    data class SetFilter(val filter: TransactionType) : HomeAction

    /**
     * The transaction type selected in the filter changed.
     * @property transactionType Newly selected type.
     */
    data class OnFilterTransactionTypeSelected(val transactionType: TransactionType) : HomeAction

    /**
     * The account selected in the filter changed.
     * @property account Newly selected account.
     */
    data class OnFilterAccountSelected(val account: Account) : HomeAction
}
