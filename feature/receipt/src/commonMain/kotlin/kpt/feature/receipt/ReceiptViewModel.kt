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
import kpt.core.data.repository.AccountRepository
import kpt.core.data.util.toForkScreenStateFlow
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.ui.utils.BaseViewModel
import mifos_pay.feature.receipt.generated.resources.Res
import mifos_pay.feature.receipt.generated.resources.feature_receipt_not_found
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

sealed interface ReceiptEvent {
    data object OnNavigateBack : ReceiptEvent
    data object OnShareReceipt : ReceiptEvent
}

sealed interface ReceiptAction {
    data object NavigateBack : ReceiptAction
    data object ShareReceipt : ReceiptAction
}

sealed interface ReceiptUiState {
    data class Success(
        val transferDetail: TransferDetail,
    ) : ReceiptUiState

    data class Error(
        val messageRes: StringResource,
    ) : ReceiptUiState

    data class ErrorMessage(
        val message: String,
    ) : ReceiptUiState

    data object Loading : ReceiptUiState
}
