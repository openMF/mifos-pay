/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savingsaccount

import kotlinx.serialization.Serializable

@Serializable
data class TransactionsEntity(
    val id: Long,
    val transactionType: TransactionType,
    val entryType: String? = null,
    val accountId: Long,
    val accountNo: String,
    val date: List<Int>,
    val currency: Currency,
    val amount: Double,
    val runningBalance: Double,
    val reversed: Boolean,
    val transfer: Transfer? = null,
    val submittedOnDate: List<Int>,
    val interestedPostedAsOn: Boolean,
    val submittedByUsername: String,
    val isManualTransaction: Boolean,
    val isReversal: Boolean,
    val originalTransactionId: Long,
    val lienTransaction: Boolean,
    val releaseTransactionId: Long,
    val paymentDetailData: PaymentDetailData? = null,
) {
    @Serializable
    data class TransactionType(
        val id: Long,
        val code: String,
        val value: String,
        val deposit: Boolean,
        val dividendPayout: Boolean,
        val withdrawal: Boolean,
        val interestPosting: Boolean,
        val feeDeduction: Boolean,
        val initiateTransfer: Boolean,
        val approveTransfer: Boolean,
        val withdrawTransfer: Boolean,
        val rejectTransfer: Boolean,
        val overdraftInterest: Boolean,
        val writtenoff: Boolean,
        val overdraftFee: Boolean,
        val withholdTax: Boolean,
        val escheat: Boolean,
        val amountHold: Boolean,
        val amountRelease: Boolean,
    )
}
