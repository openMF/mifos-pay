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
/**
 * ViewModel for the send-money options screen; maps option taps and QR scans to navigation events.
 * @property scanner QR scanner used by the scan option.
 */
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

/**
 * State of the send-money options screen.
 * @property isLoading Whether a request is in progress.
 */
data class SendMoneyOptionsState(
    val isLoading: Boolean = false,
)

/** One-off navigation events emitted by the send-money options screen. */
sealed interface SendMoneyOptionsEvent {
    /** Navigate back. */
    data object NavigateBack : SendMoneyOptionsEvent

    /** Navigate to the pay-anyone screen. */
    data object NavigateToPayAnyone : SendMoneyOptionsEvent

    /** Navigate to the bank transfer screen. */
    data object NavigateToBankTransfer : SendMoneyOptionsEvent

    /** Navigate to the Fineract payments screen. */
    data object NavigateToFineractPayments : SendMoneyOptionsEvent

    /** Navigate to the auto-pay screen. */
    data object NavigateToAutoPay : SendMoneyOptionsEvent

    /**
     * A QR code was scanned.
     * @property data Raw scanned value.
     */
    data class QrCodeScanned(val data: String) : SendMoneyOptionsEvent, BackgroundEvent

    /**
     * Navigate to payee details for a scanned QR code.
     * @property qrCodeData Raw scanned QR value.
     */
    data class NavigateToPayeeDetails(val qrCodeData: String) : SendMoneyOptionsEvent, BackgroundEvent
}

/** User actions on the send-money options screen. */
sealed interface SendMoneyOptionsAction {
    /** The user tapped back. */
    data object NavigateBack : SendMoneyOptionsAction

    /** The user tapped scan QR. */
    data object ScanQrClicked : SendMoneyOptionsAction

    /** The user tapped pay anyone. */
    data object PayAnyoneClicked : SendMoneyOptionsAction

    /** The user tapped bank transfer. */
    data object BankTransferClicked : SendMoneyOptionsAction

    /** The user tapped Fineract payments. */
    data object FineractPaymentsClicked : SendMoneyOptionsAction

    /** The user tapped auto-pay. */
    data object AutoPayClicked : SendMoneyOptionsAction
}
