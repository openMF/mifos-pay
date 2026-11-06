/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.loanAccount

import kotlinx.serialization.Serializable
import kpt.core.model.network.entity.common.CurrencyResponseDto
import kpt.core.model.network.entity.common.TypeResponseDto
@Serializable
data class LoanAccountResponseDto(
    val id: Long? = null,

    val loanProductId: Long? = null,

    val externalId: String? = null,

    val numberOfRepayments: Long? = null,

    val accountNo: String? = null,

    val productName: String? = null,

    val productId: Int? = null,

    val loanProductName: String? = null,

    val clientName: String? = null,

    val loanProductDescription: String? = null,

    val principal: Double? = null,

    val annualInterestRate: Double? = null,

    val status: LoanStatusResponseDto? = null,

    val loanType: TypeResponseDto? = null,

    val loanCycle: Int? = null,

    val loanBalance: Double? = null,

    val amountPaid: Double? = null,

    val currency: CurrencyResponseDto? = null,

    val inArrears: Boolean? = null,

    val summary: LoanSummaryResponseDto? = null,

    val loanPurposeName: String? = null,

    val timeline: LoanTimelineResponseDto? = null,
)
