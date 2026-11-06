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
import kpt.core.ui.utils.BaseViewModel
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

@Serializable
data class SelectScreenState(
    val amount: String = "",
    val accountNumber: String = "",
    val selectedAccount: AccountOption? = null,
    val state: State = State.Loading,
    val toAccountOptions: List<AccountOption>? = emptyList(),
    val filteredToAccounts: List<AccountOption>? = emptyList(),
) {

    val isProceedEnabled: Boolean
        get() = selectedAccount != null

    sealed interface State {
        data object Loading : State
        data object NoAccounts : State
        data object Success : State
        data class Error(val message: String) : State
    }
}

sealed interface SelectScreenEvent {
    data object NavigateToTransferScreen : SelectScreenEvent

    data object NavigateBack : SelectScreenEvent
}

sealed interface SelectScreenAction {
    data object NavigateBack : SelectScreenAction

    data class AccountNumberChanged(val accountNumber: String) : SelectScreenAction

    data class SelectAccount(val account: AccountOption?) : SelectScreenAction

    data object DeselectAccount : SelectScreenAction

    data object OnProceedClicked : SelectScreenAction
}
