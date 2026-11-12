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
 * A savings account loaded together with its transactions and interest settings.
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
 * @property subStatus Sub-status of the account.
 * @property timeline Lifecycle dates of the account.
 * @property currency Currency of the amounts.
 * @property nominalAnnualInterestRate Nominal annual interest rate.
 * @property interestCompoundingPeriodType Interest compounding period.
 * @property interestPostingPeriodType Interest posting period.
 * @property interestCalculationType Interest calculation method.
 * @property interestCalculationDaysInYearType Days-in-year basis for interest calculation.
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
data class SavingsWithAssociationsEntity(
    val id: Long,
    val accountNo: String,
    val depositType: DepositType,
    val clientId: Long,
    val clientName: String,
    val savingsProductId: Long,
    val savingsProductName: String,
    val fieldOfficerId: Long,
    val status: Status,
    val subStatus: SubStatus,
    val timeline: Timeline,
    val currency: Currency,
    val nominalAnnualInterestRate: Double,
    val interestCompoundingPeriodType: InterestPeriod = InterestPeriod(),
    val interestPostingPeriodType: InterestPeriod = InterestPeriod(),
    val interestCalculationType: InterestPeriod = InterestPeriod(),
    val interestCalculationDaysInYearType: InterestPeriod = InterestPeriod(),
    val withdrawalFeeForTransfers: Boolean,
    val allowOverdraft: Boolean,
    val enforceMinRequiredBalance: Boolean,
    val lienAllowed: Boolean,
    val withHoldTax: Boolean,
    val lastActiveTransactionDate: List<Long> = emptyList(),
    val isDormancyTrackingActive: Boolean,
    val summary: Summary,
    val transactions: List<TransactionsEntity> = emptyList(),
)
