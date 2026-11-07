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
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.common.DateHelper
import kpt.core.ui.utils.BaseViewModel
import kotlin.time.ExperimentalTime

/** ViewModel for the payment success screen; builds its state from the navigation arguments. */
class PaymentSuccessViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PaymentSuccessState, PaymentSuccessEvent, PaymentSuccessAction>(
    initialState = PaymentSuccessState(),
) {

    init {
        val payeeName = savedStateHandle.get<String>("payeeName") ?: ""
        val amountInPaise = savedStateHandle.get<String>("amount") ?: ""
        val upiName = savedStateHandle.get<String>("upiName") ?: payeeName
        val transactionTimestamp = savedStateHandle.get<String>("transactionTimestamp") // Unix timestamp from PSP or mobile device
        val timestamp = if (transactionTimestamp != null) {
            formatTransactionTimestamp(transactionTimestamp)
        } else {
            getCurrentTimestamp()
        }

        mutableStateFlow.update {
            it.copy(
                payeeName = payeeName,
                amount = amountInPaise,
                upiName = upiName,
                timestamp = timestamp,
            )
        }
    }

    override fun handleAction(action: PaymentSuccessAction) {
        when (action) {
            PaymentSuccessAction.ShareScreenshot -> {
                sendEvent(PaymentSuccessEvent.ShareScreenshot)
            }
            PaymentSuccessAction.Done -> {
                sendEvent(PaymentSuccessEvent.NavigateToSendMoneyOptions)
            }
        }
    }

    /**
     * Formats Unix timestamp from PSP or mobile device into readable format
     * Falls back to current device timestamp if parsing fails
     */
    @OptIn(ExperimentalTime::class)
    private fun formatTransactionTimestamp(unixTimestamp: String): String {
        return try {
            val timestamp = unixTimestamp.toLong()
            val instant = Instant.fromEpochSeconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val day = localDateTime.dayOfMonth
            val month = localDateTime.month.name.lowercase().capitalize()
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (localDateTime.hour < 12) "am" else "pm"

            "$day $month $year, $hour:$minute $amPm"
        } catch (e: Exception) {
            // Fallback to current timestamp if parsing fails
            getCurrentTimestamp()
        }
    }

    private fun getCurrentTimestamp(): String {
        val currentDateTime = DateHelper.currentDate
        val day = currentDateTime.dayOfMonth
        val month = currentDateTime.month.name.lowercase().capitalize()
        val year = currentDateTime.year
        val hour = currentDateTime.hour.toString().padStart(2, '0')
        val minute = currentDateTime.minute.toString().padStart(2, '0')
        val amPm = if (currentDateTime.hour < 12) "am" else "pm"

        return "$day $month $year, $hour:$minute $amPm"
    }
}

// amount stored in paise
/**
 * State of the payment success screen.
 * @property payeeName Name of the payee that was paid.
 * @property amount Amount paid, in paise.
 * @property upiName UPI name used for the payment.
 * @property timestamp Formatted time of the payment.
 */
data class PaymentSuccessState(
    val payeeName: String = "",
    val amount: String = "",
    val upiName: String = "",
    val timestamp: String = "",
) {
    /** The amount formatted for display. */
    val formattedAmount: String
        get() = AmountUtils.formatPaiseForUI(amount)
}

/** One-off events emitted by the payment success screen. */
sealed interface PaymentSuccessEvent {
    /** Share a screenshot of the success screen. */
    data object ShareScreenshot : PaymentSuccessEvent

    /** Navigate back to the home screen. */
    data object NavigateToHome : PaymentSuccessEvent

    /** Navigate to the send-money options screen. */
    data object NavigateToSendMoneyOptions : PaymentSuccessEvent
}

/** User actions on the payment success screen. */
sealed interface PaymentSuccessAction {
    /** The user tapped share. */
    data object ShareScreenshot : PaymentSuccessAction

    /** The user tapped done. */
    data object Done : PaymentSuccessAction
}
