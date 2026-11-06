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
