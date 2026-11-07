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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.ui.utils.BaseViewModel
/** ViewModel for the UPI PIN screen; builds its state from the navigation arguments. */
class UpiPinViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<UpiPinState, UpiPinEvent, UpiPinAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        /** Name of the payee, from the navigation arguments. */
        val payeeName = requireNotNull(savedStateHandle.get<String>("payeeName"))

        /** Amount in paise, from the navigation arguments. */
        val amountInPaise = requireNotNull(savedStateHandle.get<String>("amount"))

        /** Whether the payment came from a UPI code, from the navigation arguments. */
        val isUpiCode = requireNotNull(savedStateHandle.get<Boolean>("isUpiCode"))

        /** Name of the paying bank, from the navigation arguments. */
        val bankName = requireNotNull(savedStateHandle.get<String>("bankName"))

        /** Paying account number, from the navigation arguments. */
        val accountNo = requireNotNull(savedStateHandle.get<String>("accountNo"))

        /** Generated reference id for this payment. */
        val refId = generateRefId()

        /** Amount converted from paise to rupees. */
        val amountInRupees = if (amountInPaise.isNotEmpty()) {
            AmountUtils.paiseToRupees(amountInPaise).toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        /** Payment details shown on the PIN screen. */
        val paymentDetails = PaymentDetails(
            payeeName = payeeName,
            bankName = bankName,
            accountNumber = accountNo,
            amount = amountInRupees,
            refId = refId,
        )

        UpiPinState(
            paymentDetails = paymentDetails,
            isUpiCode = isUpiCode,
            amount = amountInRupees,
            refId = refId,
        )
    },
) {

    private companion object {
        private const val KEY_STATE = "upi_pin_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: UpiPinAction) {
        when (action) {
            is UpiPinAction.UpiPinEntered -> {
                sendEvent(UpiPinEvent.OnUpiPinEntered(action.pin))
            }
        }
    }
}

/**
 * Generates a unique 36-character alphanumeric reference ID for the transaction
 */
private fun generateRefId(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..36).map { chars.random() }.joinToString("")
}

/**
 * State of the UPI PIN screen.
 * @property paymentDetails Details of the payment being authorized.
 * @property amount Amount in rupees.
 * @property refId Reference id of the payment.
 * @property isUpiCode Whether the payment came from a UPI code.
 */
@Serializable
data class UpiPinState(
    val paymentDetails: PaymentDetails,
    val amount: Double,
    val refId: String,
    val isUpiCode: Boolean,
)

/** One-off events emitted by the UPI PIN screen. */
sealed interface UpiPinEvent {
    /**
     * Emitted when the PIN has been entered.
     * @property pin The entered UPI PIN.
     */
    data class OnUpiPinEntered(val pin: String) : UpiPinEvent
}

/** User actions on the UPI PIN screen. */
sealed interface UpiPinAction {
    /**
     * The user finished entering the PIN.
     * @property pin The entered UPI PIN.
     */
    data class UpiPinEntered(val pin: String) : UpiPinAction
}
