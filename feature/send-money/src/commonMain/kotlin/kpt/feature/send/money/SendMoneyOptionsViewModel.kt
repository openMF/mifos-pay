/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.data.util.StandardUpiQrCodeProcessor
import kpt.core.ui.utils.BackgroundEvent
import kpt.core.ui.utils.BaseViewModel
class SendMoneyOptionsViewModel(
    private val scanner: QrScanner,
) : BaseViewModel<SendMoneyOptionsState, SendMoneyOptionsEvent, SendMoneyOptionsAction>(
    initialState = SendMoneyOptionsState(),
) {

    override fun handleAction(action: SendMoneyOptionsAction) {
        when (action) {
            is SendMoneyOptionsAction.NavigateBack -> {
                sendEvent(SendMoneyOptionsEvent.NavigateBack)
            }
            is SendMoneyOptionsAction.ScanQrClicked -> {
                // Use ML Kit QR scanner directly
                scanner.startScanning().onEach { data ->
                    data?.let { result ->
                        // Check if it's a UPI QR code or regular QR code
                        if (StandardUpiQrCodeProcessor.isValidUpiQrCode(result)) {
                            // Navigate to payee details screen for UPI QR codes
                            sendEvent(SendMoneyOptionsEvent.NavigateToPayeeDetails(result))
                        } else {
                            // For non-UPI QR codes, navigate to Fineract payment
                            sendEvent(SendMoneyOptionsEvent.QrCodeScanned(result))
                        }
                    }
                }.launchIn(viewModelScope)
            }
            is SendMoneyOptionsAction.PayAnyoneClicked -> {
                sendEvent(SendMoneyOptionsEvent.NavigateToPayAnyone)
            }
            is SendMoneyOptionsAction.BankTransferClicked -> {
                sendEvent(SendMoneyOptionsEvent.NavigateToBankTransfer)
            }
            is SendMoneyOptionsAction.FineractPaymentsClicked -> {
                sendEvent(SendMoneyOptionsEvent.NavigateToFineractPayments)
            }
            is SendMoneyOptionsAction.AutoPayClicked -> {
                sendEvent(SendMoneyOptionsEvent.NavigateToAutoPay)
            }
        }
    }
}

data class SendMoneyOptionsState(
    val isLoading: Boolean = false,
)

sealed interface SendMoneyOptionsEvent {
    data object NavigateBack : SendMoneyOptionsEvent
    data object NavigateToPayAnyone : SendMoneyOptionsEvent
    data object NavigateToBankTransfer : SendMoneyOptionsEvent
    data object NavigateToFineractPayments : SendMoneyOptionsEvent
    data object NavigateToAutoPay : SendMoneyOptionsEvent
    data class QrCodeScanned(val data: String) : SendMoneyOptionsEvent, BackgroundEvent
    data class NavigateToPayeeDetails(val qrCodeData: String) : SendMoneyOptionsEvent, BackgroundEvent
}

sealed interface SendMoneyOptionsAction {
    data object NavigateBack : SendMoneyOptionsAction
    data object ScanQrClicked : SendMoneyOptionsAction
    data object PayAnyoneClicked : SendMoneyOptionsAction
    data object BankTransferClicked : SendMoneyOptionsAction
    data object FineractPaymentsClicked : SendMoneyOptionsAction
    data object AutoPayClicked : SendMoneyOptionsAction
}
