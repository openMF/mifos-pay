/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.mapper

import org.mifos.lib.loan.core.model.LoansPayload
import org.mifos.lib.loan.network.model.LoanAccountApplicationPayloadDto

/** Maps this payload to the [LoanAccountApplicationPayloadDto] sent to the API. */
fun LoansPayload.toDto(): LoanAccountApplicationPayloadDto =
    LoanAccountApplicationPayloadDto(
        clientId = clientId,
        productId = productId,
        productName = productName,
        principal = principal,
        loanTermFrequency = loanTermFrequency,
        loanTermFrequencyType = loanTermFrequencyType,
        loanType = loanType,
        numberOfRepayments = numberOfRepayments,
        repaymentEvery = repaymentEvery,
        repaymentFrequencyType = repaymentFrequencyType,
        interestRatePerPeriod = interestRatePerPeriod,
        amortizationType = amortizationType,
        interestType = interestType,
        interestCalculationPeriodType = interestCalculationPeriodType,
        transactionProcessingStrategyId = transactionProcessingStrategyId,
        transactionProcessingStrategyCode = transactionProcessingStrategyCode,
        expectedDisbursementDate = expectedDisbursementDate,
        submittedOnDate = submittedOnDate,
        linkAccountId = linkAccountId,
        loanPurposeId = loanPurposeId,
        loanPurpose = loanPurpose,
        maxOutstandingLoanBalance = maxOutstandingLoanBalance,
        currency = currency,
        dateFormat = dateFormat,
        locale = locale,
    )
