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

import kpt.core.common.CurrencyFormatter
import kpt.core.ui.utils.BaseViewModel
/**
 * ViewModel for the Payment Details screen
 * Manages payment details state and provides comprehensive transaction information
 * Currently uses placeholder data for demonstration purposes
 */
class PaymentDetailsViewModel :
    BaseViewModel<PaymentDetailsState, PaymentDetailsEvent, PaymentDetailsAction>(
        initialState = createPlaceholderState(),
    ) {

    override fun handleAction(action: PaymentDetailsAction) {
        when (action) {
            is PaymentDetailsAction.Initialize -> {
                // TODO: Fetch transaction details from API using transactionId
                // For now, we'll use placeholder data
                mutableStateFlow.value = createPlaceholderState().copy(
                    upiTransactionId = action.transactionId,
                    mifosTransactionId = "MF${action.transactionId}",
                )
            }

            is PaymentDetailsAction.UpdatePaymentStatus -> {
                mutableStateFlow.value = state.copy(
                    isPaymentSuccessful = action.isSuccessful,
                )
            }

            is PaymentDetailsAction.UpdateAmount -> {
                val formattedAmount = try {
                    val amountValue = action.newAmount.replace(",", "").toDoubleOrNull() ?: 0.0
                    CurrencyFormatter.format(
                        balance = amountValue,
                        currencyCode = "INR",
                        maximumFractionDigits = 2,
                    )
                } catch (e: Exception) {
                    "₹${action.newAmount}"
                }

                mutableStateFlow.value = state.copy(
                    amount = action.newAmount,
                    formattedAmount = formattedAmount,
                )
            }
        }
    }

    /**
     * Initializes the ViewModel with transaction ID
     * In a real app, this would fetch transaction details from the API
     */
    fun initialize(transactionId: String) {
        trySendAction(PaymentDetailsAction.Initialize(transactionId))
    }

    /**
     * Updates the payment status for demonstration purposes
     * In a real app, this would be called when payment status changes
     */
    fun updatePaymentStatus(isSuccessful: Boolean) {
        trySendAction(PaymentDetailsAction.UpdatePaymentStatus(isSuccessful))
    }

    /**
     * Updates the payment amount for demonstration purposes
     * In a real app, this would be called when amount changes
     */
    fun updateAmount(newAmount: String) {
        trySendAction(PaymentDetailsAction.UpdateAmount(newAmount))
    }

    companion object {
        /**
         * Creates placeholder data for demonstration purposes
         * Includes sample payment details with different scenarios
         */
        private fun createPlaceholderState(): PaymentDetailsState {
            return PaymentDetailsState(
                payeeName = "Jane Smith",
                isBusiness = false,
                bankingName = "TechCorp Solutions",
                phoneNumber = "+91 98765 43210",
                upiId = "jane.smith@upi",
                profileImageUrl = null,
                amount = "1,500.00",
                formattedAmount = "₹1,500.00",
                note = "Lunch payment",
                isPaymentSuccessful = true,
                upiTransactionId = "UPI123456789012345",
                payerName = "Biplab Dutta",
                payerBankName = "HDFC Bank",
                payerAccountLast4Digits = "1234",
                payerUpiAppName = "Google Pay",
                payerUpiId = "biplabdutta@okicici",
                upiAppName = "PhonePe",
                mifosTransactionId = "MF123456789",
                transactionDate = "27 Sept 2025, 2:30 PM",
            )
        }
    }
}

sealed interface PaymentDetailsEvent

sealed interface PaymentDetailsAction {
    data class Initialize(val transactionId: String) : PaymentDetailsAction
    data class UpdatePaymentStatus(val isSuccessful: Boolean) : PaymentDetailsAction
    data class UpdateAmount(val newAmount: String) : PaymentDetailsAction
}
