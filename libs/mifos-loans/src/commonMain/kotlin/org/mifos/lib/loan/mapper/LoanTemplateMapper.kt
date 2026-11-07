/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("TooManyFunctions")

package org.mifos.lib.loan.mapper

import org.mifos.lib.loan.core.model.AccountLinkingOptions
import org.mifos.lib.loan.core.model.AllowAttributeOverrides
import org.mifos.lib.loan.core.model.FundOptions
import org.mifos.lib.loan.core.model.LoanCollateralOptions
import org.mifos.lib.loan.core.model.LoanOfficerOptions
import org.mifos.lib.loan.core.model.LoanPurposeOptions
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.core.model.Product
import org.mifos.lib.loan.core.model.ProductOptions
import org.mifos.lib.loan.core.model.Timeline
import org.mifos.lib.loan.network.model.AccountLinkingOptionsResponseDto
import org.mifos.lib.loan.network.model.AllowAttributesOverridesResponseDto
import org.mifos.lib.loan.network.model.FundOptionsResponseDto
import org.mifos.lib.loan.network.model.LoanCollateralOptionsResponseDto
import org.mifos.lib.loan.network.model.LoanOfficerOptionsResponseDto
import org.mifos.lib.loan.network.model.LoanProductOptionsResponseDto
import org.mifos.lib.loan.network.model.LoanProductResponseDto
import org.mifos.lib.loan.network.model.LoanPurposeOptionsResponseDto
import org.mifos.lib.loan.network.model.LoanTemplateResponseDto
import org.mifos.lib.loan.network.model.LoanTimelineResponseDto

/** Maps this LoanTemplateResponseDto to the [LoanTemplate] domain model. */
fun LoanTemplateResponseDto.toModel(): LoanTemplate =
    LoanTemplate(
        clientId = clientId,
        clientAccountNo = clientAccountNo,
        clientName = clientName,
        clientOfficeId = clientOfficeId,
        loanProductName = loanProductName,
        loanProductLinkedToFloatingRate = loanProductLinkedToFloatingRate,
        fundId = fundId,
        fundName = fundName,
        currency = currency?.toModel(),
        principal = principal,
        approvedPrincipal = approvedPrincipal,
        proposedPrincipal = proposedPrincipal,
        termFrequency = termFrequency,
        termPeriodFrequencyType = termPeriodFrequencyType?.toTermPeriodFrequencyType(),
        numberOfRepayments = numberOfRepayments,
        repaymentEvery = repaymentEvery,
        repaymentFrequencyType = repaymentFrequencyType?.toRepaymentFrequencyType(),
        interestRatePerPeriod = interestRatePerPeriod,
        interestRateFrequencyType = interestRateFrequencyType?.toInterestRateFrequencyType(),
        annualInterestRate = annualInterestRate,
        floatingInterestRate = floatingInterestRate,
        amortizationType = amortizationType?.toAmortizationType(),
        interestType = interestType?.toInterestType(),
        interestCalculationPeriodType =
        interestCalculationPeriodType?.toInterestCalculationPeriodType(),
        allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation,
        transactionProcessingStrategyId = transactionProcessingStrategyId,
        transactionProcessingStrategyCode = transactionProcessingStrategyCode,
        graceOnArrearsAgeing = graceOnArrearsAgeing,
        timeline = timeline?.toModel(),

        productOptions = productOptions.map { it.toModel() },
        loanOfficerOptions = loanOfficerOptions.map { it.toModel() },
        loanPurposeOptions = loanPurposeOptions.map { it.toModel() },
        fundOptions = fundOptions.map { it.toModel() },

        termFrequencyTypeOptions = termFrequencyTypeOptions.map { it.toTermFrequencyTypeOptions() },

        repaymentFrequencyTypeOptions =
        repaymentFrequencyTypeOptions.map { it.toRepaymentFrequencyTypeOptions() },

        repaymentFrequencyNthDayTypeOptions =
        repaymentFrequencyNthDayTypeOptions.map {
            it.toRepaymentFrequencyNthDayTypeOptions()
        },

        repaymentFrequencyDaysOfWeekTypeOptions =
        repaymentFrequencyDaysOfWeekTypeOptions.map {
            it.toRepaymentFrequencyDaysOfWeekTypeOptions()
        },

        interestRateFrequencyTypeOptions =
        interestRateFrequencyTypeOptions.map { it.toInterestRateFrequencyTypeOptions() },

        amortizationTypeOptions = amortizationTypeOptions.map { it.toAmortizationTypeOptions() },

        interestTypeOptions = interestTypeOptions.map { it.toInterestTypeOptions() },

        interestCalculationPeriodTypeOptions =
        interestCalculationPeriodTypeOptions.map { it.toInterestCalculationPeriodType() },

        transactionProcessingStrategyOptions =
        transactionProcessingStrategyOptions.map { it.toTransactionProcessingStrategyOptions() },

        chargeOptions = chargeOptions.map { it.toModel() },
        loanCollateralOptions = loanCollateralOptions.map { it.toModel() },

        multiDisburseLoan = multiDisburseLoan,
        canDefineInstallmentAmount = canDefineInstallmentAmount,
        canDisburse = canDisburse,
        product = product?.toModel(),

        daysInMonthType = daysInMonthType?.toDaysInMonthType(),
        daysInYearType = daysInYearType?.toDaysInYearType(),

        interestRecalculationEnabled = interestRecalculationEnabled,
        valiableInstallmentsAllowed = valiableInstallmentsAllowed,
        minimumGap = minimumGap,
        maximumGap = maximumGap,

        accountLinkingOptions = accountLinkingOptions.map { it.toModel() },
    )

/** Maps this LoanTimelineResponseDto to the [Timeline] domain model. */
fun LoanTimelineResponseDto.toModel(): Timeline =
    Timeline(
        expectedDisbursementDate = expectedDisbursementDate,
    )

/** Maps this LoanProductOptionsResponseDto to the [ProductOptions] domain model. */
fun LoanProductOptionsResponseDto.toModel(): ProductOptions =
    ProductOptions(
        id = id,
        name = name,
        includeInBorrowerCycle = includeInBorrowerCycle,
        useBorrowerCycle = useBorrowerCycle,
        linkedToFloatingInterestRates = linkedToFloatingInterestRates,
        floatingInterestRateCalculationAllowed = floatingInterestRateCalculationAllowed,
        allowvaliableInstallments = allowvaliableInstallments,
        interestRecalculationEnabled = interestRecalculationEnabled,
        canDefineInstallmentAmount = canDefineInstallmentAmount,
        holdGuaranteeFunds = holdGuaranteeFunds,
        accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion,
    )

/** Maps this LoanOfficerOptionsResponseDto to the [LoanOfficerOptions] domain model. */
fun LoanOfficerOptionsResponseDto.toModel(): LoanOfficerOptions =
    LoanOfficerOptions(
        id = id,
        firstname = firstname,
        lastname = lastname,
        displayName = displayName,
        mobileNo = mobileNo,
        officeId = officeId,
        officeName = officeName,
        loanOfficer = loanOfficer,
        active = active,
        joiningDate = joiningDate,
    )

/** Maps this LoanPurposeOptionsResponseDto to the [LoanPurposeOptions] domain model. */
fun LoanPurposeOptionsResponseDto.toModel(): LoanPurposeOptions =
    LoanPurposeOptions(
        id = id,
        name = name,
        position = position,
        description = description,
        active = active,
    )

/** Maps this FundOptionsResponseDto to the [FundOptions] domain model. */
fun FundOptionsResponseDto.toModel(): FundOptions =
    FundOptions(
        id = id,
        name = name,
    )

/** Maps this LoanCollateralOptionsResponseDto to the [LoanCollateralOptions] domain model. */
fun LoanCollateralOptionsResponseDto.toModel(): LoanCollateralOptions =
    LoanCollateralOptions(
        id = id,
        name = name,
        position = position,
        description = description,
        active = active,
    )

/** Maps this LoanProductResponseDto to the [Product] domain model. */
fun LoanProductResponseDto.toModel(): Product =
    Product(
        id = id,
        name = name,
        shortName = shortName,
        fundId = fundId,
        fundName = fundName,
        includeInBorrowerCycle = includeInBorrowerCycle,
        useBorrowerCycle = useBorrowerCycle,
        startDate = startDate,
        status = status,
        currency = currency?.toModel(),
        principal = principal,
        minPrincipal = minPrincipal,
        maxPrincipal = maxPrincipal,
        numberOfRepayments = numberOfRepayments,
        minNumberOfRepayments = minNumberOfRepayments,
        maxNumberOfRepayments = maxNumberOfRepayments,
        repaymentEvery = repaymentEvery,
        repaymentFrequencyType = repaymentFrequencyType?.toRepaymentFrequencyType(),
        interestRatePerPeriod = interestRatePerPeriod,
        minInterestRatePerPeriod = minInterestRatePerPeriod,
        maxInterestRatePerPeriod = maxInterestRatePerPeriod,
        interestRateFrequencyType = interestRateFrequencyType?.toInterestRateFrequencyType(),
        annualInterestRate = annualInterestRate,
        linkedToFloatingInterestRates = linkedToFloatingInterestRates,
        floatingInterestRateCalculationAllowed = floatingInterestRateCalculationAllowed,
        allowvaliableInstallments = allowvaliableInstallments,
        minimumGap = minimumGap,
        maximumGap = maximumGap,
        amortizationType = amortizationType.toAmortizationType(),
        interestType = interestType.toInterestType(),
        interestCalculationPeriodType =
        interestCalculationPeriodType?.toInterestCalculationPeriodType(),
        allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation,
        transactionProcessingStrategyId = transactionProcessingStrategyId,
        transactionProcessingStrategyName = transactionProcessingStrategyName,
        graceOnArrearsAgeing = graceOnArrearsAgeing,
        overdueDaysForNPA = overdueDaysForNPA,
        daysInMonthType = daysInMonthType?.toDaysInMonthType(),
        daysInYearType = daysInYearType.toDaysInYearType(),
        interestRecalculationEnabled = interestRecalculationEnabled,
        interestRecalculationData = interestRecalculationData?.toModel(),
        canDefineInstallmentAmount = canDefineInstallmentAmount,
        accountingRule = accountingRule?.toAccountingRule(),
        multiDisburseLoan = multiDisburseLoan,
        maxTrancheCount = maxTrancheCount,
        principalThresholdForLastInstallment = principalThresholdForLastInstallment,
        holdGuaranteeFunds = holdGuaranteeFunds,
        accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion,
        allowAttributeOverrides = allowAttributeOverrides?.toModel(),
    )

/** Maps this AccountLinkingOptionsResponseDto to the [AccountLinkingOptions] domain model. */
fun AccountLinkingOptionsResponseDto.toModel(): AccountLinkingOptions =
    AccountLinkingOptions(
        accountNo = accountNo,
        clientId = clientId,
        clientName = clientName,
        currency = currency?.toModel(),
        fieldOfficerId = fieldOfficerId,
        id = id,
        productId = productId,
        productName = productName,
    )

/** Maps this AllowAttributesOverridesResponseDto to the [AllowAttributeOverrides] domain model. */
fun AllowAttributesOverridesResponseDto.toModel(): AllowAttributeOverrides =
    AllowAttributeOverrides(
        amortizationType = amortizationType,
        interestType = interestType,
        transactionProcessingStrategyId = transactionProcessingStrategyId,
        interestCalculationPeriodType = interestCalculationPeriodType,
        inArrearsTolerance = inArrearsTolerance,
        repaymentEvery = repaymentEvery,
        graceOnPrincipalAndInterestPayment = graceOnPrincipalAndInterestPayment,
        graceOnArrearsAgeing = graceOnArrearsAgeing,
    )
