/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.ui.viewmodel.BaseViewModel

class TransferOptionsViewModel : BaseViewModel<TransferOptionState, Unit, Unit>(
    initialState = TransferOptionState(),
) {

    init {
        fetchTransferOptions()
    }

    override fun handleAction(action: Unit) = Unit

    private fun fetchTransferOptions() {
        viewModelScope.launch {
            val countryCode = getCountryCode()

            val isUpiEnabled = countryCode == "IN"

            mutableStateFlow.update {
                it.copy(
                    isLoading = false,
                    isUpiEnabled = isUpiEnabled,
                    isInterTransferOptionEnabled = true,
                )
            }
        }
    }

    private fun getCountryCode(): String {
        // Dummy API / device locale simulation

        return "US"
    }
}

data class TransferOptionState(
    val isLoading: Boolean = true,
    val isInterTransferOptionEnabled: Boolean = false,
    val isUpiEnabled: Boolean = false,
)
