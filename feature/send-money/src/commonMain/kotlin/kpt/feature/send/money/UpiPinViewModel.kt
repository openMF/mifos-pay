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
class UpiPinViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<UpiPinState, UpiPinEvent, UpiPinAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val payeeName = requireNotNull(savedStateHandle.get<String>("payeeName"))
        val amountInPaise = requireNotNull(savedStateHandle.get<String>("amount"))
        val isUpiCode = requireNotNull(savedStateHandle.get<Boolean>("isUpiCode"))
        val bankName = requireNotNull(savedStateHandle.get<String>("bankName"))
        val accountNo = requireNotNull(savedStateHandle.get<String>("accountNo"))

        val refId = generateRefId()

        val amountInRupees = if (amountInPaise.isNotEmpty()) {
            AmountUtils.paiseToRupees(amountInPaise).toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

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

    companion object {
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

@Serializable
data class UpiPinState(
    val paymentDetails: PaymentDetails,
    val amount: Double,
    val refId: String,
    val isUpiCode: Boolean,
)

sealed interface UpiPinEvent {
    data class OnUpiPinEntered(val pin: String) : UpiPinEvent
}

sealed interface UpiPinAction {
    data class UpiPinEntered(val pin: String) : UpiPinAction
}
