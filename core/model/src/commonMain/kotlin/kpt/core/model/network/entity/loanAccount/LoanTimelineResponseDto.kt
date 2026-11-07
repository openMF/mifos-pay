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
 * Dates and actors for each stage of a loan (submit, approve, disburse, close).
 *
 * @property submittedOnDate Date submitted, as [year, month, day].
 * @property submittedByUsername Username of the submitting user.
 * @property submittedByFirstname First name of the submitting user.
 * @property submittedByLastname Last name of the submitting user.
 * @property approvedOnDate Date approved, as [year, month, day].
 * @property approvedByUsername Username of the approving user.
 * @property approvedByFirstname First name of the approving user.
 * @property approvedByLastname Last name of the approving user.
 * @property expectedDisbursementDate Expected disbursement date.
 * @property actualDisbursementDate Actual disbursement date.
 * @property disbursedByUsername Username of the disbursing user.
 * @property disbursedByFirstname First name of the disbursing user.
 * @property disbursedByLastname Last name of the disbursing user.
 * @property closedOnDate Date closed, as [year, month, day].
 * @property expectedMaturityDate Expected maturity date.
 * @property withdrawnOnDate Date withdrawn.
 */
@Serializable
data class LoanTimelineResponseDto(
    val submittedOnDate: List<Int>? = null,

    val submittedByUsername: String? = null,

    val submittedByFirstname: String? = null,

    val submittedByLastname: String? = null,

    val approvedOnDate: List<Int>? = null,

    val approvedByUsername: String? = null,

    val approvedByFirstname: String? = null,

    val approvedByLastname: String? = null,

    val expectedDisbursementDate: List<Int>? = null,

    val actualDisbursementDate: List<Int>? = null,

    val disbursedByUsername: String? = null,

    val disbursedByFirstname: String? = null,

    val disbursedByLastname: String? = null,

    val closedOnDate: List<Int>? = null,

    val expectedMaturityDate: List<Int>? = null,

    val withdrawnOnDate: List<Int>? = null,

)
