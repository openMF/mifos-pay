/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network response DTO for the loan product, as returned by the Fineract loans API.
 *
 * @property id Unique identifier.
 * @property name Name.
 * @property shortName Short name.
 * @property fundId Identifier of the fund.
 * @property fundName Name of the fund.
 * @property includeInBorrowerCycle Whether the loan counts toward the borrower cycle.
 * @property useBorrowerCycle Whether borrower-cycle based limits are used.
 * @property startDate Start date.
 * @property status Status.
 * @property currency Currency of the amount.
 * @property principal Principal amount of the loan.
 * @property minPrincipal Minimum principal allowed for the product.
 * @property maxPrincipal Maximum principal allowed for the product.
 * @property numberOfRepayments Number of repayments for the loan.
 * @property minNumberOfRepayments Minimum number of repayments allowed.
 * @property maxNumberOfRepayments Maximum number of repayments allowed.
 * @property repaymentEvery Number of frequency units between repayments.
 * @property repaymentFrequencyType Repayment frequency type.
 * @property interestRatePerPeriod Interest rate applied per period.
 * @property minInterestRatePerPeriod Minimum interest rate per period allowed.
 * @property maxInterestRatePerPeriod Maximum interest rate per period allowed.
 * @property interestRateFrequencyType Interest rate frequency type.
 * @property annualInterestRate Annual interest rate.
 * @property linkedToFloatingInterestRates Whether the product is linked to floating interest rates.
 * @property floatingInterestRateCalculationAllowed Whether floating interest rate calculation is allowed.
 * @property allowvaliableInstallments Whether variable installments are allowed.
 * @property minimumGap Minimum gap between installments.
 * @property maximumGap Maximum gap between installments.
 * @property amortizationType Amortization type.
 * @property interestType Interest type.
 * @property interestCalculationPeriodType Interest calculation period type.
 * @property allowPartialPeriodInterestCalculation Whether interest is calculated for partial periods.
 * @property transactionProcessingStrategyId Identifier of the repayment transaction processing strategy.
 * @property transactionProcessingStrategyName Name of the repayment transaction processing strategy.
 * @property graceOnArrearsAgeing Grace days before arrears ageing starts.
 * @property overdueDaysForNPA Overdue days after which the loan becomes a non-performing asset.
 * @property daysInMonthType Days in month type.
 * @property daysInYearType Days in year type.
 * @property interestRecalculationEnabled Whether interest recalculation is enabled.
 * @property interestRecalculationData Interest recalculation data.
 * @property canDefineInstallmentAmount Whether the installment amount can be defined.
 * @property accountingRule Accounting rule.
 * @property multiDisburseLoan Whether the loan can be disbursed in multiple tranches.
 * @property maxTrancheCount Maximum number of disbursement tranches.
 * @property principalThresholdForLastInstallment Percentage threshold of principal used to merge the last installment.
 * @property holdGuaranteeFunds Whether guarantee funds are held for the loan.
 * @property accountMovesOutOfNPAOnlyOnArrearsCompletion Whether NPA status ends only after arrears clear.
 * @property allowAttributeOverrides Allow attribute overrides.
 */
@Serializable
data class LoanProductResponseDto(

    val id: Int? = null,

    val name: String? = null,

    val shortName: String? = null,

    val fundId: Int? = null,

    val fundName: String? = null,

    val includeInBorrowerCycle: Boolean? = null,

    val useBorrowerCycle: Boolean? = null,

    val startDate: List<Int>? = null,

    val status: String? = null,

    val currency: CurrencyResponseDto? = null,

    val principal: Double? = null,

    val minPrincipal: Double? = null,

    val maxPrincipal: Double? = null,

    val numberOfRepayments: Int? = null,

    val minNumberOfRepayments: Int? = null,

    val maxNumberOfRepayments: Int? = null,

    val repaymentEvery: Int? = null,

    val repaymentFrequencyType: TypeResponseDto? = null,

    val interestRatePerPeriod: Double? = null,

    val minInterestRatePerPeriod: Double? = null,

    val maxInterestRatePerPeriod: Double? = null,

    val interestRateFrequencyType: TypeResponseDto? = null,

    val annualInterestRate: Double? = null,

    @SerialName("isLinkedToFloatingInterestRates")
    val linkedToFloatingInterestRates: Boolean? = null,

    @SerialName("isFloatingInterestRateCalculationAllowed")
    val floatingInterestRateCalculationAllowed: Boolean? = null,

    val allowvaliableInstallments: Boolean? = null,

    val minimumGap: Double? = null,

    val maximumGap: Double? = null,

    val amortizationType: TypeResponseDto,

    val interestType: TypeResponseDto,

    val interestCalculationPeriodType: TypeResponseDto? = null,

    val allowPartialPeriodInterestCalculation: Boolean? = null,

    val transactionProcessingStrategyId: Int? = null,

    val transactionProcessingStrategyName: String? = null,

    val graceOnArrearsAgeing: Int? = null,

    val overdueDaysForNPA: Int? = null,

    val daysInMonthType: TypeResponseDto? = null,

    val daysInYearType: TypeResponseDto,

    @SerialName("isInterestRecalculationEnabled")
    val interestRecalculationEnabled: Boolean? = null,

    val interestRecalculationData: InterestRecalculationDataResponseDto? = null,

    val canDefineInstallmentAmount: Boolean? = null,

    val accountingRule: TypeResponseDto? = null,

    val multiDisburseLoan: Boolean? = null,

    val maxTrancheCount: Int? = null,

    val principalThresholdForLastInstallment: Double? = null,

    val holdGuaranteeFunds: Boolean? = null,

    val accountMovesOutOfNPAOnlyOnArrearsCompletion: Boolean? = null,

    val allowAttributeOverrides: AllowAttributesOverridesResponseDto? = null,

)
