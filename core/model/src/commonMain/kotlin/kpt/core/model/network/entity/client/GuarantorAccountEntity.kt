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

/**
 * A loan account for which the client acts as guarantor.
 *
 * @property id Unique id.
 * @property accountNo Account number.
 * @property productId Id of the product.
 * @property productName Name of the product.
 * @property shortProductName Short name of the product.
 * @property status Current status.
 * @property loanType Type of loan.
 * @property loanCycle Loan cycle number.
 * @property inArrears Whether payments are overdue.
 * @property originalLoan Original loan amount.
 * @property loanBalance Outstanding loan balance.
 * @property isActive Whether the loan is active.
 * @property relationship Relationship of the guarantor to the client.
 */
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

/**
 * Status of a loan with its lifecycle flags.
 *
 * @property id Unique id.
 * @property code Machine-readable code.
 * @property value Human-readable display value.
 * @property pendingApproval Pending approval.
 * @property waitingForDisbursal Approved and waiting for disbursal.
 * @property active Whether it is active.
 * @property closedObligationsMet Closed with obligations met.
 * @property closedWrittenOff Closed as written off.
 * @property closedRescheduled Closed as rescheduled.
 * @property closed Whether it is closed.
 * @property overpaid Whether it is overpaid.
 */
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
