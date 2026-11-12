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

import kotlinx.serialization.Serializable

/**
 * Domain model of the allow attribute overrides used in the loan application flow.
 *
 * @property amortizationType Whether amortization type.
 * @property interestType Whether interest type.
 * @property transactionProcessingStrategyId Identifier of the repayment transaction processing strategy.
 * @property interestCalculationPeriodType Whether interest calculation period type.
 * @property inArrearsTolerance Amount tolerated in arrears before the loan is considered overdue.
 * @property repaymentEvery Number of frequency units between repayments.
 * @property graceOnPrincipalAndInterestPayment Whether grace on principal and interest payment.
 * @property graceOnArrearsAgeing Grace days before arrears ageing starts.
 */
@Serializable
data class AllowAttributeOverrides(

    val amortizationType: Boolean? = null,

    val interestType: Boolean? = null,

    val transactionProcessingStrategyId: Boolean? = null,

    val interestCalculationPeriodType: Boolean? = null,

    val inArrearsTolerance: Boolean? = null,

    val repaymentEvery: Boolean? = null,

    val graceOnPrincipalAndInterestPayment: Boolean? = null,

    val graceOnArrearsAgeing: Boolean? = null,

)
