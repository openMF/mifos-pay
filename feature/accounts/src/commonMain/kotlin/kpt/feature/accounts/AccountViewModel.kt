/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kpt.core.common.ScreenState
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.Account
import kpt.core.model.account.DefaultAccount
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.accounts.AccountEvent.OnAddEditSavingsAccount
import kpt.feature.accounts.generated.resources.Res
import kpt.feature.accounts.generated.resources.feature_accounts_default_account_updated
import kpt.feature.accounts.savingsaccount.SavingsAddEditType
import org.jetbrains.compose.resources.StringResource
/**
 * Loads the user's savings accounts and handles default-account and navigation actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel(
    private val userRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        /** Client id read from user preferences when the view model is created. */
        val clientId = userRepository.clientId.value

        /** Default account id read from user preferences when the view model is created. */
        val defaultAccount = userRepository.defaultAccountId.value

        AccountState(
            clientId = clientId,
            defaultAccountId = defaultAccount,
        )
    },
) {
    // Phase-5 Batch-4: cut over from the transitional
    // `getAccountAndBeneficiaryList` (Flow<DataState<T>>) to the store-native
    // `getAccountAndBeneficiaryListScreen` (Flow<ScreenState<T>>), which folds
    // the network-account stream + store-backed beneficiary stream through a
    // manual `combine {}` fold over the FORK's ScreenState (priority ladder:
    // NoNetwork > Loading > Unauthenticated > Error > Empty > Content). See
    // `SelfServiceRepositoryImpl.foldAccountAndBeneficiary` for the fold's
    // implementation; the template's `combineScreenStates` helper cannot be
    // used verbatim because it is typed against the template's ScreenState.
    /** The signed-in client's accounts, as a screen state. */
    val accountState = mutableStateFlow
        .flatMapLatest { currentState ->
            val clientId = currentState.clientId
            if (clientId != null) {
                repository.getAccountAndBeneficiaryListScreen(clientId, viewModelScope)
            } else {
                flowOf(
                    ScreenState.Error(IllegalStateException("Client ID not available"))
                        as ScreenState<kpt.core.model.account.AccountContent>,
                )
            }
        }
        .mapLatest { screenState ->
            when (screenState) {
                is ScreenState.Loading -> AccountState.ViewState.Loading

                is ScreenState.Empty ->
                    // Empty account-list branch → render an empty Content
                    // (screen still shows the FAB + empty-state copy; no
                    // dedicated Empty ViewState variant exists here).
                    AccountState.ViewState.Content(accounts = emptyList())

                is ScreenState.NoNetwork ->
                    AccountState.ViewState.Error("No network connection")

                is ScreenState.Unauthenticated ->
                    AccountState.ViewState.Error("Session expired — please sign in")

                is ScreenState.Error ->
                    AccountState.ViewState.Error(screenState.error.message.toString())

                is ScreenState.Content -> {
                    val sortedAccounts = screenState.data.accounts.sortedWith(
                        compareByDescending<Account> { account -> account.status.active }
                            .thenBy { account -> account.number },
                    )

                    AccountState.ViewState.Content(accounts = sortedAccounts)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountState.ViewState.Loading,
        )

    override fun handleAction(action: AccountAction) {
        when (action) {
            is AccountAction.CreateSavingsAccount -> {
                sendEvent(OnAddEditSavingsAccount(SavingsAddEditType.AddItem))
            }

            is AccountAction.EditSavingsAccount -> {
                sendEvent(OnAddEditSavingsAccount(SavingsAddEditType.EditItem(action.accountId)))
            }

            is AccountAction.ViewAccountDetails -> {
                sendEvent(AccountEvent.OnNavigateToAccountDetail(action.accountId))
            }

            is AccountAction.SetDefaultAccount -> handleSetDefaultAccount(action)

            is AccountAction.ApplyForLoan -> {
                state.clientId?.let { clientId ->
                    sendEvent(AccountEvent.OnApplyForLoan(clientId))
                }
            }
        }
    }

    private fun handleSetDefaultAccount(action: AccountAction.SetDefaultAccount) {
        launchIO {
            userRepository.updateDefaultAccount(
                DefaultAccount(
                    accountId = action.accountId,
                    accountNo = action.accountNo,
                ),
            )
        }

        mutableStateFlow.update { it.copy(defaultAccountId = action.accountId) }
        sendEvent(AccountEvent.ShowToast(Res.string.feature_accounts_default_account_updated))
    }
}

/**
 * UI state of the accounts screen.
 *
 * @property clientId Id of the signed-in client, or null.
 * @property defaultAccountId Id of the user's default account, or null.
 */
data class AccountState(
    val clientId: Long? = null,
    val defaultAccountId: Long? = null,
) {
    /** Content states of the accounts screen. */
    sealed interface ViewState {
        /** Whether a floating action button is shown. */
        val hasFab: Boolean

        /** Whether pull-to-refresh is enabled. */
        val isPullToRefreshEnabled: Boolean

        /** Accounts are loading. */
        data object Loading : ViewState {
            override val hasFab: Boolean get() = false
            override val isPullToRefreshEnabled: Boolean get() = false
        }

        /**
         * Loading failed.
         *
         * @property message Error text to display.
         */
        data class Error(val message: String) : ViewState {
            override val hasFab: Boolean get() = false
            override val isPullToRefreshEnabled: Boolean get() = false
        }

        /**
         * Accounts loaded.
         *
         * @property accounts The user's savings accounts.
         */
        data class Content(
            val accounts: List<Account>,
        ) : ViewState {
            override val hasFab: Boolean get() = true
            override val isPullToRefreshEnabled: Boolean get() = true
        }
    }
}

/** One-off events emitted by the accounts screen. */
sealed interface AccountEvent {
    /**
     * Open the add or edit savings account screen.
     *
     * @property type Whether to add a new account or edit an existing one.
     */
    data class OnAddEditSavingsAccount(val type: SavingsAddEditType) : AccountEvent

    /**
     * Open the savings account detail screen.
     *
     * @property accountId Id of the account to show.
     */
    data class OnNavigateToAccountDetail(val accountId: Long) : AccountEvent

    /**
     * Open the loan application flow.
     *
     * @property clientId Id of the applying client.
     */
    data class OnApplyForLoan(val clientId: Long) : AccountEvent

    /**
     * Show a transient message.
     *
     * @property message Message string resource.
     */
    data class ShowToast(val message: StringResource) : AccountEvent
}

/** Actions the accounts screen can send to its view model. */
sealed interface AccountAction {
    /** Start creating a savings account. */
    data object CreateSavingsAccount : AccountAction

    /**
     * Start editing a savings account.
     *
     * @property accountId Id of the account.
     */
    data class EditSavingsAccount(val accountId: Long) : AccountAction

    /**
     * Open the details of a savings account.
     *
     * @property accountId Id of the account.
     */
    data class ViewAccountDetails(val accountId: Long) : AccountAction

    /**
     * Make an account the user's default.
     *
     * @property accountId Id of the account.
     * @property accountNo Account number of the account.
     */
    data class SetDefaultAccount(val accountId: Long, val accountNo: String) : AccountAction

    /** Start a loan application. */
    data object ApplyForLoan : AccountAction
}
