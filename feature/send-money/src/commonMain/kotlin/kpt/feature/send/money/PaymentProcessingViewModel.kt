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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.ui.utils.BaseViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class PaymentProcessingViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PaymentProcessingState, PaymentProcessingEvent, PaymentProcessingAction>(
    initialState = PaymentProcessingState(),
) {

    init {
        val payeeName = savedStateHandle.get<String>("payeeName") ?: ""
        val amountInPaise = savedStateHandle.get<String>("amount") ?: ""
        val isUpiCode = savedStateHandle.get<Boolean>("isUpiCode") ?: false

        mutableStateFlow.update {
            it.copy(
                payeeName = payeeName,
                amount = amountInPaise,
                isUpiCode = isUpiCode,
            )
        }

        startPaymentProcessing()
    }

    private fun startPaymentProcessing() {
        viewModelScope.launch {
            try {
                mutableStateFlow.update { it.copy(isProcessing = true) }

                delay(3000)

                mutableStateFlow.update { it.copy(isProcessing = false) }

                delay(1000)

                sendEvent(
                    PaymentProcessingEvent.PaymentComplete(
                        payeeName = state.payeeName,
                        amount = state.amount,
                        upiName = state.payeeName.uppercase(),
                        transactionTimestamp = getCurrentUnixTimestamp(),
                    ),
                )
            } catch (e: Exception) {
                sendEvent(PaymentProcessingEvent.PaymentFailed(e.message ?: "Payment failed"))
            }
        }
    }

    override fun handleAction(action: PaymentProcessingAction) {
        when (action) {
            PaymentProcessingAction.RetryPayment -> {
                startPaymentProcessing()
            }
        }
    }

    /**
     * Gets the current Unix timestamp from the mobile device
     * This is used as a fallback when PSP timestamp is not available
     */
    @OptIn(ExperimentalTime::class)
    private fun getCurrentUnixTimestamp(): String {
        return Clock.System.now().epochSeconds.toString()
    }
}

data class PaymentProcessingState(
    val payeeName: String = "",
    val amount: String = "",
    val isUpiCode: Boolean = false,
    val isProcessing: Boolean = true,
) {
    val formattedAmount: String
        get() = AmountUtils.formatPaiseForUI(amount)
}

sealed interface PaymentProcessingEvent {
    data class PaymentComplete(
        val payeeName: String,
        val amount: String,
        val upiName: String,
        val transactionTimestamp: String,
    ) : PaymentProcessingEvent
    data class PaymentFailed(val errorMessage: String) : PaymentProcessingEvent
}

sealed interface PaymentProcessingAction {
    data object RetryPayment : PaymentProcessingAction
}
