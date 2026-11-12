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
 *
 * @property payeeName Name of the person or business that was paid.
 * @property isBusiness Whether the payee is a business.
 * @property bankingName Registered banking name of the payee.
 * @property phoneNumber Phone number of the payee.
 * @property upiId UPI ID of the payee.
 * @property profileImageUrl URL of the payee's profile image, if any.
 * @property amount Raw payment amount.
 * @property formattedAmount Payment amount formatted for display.
 * @property note Note attached to the payment.
 * @property isPaymentSuccessful Whether the payment succeeded.
 * @property upiTransactionId UPI transaction reference.
 * @property payerName Name of the payer.
 * @property payerBankName Bank of the paying account.
 * @property payerAccountLast4Digits Last four digits of the paying account number.
 * @property payerUpiAppName UPI app used by the payer.
 * @property payerUpiId UPI ID of the payer.
 * @property upiAppName UPI app that processed the payment.
 * @property mifosTransactionId Mifos transaction reference.
 * @property transactionDate Formatted date of the transaction.
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
