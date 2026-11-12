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

/**
 * Loan status with its lifecycle flags.
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
data class LoanStatusResponseDto(
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
