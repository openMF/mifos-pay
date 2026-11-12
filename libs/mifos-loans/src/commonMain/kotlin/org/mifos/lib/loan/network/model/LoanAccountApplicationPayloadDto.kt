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

import kotlinx.serialization.Serializable

/**
 * Request payload DTO sent to the Fineract API when submitting a loan application.
 *
 * @property clientId Identifier of the client.
 * @property productId Identifier of the loan product.
 * @property productName Name of the loan product.
 * @property principal Principal amount of the loan.
 * @property loanTermFrequency Loan term length, expressed in units of the term frequency type.
 * @property loanTermFrequencyType Identifier of the unit used for the loan term.
 * @property loanType Type of loan being applied for.
 * @property numberOfRepayments Number of repayments for the loan.
 * @property repaymentEvery Number of frequency units between repayments.
 * @property repaymentFrequencyType Repayment frequency type.
 * @property interestRatePerPeriod Interest rate applied per period.
 * @property amortizationType Amortization type.
 * @property interestType Interest type.
 * @property interestCalculationPeriodType Interest calculation period type.
 * @property transactionProcessingStrategyId Identifier of the repayment transaction processing strategy.
 * @property transactionProcessingStrategyCode Code of the repayment transaction processing strategy.
 * @property expectedDisbursementDate Expected disbursement date as year, month and day components.
 * @property submittedOnDate Date the application was submitted.
 * @property linkAccountId Identifier of the linked savings account.
 * @property loanPurposeId Identifier of the selected loan purpose.
 * @property loanPurpose Selected loan purpose.
 * @property maxOutstandingLoanBalance Maximum outstanding balance allowed across tranches.
 * @property currency Currency of the amount.
 * @property dateFormat Date format pattern used for date fields.
 * @property locale Locale used to format numbers and dates.
 */
@Serializable
data class LoanAccountApplicationPayloadDto(

    val clientId: Int? = null,

    val productId: Int? = null,

    val productName: String? = null,

    val principal: Double? = null,

    val loanTermFrequency: Int? = null,

    val loanTermFrequencyType: Int? = null,

    val loanType: String? = null,

    val numberOfRepayments: Int? = null,

    val repaymentEvery: Int? = null,

    val repaymentFrequencyType: Int? = null,

    val interestRatePerPeriod: Double? = null,

    val amortizationType: Int? = null,

    val interestType: Int? = null,

    val interestCalculationPeriodType: Int? = null,

    val transactionProcessingStrategyId: Int? = null,

    val transactionProcessingStrategyCode: String? = null,

    val expectedDisbursementDate: String? = null,

    val submittedOnDate: String? = null,

    val linkAccountId: Int? = null,

    val loanPurposeId: Int? = null,

    val loanPurpose: String? = null,

    val maxOutstandingLoanBalance: Double? = null,

    val currency: String? = null,

    val dateFormat: String? = null,

    val locale: String? = null,
)
