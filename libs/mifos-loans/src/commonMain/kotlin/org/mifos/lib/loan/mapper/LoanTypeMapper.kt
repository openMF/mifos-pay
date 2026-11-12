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

import org.mifos.lib.loan.core.model.AccountingRule
import org.mifos.lib.loan.core.model.AmortizationType
import org.mifos.lib.loan.core.model.AmortizationTypeOptions
import org.mifos.lib.loan.core.model.ChargeAppliesTo
import org.mifos.lib.loan.core.model.ChargeCalculationType
import org.mifos.lib.loan.core.model.ChargePaymentMode
import org.mifos.lib.loan.core.model.ChargeTimeType
import org.mifos.lib.loan.core.model.DaysInMonthType
import org.mifos.lib.loan.core.model.DaysInYearType
import org.mifos.lib.loan.core.model.EntityType
import org.mifos.lib.loan.core.model.Frequency
import org.mifos.lib.loan.core.model.InterestCalculationPeriodType
import org.mifos.lib.loan.core.model.InterestRateFrequencyType
import org.mifos.lib.loan.core.model.InterestRateFrequencyTypeOptions
import org.mifos.lib.loan.core.model.InterestRecalculationCompoundingType
import org.mifos.lib.loan.core.model.InterestType
import org.mifos.lib.loan.core.model.InterestTypeOptions
import org.mifos.lib.loan.core.model.RecalculationCompoundingFrequencyType
import org.mifos.lib.loan.core.model.RecalculationRestFrequencyType
import org.mifos.lib.loan.core.model.RepaymentFrequencyDaysOfWeekTypeOptions
import org.mifos.lib.loan.core.model.RepaymentFrequencyNthDayTypeOptions
import org.mifos.lib.loan.core.model.RepaymentFrequencyType
import org.mifos.lib.loan.core.model.RepaymentFrequencyTypeOptions
import org.mifos.lib.loan.core.model.RepeatsOnNthDayOfMonth
import org.mifos.lib.loan.core.model.RescheduleStrategyType
import org.mifos.lib.loan.core.model.TermFrequencyTypeOptions
import org.mifos.lib.loan.core.model.TermPeriodFrequencyType
import org.mifos.lib.loan.core.model.TransactionProcessingStrategyOptions
import org.mifos.lib.loan.core.model.Type
import org.mifos.lib.loan.network.model.TypeResponseDto

/** Maps this type DTO to the term period frequency type domain value. */
fun TypeResponseDto.toTermPeriodFrequencyType() = TermPeriodFrequencyType(id, code, value)

/** Maps this type DTO to the repayment frequency type domain value. */
fun TypeResponseDto.toRepaymentFrequencyType() = RepaymentFrequencyType(id, code, value)

/** Maps this type DTO to the interest rate frequency type domain value. */
fun TypeResponseDto.toInterestRateFrequencyType() = InterestRateFrequencyType(id, code, value)

/** Maps this type DTO to the amortization type domain value. */
fun TypeResponseDto.toAmortizationType() = AmortizationType(id, code, value)

/** Maps this type DTO to the interest type domain value. */
fun TypeResponseDto.toInterestType() = InterestType(id, code, value)

/** Maps this type DTO to the interest calculation period type domain value. */
fun TypeResponseDto.toInterestCalculationPeriodType() =
    InterestCalculationPeriodType(id, code, value)

/** Maps this type DTO to the days in month type domain value. */
fun TypeResponseDto.toDaysInMonthType() = DaysInMonthType(id, code, value)

/** Maps this type DTO to the days in year type domain value. */
fun TypeResponseDto.toDaysInYearType() = DaysInYearType(id, code, value)

/** Maps this type DTO to the entity type domain value. */
fun TypeResponseDto.toEntityType() = EntityType(id, code, value)

/** Maps this type DTO to the type domain value. */
fun TypeResponseDto.toType() = Type(id, code, value)

/** Maps this type DTO to the frequency domain value. */
fun TypeResponseDto.toFrequency() = Frequency(id, code, value)

/** Maps this type DTO to the repeats on nth day of month domain value. */
fun TypeResponseDto.toRepeatsOnNthDayOfMonth() = RepeatsOnNthDayOfMonth(id, code, value)

/** Maps this type DTO to the interest recalculation compounding type domain value. */
fun TypeResponseDto.toInterestRecalculationCompoundingType() =
    InterestRecalculationCompoundingType(id, code, value)

/** Maps this type DTO to the reschedule strategy type domain value. */
fun TypeResponseDto.toRescheduleStrategyType() = RescheduleStrategyType(id, code, value)

/** Maps this type DTO to the recalculation rest frequency type domain value. */
fun TypeResponseDto.toRecalculationRestFrequencyType() =
    RecalculationRestFrequencyType(id, code, value)

/** Maps this type DTO to the recalculation compounding frequency type domain value. */
fun TypeResponseDto.toRecalculationCompoundingFrequencyType() =
    RecalculationCompoundingFrequencyType(id, code, value)

/** Maps this type DTO to the accounting rule domain value. */
fun TypeResponseDto.toAccountingRule() = AccountingRule(id, code, value)

/** Maps this type DTO to the charge applies to domain value. */
fun TypeResponseDto.toChargeAppliesTo() = ChargeAppliesTo(id, code, value)

/** Maps this type DTO to the charge payment mode domain value. */
fun TypeResponseDto.toChargePaymentMode() = ChargePaymentMode(id, code, value)

/** Maps this type DTO to the charge time type domain value. */
fun TypeResponseDto.toChargeTimeType() = ChargeTimeType(id = id ?: 0, code = code, value = value)

/** Maps this type DTO to the charge calculation type domain value. */
fun TypeResponseDto.toChargeCalculationType() =
    ChargeCalculationType(id = id ?: 0, code = code, value = value)

/** Maps this type DTO to the term frequency type options domain value. */
fun TypeResponseDto.toTermFrequencyTypeOptions() =
    TermFrequencyTypeOptions(id, code, value)

/** Maps this type DTO to the repayment frequency type options domain value. */
fun TypeResponseDto.toRepaymentFrequencyTypeOptions() =
    RepaymentFrequencyTypeOptions(id, code, value)

/** Maps this type DTO to the repayment frequency nth day type options domain value. */
fun TypeResponseDto.toRepaymentFrequencyNthDayTypeOptions() =
    RepaymentFrequencyNthDayTypeOptions(id, code, value)

/** Maps this type DTO to the repayment frequency days of week type options domain value. */
fun TypeResponseDto.toRepaymentFrequencyDaysOfWeekTypeOptions() =
    RepaymentFrequencyDaysOfWeekTypeOptions(id, code, value)

/** Maps this type DTO to the interest rate frequency type options domain value. */
fun TypeResponseDto.toInterestRateFrequencyTypeOptions() =
    InterestRateFrequencyTypeOptions(id, code, value)

/** Maps this type DTO to the amortization type options domain value. */
fun TypeResponseDto.toAmortizationTypeOptions() =
    AmortizationTypeOptions(id, code, value)

/** Maps this type DTO to the interest type options domain value. */
fun TypeResponseDto.toInterestTypeOptions() =
    InterestTypeOptions(id, code, value)

/** Maps this type DTO to the transaction processing strategy options domain value. */
fun TypeResponseDto.toTransactionProcessingStrategyOptions() =
    TransactionProcessingStrategyOptions(id = id, code = code, name = value)
