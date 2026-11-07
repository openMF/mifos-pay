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

/**
 * A savings account transaction.
 *
 * @property reversed Whether the transaction has been reversed.
 * @property accountId Identifier of the account.
 * @property amount Amount.
 * @property date Transaction date as a string.
 * @property currency Currency of the transaction.
 * @property transactionType Type of the transaction.
 * @property transactionId Identifier of the transaction.
 * @property accountNo Account number.
 * @property transferId Identifier of the related transfer, if any.
 * @property originalTransactionId Identifier of the transaction this one reverses or derives from.
 * @property paymentDetailId Identifier of the payment details, if any.
 * @property description Description.
 * @property transfer Related transfer, if any.
 * @property paymentDetailData Payment details, if any.
 */
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
    /**
     * Transaction type reference (id, code and label).
     *
     * @property id Unique identifier.
     * @property code Code identifying the item.
     * @property value Human-readable label.
     */
    @Serializable
    data class Type(
        val id: Long,
        val code: String,
        val value: String,
    )

    /**
     * Transfer information attached to a transaction.
     *
     * @property id Unique identifier.
     * @property transferAmount Amount transferred.
     * @property transferDescription Description of the transfer.
     * @property reversed Whether the transfer has been reversed.
     */
    @Serializable
    data class Transfer(
        val id: Long,
        val transferAmount: Double,
        val transferDescription: String,
        val reversed: Boolean,
    )

    /**
     * Payment details attached to a transaction.
     *
     * @property id Unique identifier.
     * @property paymentType Payment type used.
     * @property accountNumber Account number.
     * @property checkNumber Cheque number.
     * @property routingCode Bank routing code.
     * @property receiptNumber Receipt number.
     * @property bankNumber Bank number.
     */
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

    /**
     * Payment type reference used in transaction payment details.
     *
     * @property id Payment type id.
     * @property name Payment type name.
     * @property isSystemDefined Whether the type is predefined by the system.
     */
    @Serializable
    data class PaymentType(
        val id: Int,
        val name: String,
        val isSystemDefined: Boolean,
    )
}
