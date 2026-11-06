/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kpt.core.common.ScreenState
import kpt.core.data.repository.AccountRepository
import kpt.core.data.util.toForkScreenStateFlow
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.history.detail.TransactionDetailState.ViewState.Content
import kpt.feature.history.detail.TransactionDetailState.ViewState.Error
import mifos_pay.feature.history.generated.resources.Res
import mifos_pay.feature.history.generated.resources.feature_history_error_fallback
import org.jetbrains.compose.resources.StringResource
internal class TransactionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountRepository: AccountRepository,
) : BaseViewModel<TransactionDetailState, TransactionDetailEvent, TransactionDetailAction>(
    initialState = TransactionDetailState(TransactionDetailState.ViewState.Loading),
) {

    companion object {
        private const val TRANSFER_ID_KEY = "transferId"
    }

    /** Reused by [TransactionDetailScreen]'s Share action to open Receipt on the same transfer. */
    val transferId: Long? = savedStateHandle.get<Long>(TRANSFER_ID_KEY)

    init {
        transferId?.let { transferId ->
            // transfer-detail Store5 vertical (GOAL D13) — switched from the
            // transitional `getAccountTransfer(transferId)` (`asScreenStateFlow`
            // shim over the raw Ktorfit flow) to the store-native
            // `getAccountTransferStream(...)` that consumes the `transferDetail`
            // Store5 read (`createStore` + Room SoT + CACHE_FIRST_SWR + single-row
            // upsert), making the transaction-detail drill-down offline-first. The
            // `.state` Flow is bridged to the fork ScreenState and folded through
            // the same `observeScreen` reducer; consumer branches are unchanged.
            // Requires `viewModelScope` for the stream's internal reconnect +
            // periodic + SWR side-fetch coroutines.
            accountRepository.getAccountTransferStream(transferId, viewModelScope)
                .state.toForkScreenStateFlow()
                .observeScreen { screenState ->
                    mutableStateFlow.update { it.copy(viewState = screenState.toViewState()) }
                }
        }
    }

    override fun handleAction(action: TransactionDetailAction) {
        when (action) {
            is TransactionDetailAction.NavigateBack -> {
                sendEvent(TransactionDetailEvent.OnNavigateBack)
            }

            TransactionDetailAction.ShareTransaction -> {
                sendEvent(TransactionDetailEvent.OnShareTransaction)
            }
        }
    }
}

/**
 * Fold the 6-branch [ScreenState] into the existing 3-branch
 * [TransactionDetailState.ViewState] (Loading / Error{String,Resource} /
 * Content). The Error sub-hierarchy is preserved:
 *  - unwrapped exception with a non-empty message → [Error.StringMessage]
 *  - anything else (blank message, Empty, NoNetwork, Unauthenticated) →
 *    [Error.ResourceMessage] with the localised fallback string.
 */
private fun ScreenState<TransferDetail>.toViewState(): TransactionDetailState.ViewState =
    when (this) {
        is ScreenState.Loading -> TransactionDetailState.ViewState.Loading

        is ScreenState.Empty ->
            Error.ResourceMessage(Res.string.feature_history_error_fallback)

        is ScreenState.Content -> Content(data)

        is ScreenState.Error -> {
            val message = error.message
            if (message.isNullOrEmpty()) {
                Error.ResourceMessage(Res.string.feature_history_error_fallback)
            } else {
                Error.StringMessage(message)
            }
        }

        is ScreenState.NoNetwork ->
            Error.ResourceMessage(Res.string.feature_history_error_fallback)

        is ScreenState.Unauthenticated ->
            Error.ResourceMessage(Res.string.feature_history_error_fallback)
    }

internal data class TransactionDetailState(
    val viewState: ViewState,
) {
    internal sealed interface ViewState {
        data object Loading : ViewState

        sealed interface Error : ViewState {
            data class StringMessage(val message: String) : Error
            data class ResourceMessage(val message: StringResource) : Error
        }
        data class Content(val transaction: TransferDetail) : ViewState
    }
}

internal sealed interface TransactionDetailEvent {
    data object OnNavigateBack : TransactionDetailEvent
    data object OnShareTransaction : TransactionDetailEvent
}

internal sealed interface TransactionDetailAction {
    data object NavigateBack : TransactionDetailAction
    data object ShareTransaction : TransactionDetailAction
}
