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
import kpt.core.ui.utils.BaseViewModel
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

sealed interface MerchantTransferEvent

sealed interface MerchantTransferAction

sealed class MerchantTransferUiState {
    data object Loading : MerchantTransferUiState()
    class Success(val transactionsList: List<Transaction>) : MerchantTransferUiState()
    data object Empty : MerchantTransferUiState()
    data object Error : MerchantTransferUiState()
    data object InsufficientBalance : MerchantTransferUiState()
}
