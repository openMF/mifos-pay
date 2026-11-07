/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.selectScreen

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.data.repository.ThirdPartyTransferRepository
import kpt.core.model.network.entity.templates.account.AccountOption
import kpt.core.ui.fork.utils.BaseViewModel
/** ViewModel for choosing a destination account; loads the destination options and filters them by the entered text. */
class SelectScreenViewModel(
    private val repository: ThirdPartyTransferRepository,
) : BaseViewModel<SelectScreenState, SelectScreenEvent, SelectScreenAction>(
    initialState = SelectScreenState(),
) {

    init {
        viewModelScope.launch {
            getToAccounts()
        }
    }

    override fun handleAction(action: SelectScreenAction) {
        when (action) {
            is SelectScreenAction.AccountNumberChanged -> {
                val filteredAccounts = state.toAccountOptions?.filter { account ->
                    account.accountNo?.contains(action.accountNumber) == true ||
                        account.clientName?.contains(action.accountNumber, true) == true
                }
                mutableStateFlow.update {
                    it.copy(
                        accountNumber = action.accountNumber,
                        filteredToAccounts = filteredAccounts,
                    )
                }
            }

            SelectScreenAction.NavigateBack -> {
                sendEvent(SelectScreenEvent.NavigateBack)
            }

            is SelectScreenAction.SelectAccount -> {
                mutableStateFlow.update {
                    it.copy(selectedAccount = action.account)
                }
            }

            SelectScreenAction.DeselectAccount -> {
                mutableStateFlow.update {
                    it.copy(selectedAccount = null)
                }
            }

            SelectScreenAction.OnProceedClicked -> {
                sendEvent(SelectScreenEvent.NavigateToTransferScreen)
            }
        }
    }

    private suspend fun getToAccounts() {
        try {
            val res = repository.getTransferTemplate()
            val toAccounts = res.toAccountOptions?.filter {
                it.accountType?.id == 2
            }
            if (toAccounts.isNullOrEmpty()) {
                mutableStateFlow.update {
                    it.copy(
                        state = SelectScreenState.State.NoAccounts,
                    )
                }
            } else {
                mutableStateFlow.update {
                    it.copy(
                        state = SelectScreenState.State.Success,
                        toAccountOptions = toAccounts,
                        filteredToAccounts = toAccounts,
                    )
                }
            }
        } catch (e: Exception) {
            mutableStateFlow.update {
                it.copy(
                    state = SelectScreenState.State.Error(e.message ?: ""),
                )
            }
        }
    }
}

/**
 * UI state of the select-account screen.
 * @property amount Amount to transfer, as text.
 * @property accountNumber Account number or name filter entered by the user.
 * @property selectedAccount Chosen destination account, or null.
 * @property state Loading state of the destination accounts.
 * @property toAccountOptions All destination account options, or null.
 * @property filteredToAccounts Destination options matching the filter, or null.
 */
@Serializable
data class SelectScreenState(
    val amount: String = "",
    val accountNumber: String = "",
    val selectedAccount: AccountOption? = null,
    val state: State = State.Loading,
    val toAccountOptions: List<AccountOption>? = emptyList(),
    val filteredToAccounts: List<AccountOption>? = emptyList(),
) {

    /** Whether an account is selected and the user can proceed. */
    val isProceedEnabled: Boolean
        get() = selectedAccount != null

    /** Loading state of the destination accounts. */
    sealed interface State {
        /** Accounts are loading. */
        data object Loading : State

        /** There are no destination accounts. */
        data object NoAccounts : State

        /** Accounts loaded. */
        data object Success : State

        /**
         * Loading failed.
         * @property message Error text.
         */
        data class Error(val message: String) : State
    }
}

/** One-shot events emitted by [SelectScreenViewModel]. */
sealed interface SelectScreenEvent {
    /** Navigate to the transfer screen. */
    data object NavigateToTransferScreen : SelectScreenEvent

    /** Navigate back. */
    data object NavigateBack : SelectScreenEvent
}

/** Actions handled by [SelectScreenViewModel]. */
sealed interface SelectScreenAction {
    /** Navigate back. */
    data object NavigateBack : SelectScreenAction

    /**
     * The account number filter changed.
     * @property accountNumber New filter text.
     */
    data class AccountNumberChanged(val accountNumber: String) : SelectScreenAction

    /**
     * A destination account was chosen.
     * @property account Chosen account, or null.
     */
    data class SelectAccount(val account: AccountOption?) : SelectScreenAction

    /** The chosen account was cleared. */
    data object DeselectAccount : SelectScreenAction

    /** The proceed button was clicked. */
    data object OnProceedClicked : SelectScreenAction
}
