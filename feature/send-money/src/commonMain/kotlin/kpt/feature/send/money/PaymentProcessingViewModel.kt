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

/** ViewModel for the payment processing screen; builds its state from the navigation arguments. */
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

/**
 * State of the payment processing screen.
 * @property payeeName Name of the payee being paid.
 * @property amount Amount being paid, in paise.
 * @property isUpiCode Whether the payment was started from a UPI code.
 * @property isProcessing Whether the payment is still being processed.
 */
data class PaymentProcessingState(
    val payeeName: String = "",
    val amount: String = "",
    val isUpiCode: Boolean = false,
    val isProcessing: Boolean = true,
) {
    /** The amount formatted for display. */
    val formattedAmount: String
        get() = AmountUtils.formatPaiseForUI(amount)
}

/** One-off events emitted by the payment processing screen. */
sealed interface PaymentProcessingEvent {
    /**
     * Emitted when the payment completes.
     * @property payeeName Name of the payee that was paid.
     * @property amount Amount paid.
     * @property upiName UPI name used for the payment.
     * @property transactionTimestamp Formatted time of the transaction.
     */
    data class PaymentComplete(
        val payeeName: String,
        val amount: String,
        val upiName: String,
        val transactionTimestamp: String,
    ) : PaymentProcessingEvent

    /**
     * Emitted when the payment fails.
     * @property errorMessage Reason the payment failed.
     */
    data class PaymentFailed(val errorMessage: String) : PaymentProcessingEvent
}

/** User actions on the payment processing screen. */
sealed interface PaymentProcessingAction {
    /** The user asked to retry the payment. */
    data object RetryPayment : PaymentProcessingAction
}
