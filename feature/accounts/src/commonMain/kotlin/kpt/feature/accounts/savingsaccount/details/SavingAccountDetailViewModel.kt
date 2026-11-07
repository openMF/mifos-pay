/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.savingsaccount.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.data.fork.util.toForkScreenStateFlow
import kpt.core.data.repository.SavingsAccountRepository
import kpt.core.model.savingsaccount.SavingAccountDetail
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.accounts.savingsaccount.details.SADState.ViewState.Error
internal class SavingAccountDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: SavingsAccountRepository,
) : BaseViewModel<SADState, SADEvent, SADAction>(
    initialState = savedStateHandle.getSerialized(KEY) ?: SADState(
        accountId = requireNotNull(savedStateHandle[ACCOUNT_ID_KEY]),
        viewState = SADState.ViewState.Loading,
    ),
) {

    companion object {
        private const val KEY = "saving_account_detail"
        private const val ACCOUNT_ID_KEY = "accountId"
    }

    init {
        // Phase-5 Batch-1 SINGLE-ROW-PER-KEY read (GOAL D13) — switched from the
        // transitional `getAccountDetail(accountId)` (`asScreenStateFlow` shim
        // over the raw Ktorfit flow) to the store-native `getAccountDetailScreen(...)`
        // that consumes the `accountDetail` Store5 read (`createStore` + Room SoT
        // + CACHE_FIRST_SWR + single-row upsert). Same `ScreenStateStream<SavingAccountDetail>`
        // shape; consumer branches are unchanged. Requires `viewModelScope` for
        // the stream's internal reconnect + periodic + SWR side-fetch coroutines.
        repository.getAccountDetailStream(state.accountId, scope = viewModelScope)
            .state.toForkScreenStateFlow()
            .observeScreen { screenState ->
                mutableStateFlow.update { it.copy(viewState = screenState.toViewState()) }
            }
    }

    override fun handleAction(action: SADAction) {
        when (action) {
            is SADAction.NavigateBack -> {
                sendEvent(SADEvent.NavigateBack)
            }

            is SADAction.ViewTransaction -> {
                sendEvent(SADEvent.OnViewTransaction(action.clientId, action.accountId))
            }
        }
    }
}

/**
 * Fold the 6-branch [ScreenState] into the existing 3-branch
 * [SADState.ViewState] (Loading/Error/Content). Detail flows shouldn't emit
 * [ScreenState.Empty] (single-record endpoints return Content or Error) but
 * we defensively route it to Error. NoNetwork / Unauthenticated fold into
 * Error until Phase-4 wires per-branch messaging.
 */
private fun ScreenState<SavingAccountDetail>.toViewState(): SADState.ViewState = when (this) {
    is ScreenState.Loading -> SADState.ViewState.Loading
    is ScreenState.Empty -> Error("Account not found.")
    is ScreenState.Content -> SADState.ViewState.Content(data)
    is ScreenState.Error -> Error(error.message.toString())
    is ScreenState.NoNetwork -> Error("No network. Please check your connection.")
    is ScreenState.Unauthenticated -> Error("Session expired. Please log in again.")
}

@Serializable
internal data class SADState(
    val accountId: Long,
    val viewState: ViewState,
) {
    @Serializable
    sealed interface ViewState {
        @Serializable
        data object Loading : ViewState

        @Serializable
        data class Error(val message: String) : ViewState

        @Serializable
        data class Content(val data: SavingAccountDetail) : ViewState
    }
}

internal sealed interface SADEvent {
    data class ShowToast(val message: String) : SADEvent
    data class OnViewTransaction(val clientId: Long, val accountId: Long) : SADEvent
    data object NavigateBack : SADEvent
}

internal sealed interface SADAction {
    data object NavigateBack : SADAction
    data class ViewTransaction(val clientId: Long, val accountId: Long) : SADAction
}
