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

import kpt.core.ui.utils.BaseViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel for the Payment Chat History screen
 * Manages payment history state and user interactions
 * Currently uses placeholder data for demonstration
 */
class PaymentChatHistoryViewModel :
    BaseViewModel<PaymentChatHistoryState, PaymentChatHistoryEvent, PaymentChatHistoryAction>(
        initialState = createPlaceholderState(),
    ) {

    override fun handleAction(action: PaymentChatHistoryAction) {
        when (action) {
            is PaymentChatHistoryAction.SendMessage -> {
                // TODO: Implement actual message sending logic
                // For now, just log the message
                println("Message sent: ${action.message}")
            }
        }
    }

    /**
     * Sends a message to the recipient
     * Currently just logs the message for demonstration
     */
    fun sendMessage(message: String) {
        trySendAction(PaymentChatHistoryAction.SendMessage(message))
    }

    companion object {
        /**
         * Creates placeholder data for demonstration purposes
         * Includes sample payment history with different user types
         */
        private fun createPlaceholderState(): PaymentChatHistoryState {
            return PaymentChatHistoryState(
                userType = UserType.INDIVIDUAL,
                businessName = "TechCorp Solutions",
                bankingName = "Jane Smith",
                upiId = "jane.smith@upi",
                profileImageUrl = null,
                paymentHistory = createPlaceholderPaymentHistory(),
                isLoading = false,
                error = null,
            )
        }

        /**
         * Creates sample payment history data grouped by date
         * Demonstrates the chat-like payment history interface
         * Most recent transactions appear at the bottom (like chat messages)
         */
        @OptIn(ExperimentalTime::class)
        private fun createPlaceholderPaymentHistory(): List<PaymentHistoryGroup> {
            return listOf(
                PaymentHistoryGroup(
                    date = "25 Sept 2025, 3:15 PM",
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "txn_005",
                            recipientName = "Jane Smith",
                            amount = "3,200.00",
                            note = "Shopping payment",
                            paymentDate = "25 Sep 2025",
                            timestamp = Clock.System.now().toEpochMilliseconds() - 172800000,
                            isSent = true,
                        ),
                    ),
                ),
                PaymentHistoryGroup(
                    date = "26 Sept 2025, 8:45 PM",
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "txn_003",
                            recipientName = "Jane Smith",
                            amount = "2,000.00",
                            note = "Dinner payment",
                            paymentDate = "26 Sep 2025",
                            timestamp = Clock.System.now().toEpochMilliseconds() - 86400000,
                            isSent = true,
                        ),
                        PaymentTransaction(
                            transactionId = "txn_004",
                            recipientName = "You",
                            amount = "750.00",
                            note = "Movie tickets",
                            paymentDate = "26 Sep 2025",
                            timestamp = Clock.System.now().toEpochMilliseconds() - 90000000,
                            isSent = false,
                        ),
                    ),
                ),
                PaymentHistoryGroup(
                    date = "27 Sept 2025, 2:30 PM",
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "txn_001",
                            recipientName = "Jane Smith",
                            amount = "1,500.00",
                            note = "Lunch payment",
                            paymentDate = "27 Sep 2025",
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            isSent = true,
                        ),
                        PaymentTransaction(
                            transactionId = "txn_002",
                            recipientName = "Jane Smith",
                            amount = "500.00",
                            note = "Coffee and snacks",
                            paymentDate = "27 Sep 2025",
                            timestamp = Clock.System.now().toEpochMilliseconds() - 3600000,
                            isSent = true,
                        ),
                    ),
                ),
            )
        }
    }
}

sealed interface PaymentChatHistoryEvent

sealed interface PaymentChatHistoryAction {
    data class SendMessage(val message: String) : PaymentChatHistoryAction
}
