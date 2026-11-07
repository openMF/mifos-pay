/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Domain model of the loan template used in the loan application flow.
 *
 * @property clientId Identifier of the client.
 * @property clientAccountNo Account number of the client.
 * @property clientName Name of the client.
 * @property clientOfficeId Identifier of the office the client belongs to.
 * @property loanProductName Name of the loan product.
 * @property loanProductLinkedToFloatingRate Whether the loan product is linked to a floating rate.
 * @property fundId Identifier of the fund.
 * @property fundName Name of the fund.
 * @property currency Currency of the amount.
 * @property principal Principal amount of the loan.
 * @property approvedPrincipal Principal amount approved for the loan.
 * @property proposedPrincipal Principal amount proposed in the application.
 * @property termFrequency Loan term length, expressed in units of the term period frequency type.
 * @property termPeriodFrequencyType Term period frequency type.
 * @property numberOfRepayments Number of repayments for the loan.
 * @property repaymentEvery Number of frequency units between repayments.
 * @property repaymentFrequencyType Repayment frequency type.
 * @property interestRatePerPeriod Interest rate applied per period.
 * @property interestRateFrequencyType Interest rate frequency type.
 * @property annualInterestRate Annual interest rate.
 * @property floatingInterestRate Whether the loan uses a floating interest rate.
 * @property amortizationType Amortization type.
 * @property interestType Interest type.
 * @property interestCalculationPeriodType Interest calculation period type.
 * @property allowPartialPeriodInterestCalculation Whether interest is calculated for partial periods.
 * @property transactionProcessingStrategyId Identifier of the repayment transaction processing strategy.
 * @property transactionProcessingStrategyCode Code of the repayment transaction processing strategy.
 * @property graceOnArrearsAgeing Grace days before arrears ageing starts.
 * @property timeline Key dates of the loan.
 * @property productOptions Available product options.
 * @property loanOfficerOptions Available loan officer options.
 * @property loanPurposeOptions Available loan purpose options.
 * @property fundOptions Available fund options.
 * @property termFrequencyTypeOptions Available term frequency type options.
 * @property repaymentFrequencyTypeOptions Available repayment frequency type options.
 * @property repaymentFrequencyNthDayTypeOptions Available repayment frequency nth day type options.
 * @property repaymentFrequencyDaysOfWeekTypeOptions Available repayment frequency days of week type options.
 * @property interestRateFrequencyTypeOptions Available interest rate frequency type options.
 * @property amortizationTypeOptions Available amortization type options.
 * @property interestTypeOptions Available interest type options.
 * @property interestCalculationPeriodTypeOptions Available interest calculation period type options.
 * @property transactionProcessingStrategyOptions Available transaction processing strategy options.
 * @property chargeOptions Available charge options.
 * @property loanCollateralOptions Available loan collateral options.
 * @property multiDisburseLoan Whether the loan can be disbursed in multiple tranches.
 * @property canDefineInstallmentAmount Whether the installment amount can be defined.
 * @property canDisburse Whether the loan can be disbursed.
 * @property product Loan product.
 * @property daysInMonthType Days in month type.
 * @property daysInYearType Days in year type.
 * @property interestRecalculationEnabled Whether interest recalculation is enabled.
 * @property valiableInstallmentsAllowed Whether variable installments are allowed.
 * @property minimumGap Minimum gap between installments.
 * @property maximumGap Maximum gap between installments.
 * @property accountLinkingOptions Available account linking options.
 */
@Serializable
data class LoanTemplate(

    val clientId: Int? = null,

    val clientAccountNo: String? = null,

    val clientName: String? = null,

    val clientOfficeId: Int? = null,

    val loanProductName: String? = null,

    @SerialName("isLoanProductLinkedToFloatingRate")
    val loanProductLinkedToFloatingRate: Boolean? = null,

    val fundId: Int? = null,

    val fundName: String? = null,

    val currency: Currency? = null,

    val principal: Double? = null,

    val approvedPrincipal: Double? = null,

    val proposedPrincipal: Double? = null,

    val termFrequency: Int? = null,

    val termPeriodFrequencyType: TermPeriodFrequencyType? = null,

    val numberOfRepayments: Int? = null,

    val repaymentEvery: Int? = null,

    val repaymentFrequencyType: RepaymentFrequencyType? = null,

    val interestRatePerPeriod: Double? = null,

    val interestRateFrequencyType: InterestRateFrequencyType? = null,

    val annualInterestRate: Double? = null,

    @SerialName("isFloatingInterestRate")
    val floatingInterestRate: Boolean? = null,

    val amortizationType: AmortizationType? = null,

    val interestType: InterestType? = null,

    val interestCalculationPeriodType: InterestCalculationPeriodType? = null,

    val allowPartialPeriodInterestCalculation: Boolean? = null,

    val transactionProcessingStrategyId: Int? = null,

    val transactionProcessingStrategyCode: String? = null,

    val graceOnArrearsAgeing: Int? = null,

    val timeline: Timeline? = null,

    val productOptions: List<ProductOptions> = emptyList(),

    val loanOfficerOptions: List<LoanOfficerOptions> = emptyList(),

    val loanPurposeOptions: List<LoanPurposeOptions> = emptyList(),

    val fundOptions: List<FundOptions> = emptyList(),

    val termFrequencyTypeOptions: List<TermFrequencyTypeOptions> = emptyList(),

    val repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = emptyList(),

    val repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = emptyList(),

    val repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = emptyList(),

    val interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = emptyList(),

    val amortizationTypeOptions: List<AmortizationTypeOptions> = emptyList(),

    val interestTypeOptions: List<InterestTypeOptions> = emptyList(),

    val interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType> = emptyList(),

    val transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions> = emptyList(),

    val chargeOptions: List<ChargeOptions> = emptyList(),

    val loanCollateralOptions: List<LoanCollateralOptions> = emptyList(),

    val multiDisburseLoan: Boolean? = null,

    val canDefineInstallmentAmount: Boolean? = null,

    val canDisburse: Boolean? = null,

    val product: Product? = null,

    val daysInMonthType: DaysInMonthType? = null,

    val daysInYearType: DaysInYearType? = null,

    @SerialName("isInterestRecalculationEnabled")
    val interestRecalculationEnabled: Boolean? = null,

    @SerialName("isvaliableInstallmentsAllowed")
    val valiableInstallmentsAllowed: Boolean? = null,

    val minimumGap: Int? = null,

    val maximumGap: Int? = null,

    val accountLinkingOptions: List<AccountLinkingOptions> = emptyList(),

) {
    /**
     * Required to set default value to the Fund spinner
     *
     * @param fundId The value received from the Template for that particular loanProduct
     * @return Returns the index of the fundOption list where the specified fundId is located
     */
    fun getFundNameFromId(fundId: Int): Int {
        for (i in fundOptions.indices) {
            if (fundOptions[i].id == fundId) {
                return i
            }
        }
        return 0
    }
}
