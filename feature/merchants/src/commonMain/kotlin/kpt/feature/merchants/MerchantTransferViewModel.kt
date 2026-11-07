/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.merchants

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.savingsaccount.Transaction
import kpt.core.ui.fork.utils.BaseViewModel
/** ViewModel of the merchant transfer screen; currently a stub with no backend or actions. */
class MerchantTransferViewModel(
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<MerchantTransferUiState, MerchantTransferEvent, MerchantTransferAction>(
    initialState = MerchantTransferUiState.Empty,
) {

    /**
     * Preserves the original public surface (`viewModel.uiState`) so the Screen
     * compiles unchanged — aliased onto [BaseViewModel.stateFlow].
     */
    val uiState: StateFlow<MerchantTransferUiState> get() = stateFlow

    // Stub feature: no backend, no user-driven actions today. The action stream
    // stays empty until a merchant-transfer endpoint materializes.
    override fun handleAction(action: MerchantTransferAction) = Unit
}

/** One-off events of the merchant transfer screen (none defined). */
sealed interface MerchantTransferEvent

/** User actions of the merchant transfer screen (none defined). */
sealed interface MerchantTransferAction

/** UI states of the merchant transfer screen. */
sealed class MerchantTransferUiState {
    /** Transactions are being loaded. */
    data object Loading : MerchantTransferUiState()

    /**
     * Transactions loaded.
     *
     * @property transactionsList The loaded transactions.
     */
    class Success(val transactionsList: List<Transaction>) : MerchantTransferUiState()

    /** No transactions to show. */
    data object Empty : MerchantTransferUiState()

    /** Loading failed. */
    data object Error : MerchantTransferUiState()

    /** The account balance is too low for the transfer. */
    data object InsufficientBalance : MerchantTransferUiState()
}
