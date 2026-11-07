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
import kpt.core.model.account.Account
/**
 * Detailed view of a savings account including summary and transactions.
 *
 * @property id Unique identifier.
 * @property accountNo Account number.
 * @property depositType Deposit account type.
 * @property clientId Identifier of the client.
 * @property clientName Name of the client.
 * @property savingsProductId Identifier of the savings product.
 * @property savingsProductName Name of the savings product.
 * @property fieldOfficerId Identifier of the assigned field officer.
 * @property status Current status.
 * @property timeline Lifecycle dates of the account.
 * @property currency Currency of the amounts.
 * @property nominalAnnualInterestRate Nominal annual interest rate.
 * @property withdrawalFeeForTransfers Whether a withdrawal fee applies to transfers.
 * @property allowOverdraft Whether overdraft is allowed.
 * @property enforceMinRequiredBalance Whether the minimum required balance is enforced.
 * @property lienAllowed Whether a lien can be placed on the account.
 * @property withHoldTax Whether withholding tax applies.
 * @property lastActiveTransactionDate Date of the last active transaction as year, month and day components.
 * @property isDormancyTrackingActive Whether dormancy tracking is active.
 * @property summary Balance summary of the account.
 * @property transactions Transactions of the account.
 */
@Serializable
data class SavingAccountDetail(
    val id: Long,
    val accountNo: String,
    val depositType: DepositType,
    val clientId: Long,
    val clientName: String,
    val savingsProductId: Long,
    val savingsProductName: String,
    val fieldOfficerId: Long,
    val status: Status,
    val timeline: Timeline,
    val currency: Currency,
    val nominalAnnualInterestRate: Double,
    val withdrawalFeeForTransfers: Boolean,
    val allowOverdraft: Boolean,
    val enforceMinRequiredBalance: Boolean,
    val lienAllowed: Boolean,
    val withHoldTax: Boolean,
    val lastActiveTransactionDate: List<Long>,
    val isDormancyTrackingActive: Boolean,
    val summary: Summary,
    val transactions: List<Transaction>,
)

/** Converts this detail into the lightweight [Account] model used by the UI. */
fun SavingAccountDetail.toAccount(): Account {
    return Account(
        name = savingsProductName,
        number = accountNo,
        balance = summary.accountBalance,
        id = id,
        productId = savingsProductId,
        currency = currency,
        status = status,
        clientName = clientName,
    )
}
