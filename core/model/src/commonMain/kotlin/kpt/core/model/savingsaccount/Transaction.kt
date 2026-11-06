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
data class Transaction(
    val reversed: Boolean,
    val accountId: Long,
    val amount: Double,
    val date: String,
    val currency: Currency,
    val transactionType: TransactionType,
    val transactionId: Long,
    val accountNo: String,
    val transferId: Long?,
    val originalTransactionId: Long,
    val paymentDetailId: Long?,
    val description: String = "",
    val transfer: Transfer? = null,
    val paymentDetailData: PaymentDetailData? = null,
) {
    @Serializable
    data class Type(
        val id: Long,
        val code: String,
        val value: String,
    )

    @Serializable
    data class Transfer(
        val id: Long,
        val transferAmount: Double,
        val transferDescription: String,
        val reversed: Boolean,
    )

    @Serializable
    data class PaymentDetailData(
        val id: Long,
        val paymentType: PaymentType?,
        val accountNumber: String,
        val checkNumber: String,
        val routingCode: String,
        val receiptNumber: String,
        val bankNumber: String,
    )

    @Serializable
    data class PaymentType(
        val id: Int,
        val name: String,
        val isSystemDefined: Boolean,
    )
}
