/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.mapper

import kpt.core.common.DateHelper
import kpt.core.model.savingsaccount.PaymentType
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.model.savingsaccount.TransactionsEntity
/** Extracts the transactions of a savings-with-associations entity as domain [Transaction]s. */
fun SavingsWithAssociationsEntity.toTransactionList(): List<Transaction> {
    return this.transactions.map { it.toModel() }
}

/** Maps a transactions network entity to a domain [Transaction]. */
fun TransactionsEntity.toModel(): Transaction {
    return Transaction(
        reversed = this.reversed,
        transactionId = this.id,
        amount = this.amount,
        date = DateHelper.getDateAsString(this.submittedOnDate),
        currency = this.currency,
        transactionType = when {
            this.transactionType.deposit -> TransactionType.CREDIT
            this.transactionType.withdrawal -> TransactionType.DEBIT
            else -> TransactionType.OTHER
        },
        transferId = this.transfer?.id,
        accountId = this.accountId,
        accountNo = this.accountNo,
        originalTransactionId = this.originalTransactionId,
        paymentDetailId = this.paymentDetailData?.id,
        description = this.transfer?.transferDescription ?: "",
        transfer = this.transfer?.let {
            Transaction.Transfer(
                id = it.id ?: 0L,
                transferAmount = it.transferAmount ?: 0.00,
                transferDescription = it.transferDescription ?: "",
                reversed = it.reversed ?: false,
            )
        },
        paymentDetailData = this.paymentDetailData?.let {
            Transaction.PaymentDetailData(
                id = it.id,
                paymentType = it.paymentType?.toModalUi(),
                accountNumber = it.accountNumber ?: "",
                checkNumber = it.checkNumber ?: "",
                routingCode = it.routingCode ?: "",
                receiptNumber = it.receiptNumber ?: "",
                bankNumber = it.bankNumber ?: "",
            )
        },
    )
}

/** Maps a network [PaymentType] to the domain [Transaction.PaymentType]. */
fun PaymentType.toModalUi(): Transaction.PaymentType {
    return Transaction.PaymentType(
        id = this.id ?: 0,
        name = this.name ?: "",
        isSystemDefined = this.isSystemDefined ?: false,
    )
}
