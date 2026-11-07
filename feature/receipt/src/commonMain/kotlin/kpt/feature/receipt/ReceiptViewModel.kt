/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kpt.core.common.ScreenState
import kpt.core.data.fork.util.toForkScreenStateFlow
import kpt.core.data.repository.AccountRepository
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.receipt.generated.resources.Res
import kpt.feature.receipt.generated.resources.feature_receipt_not_found
import org.jetbrains.compose.resources.StringResource
/**
 * Reads OFF the shared `TransferDetail` Store5 vertical — the SAME store
 * `TransactionDetailViewModel` (`feature/history`) already consumes via
 * [AccountRepository.getAccountTransferStream]. No dedicated `ReceiptStore` /
 * `ReceiptEntity` is authored for this feature (D13 verdict — see
 * `sub-plans/RECEIPT_DATA_SOURCE.md`): the row is cached the moment the user
 * viewed the transaction detail, and `TransferDetail` already carries every
 * field a receipt card needs (amount, date, currency, from/to, reference id).
 */
class ReceiptViewModel(
    savedStateHandle: SavedStateHandle,
    accountRepository: AccountRepository,
) : BaseViewModel<ReceiptUiState, ReceiptEvent, ReceiptAction>(
    initialState = ReceiptUiState.Loading,
) {

    /** Holds the saved-state key for the transfer id. */
    companion object {
        private const val TRANSFER_ID_KEY = "transferId"
    }

    init {
        val transferId = savedStateHandle.get<Long>(TRANSFER_ID_KEY)
        if (transferId != null) {
            accountRepository.getAccountTransferStream(transferId, viewModelScope)
                .state.toForkScreenStateFlow()
                .observeScreen { screenState ->
                    mutableStateFlow.update { screenState.toReceiptUiState() }
                }
        } else {
            mutableStateFlow.update { ReceiptUiState.Error(Res.string.feature_receipt_not_found) }
        }
    }

    /**
     * Preserves the original public surface (`viewModel.receiptUiState`) so the
     * Screen compiles unchanged — aliased onto [BaseViewModel.stateFlow].
     */
    val receiptUiState: StateFlow<ReceiptUiState> get() = stateFlow

    override fun handleAction(action: ReceiptAction) {
        when (action) {
            ReceiptAction.NavigateBack -> sendEvent(ReceiptEvent.OnNavigateBack)
            ReceiptAction.ShareReceipt -> sendEvent(ReceiptEvent.OnShareReceipt)
        }
    }
}

private fun ScreenState<TransferDetail>.toReceiptUiState(): ReceiptUiState = when (this) {
    is ScreenState.Loading -> ReceiptUiState.Loading
    is ScreenState.Content -> ReceiptUiState.Success(data)
    is ScreenState.Empty -> ReceiptUiState.Error(Res.string.feature_receipt_not_found)
    is ScreenState.Error -> {
        val message = error.message
        if (message.isNullOrEmpty()) {
            ReceiptUiState.Error(Res.string.feature_receipt_not_found)
        } else {
            ReceiptUiState.ErrorMessage(message)
        }
    }
    is ScreenState.NoNetwork -> ReceiptUiState.Error(Res.string.feature_receipt_not_found)
    is ScreenState.Unauthenticated -> ReceiptUiState.Error(Res.string.feature_receipt_not_found)
}

/** One-shot events emitted by [ReceiptViewModel]. */
sealed interface ReceiptEvent {
    /** Event requesting navigation back. */
    data object OnNavigateBack : ReceiptEvent

    /** Event requesting the receipt to be shared. */
    data object OnShareReceipt : ReceiptEvent
}

/** Actions dispatched to [ReceiptViewModel]. */
sealed interface ReceiptAction {
    /** User requested to navigate back. */
    data object NavigateBack : ReceiptAction

    /** User requested to share the receipt. */
    data object ShareReceipt : ReceiptAction
}

/** UI state of the receipt screen. */
sealed interface ReceiptUiState {
    /**
     * Receipt loaded successfully.
     *
     * @property transferDetail Transfer details rendered on the receipt.
     */
    data class Success(
        val transferDetail: TransferDetail,
    ) : ReceiptUiState

    /**
     * Receipt failed to load, with a string-resource error message.
     *
     * @property messageRes String resource of the error message.
     */
    data class Error(
        val messageRes: StringResource,
    ) : ReceiptUiState

    /**
     * Receipt failed to load, with a literal error message.
     *
     * @property message Error message text to display.
     */
    data class ErrorMessage(
        val message: String,
    ) : ReceiptUiState

    /** Receipt is being loaded. */
    data object Loading : ReceiptUiState
}
