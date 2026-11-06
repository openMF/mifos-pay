/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.client

import kotlinx.serialization.Serializable

@Serializable
data class GuarantorAccountEntity(
    val id: Long? = null,
    val accountNo: String? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val shortProductName: String? = null,
    val status: LoanStatusEntity? = null,
    val loanType: Status? = null,
    val loanCycle: Int? = null,
    val inArrears: Boolean? = null,
    val originalLoan: Double? = null,
    val loanBalance: Double? = null,
    val isActive: Boolean? = null,
    val relationship: String? = null,
)

@Serializable
data class LoanStatusEntity(
    val id: Int? = null,
    val code: String? = null,
    val value: String? = null,
    val pendingApproval: Boolean? = null,
    val waitingForDisbursal: Boolean? = null,
    val active: Boolean? = null,
    val closedObligationsMet: Boolean? = null,
    val closedWrittenOff: Boolean? = null,
    val closedRescheduled: Boolean? = null,
    val closed: Boolean? = null,
    val overpaid: Boolean? = null,
)
