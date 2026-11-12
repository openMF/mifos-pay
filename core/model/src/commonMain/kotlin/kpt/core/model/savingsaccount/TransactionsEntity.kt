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
 * A savings transaction as returned with a savings account association.
 *
 * @property id Identifier of the transaction.
 * @property transactionType Type of the transaction.
 * @property entryType Ledger entry type (for example debit or credit).
 * @property accountId Identifier of the account.
 * @property accountNo Account number.
 * @property date Transaction date as year, month and day components.
 * @property currency Currency of the transaction.
 * @property amount Amount.
 * @property runningBalance Account balance after this transaction.
 * @property reversed Whether the transaction has been reversed.
 * @property transfer Related transfer, if any.
 * @property submittedOnDate Date the transaction was submitted as year, month and day components.
 * @property interestedPostedAsOn Whether interest was posted as on this date.
 * @property submittedByUsername Username of the user who submitted the transaction.
 * @property isManualTransaction Whether the transaction was entered manually.
 * @property isReversal Whether this transaction is a reversal.
 * @property originalTransactionId Identifier of the transaction this one reverses or derives from.
 * @property lienTransaction Whether this is a lien transaction.
 * @property releaseTransactionId Identifier of the transaction that released the lien.
 * @property paymentDetailData Payment details, if any.
 */
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
    /**
     * Transaction type with a flag for each kind of savings transaction.
     *
     * @property id Transaction type id.
     * @property code Transaction type code.
     * @property value Human-readable transaction type label.
     * @property deposit Deposit.
     * @property dividendPayout Dividend payout.
     * @property withdrawal Withdrawal.
     * @property interestPosting Interest posting.
     * @property feeDeduction Fee deduction.
     * @property initiateTransfer Transfer initiation.
     * @property approveTransfer Transfer approval.
     * @property withdrawTransfer Transfer withdrawal.
     * @property rejectTransfer Transfer rejection.
     * @property overdraftInterest Overdraft interest.
     * @property writtenoff Write-off.
     * @property overdraftFee Overdraft fee.
     * @property withholdTax Withholding tax.
     * @property escheat Account has escheated.
     * @property amountHold Amount hold.
     * @property amountRelease Amount release.
     */
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
