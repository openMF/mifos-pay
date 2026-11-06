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

/**
 * Data class representing the state of the Payment Details screen
 * Contains all necessary information for displaying comprehensive payment details
 */
data class PaymentDetailsState(
    val payeeName: String = "",
    val isBusiness: Boolean = false,
    val bankingName: String = "",
    val phoneNumber: String = "",
    val upiId: String = "",
    val profileImageUrl: String? = null,
    val amount: String = "",
    val formattedAmount: String = "",
    val note: String = "",
    val isPaymentSuccessful: Boolean = true,
    val upiTransactionId: String = "",
    val payerName: String = "",
    val payerBankName: String = "",
    val payerAccountLast4Digits: String = "",
    val payerUpiAppName: String = "",
    val payerUpiId: String = "",
    val upiAppName: String = "",
    val mifosTransactionId: String = "",
    val transactionDate: String = "",
)
