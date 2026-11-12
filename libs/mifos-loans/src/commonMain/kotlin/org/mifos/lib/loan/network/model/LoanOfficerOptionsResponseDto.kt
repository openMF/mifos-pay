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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network response DTO for the loan officer options, as returned by the Fineract loans API.
 *
 * @property id Unique identifier.
 * @property firstname First name.
 * @property lastname Last name.
 * @property displayName Name shown to the user.
 * @property mobileNo Mobile phone number.
 * @property officeId Identifier of the office.
 * @property officeName Name of the office.
 * @property loanOfficer Loan officer assigned to the loan.
 * @property active Whether this entry is active.
 * @property joiningDate Date the staff member joined.
 */
@Serializable
data class LoanOfficerOptionsResponseDto(

    val id: Int? = null,

    val firstname: String? = null,

    val lastname: String? = null,

    val displayName: String? = null,

    val mobileNo: String? = null,

    val officeId: Int? = null,

    val officeName: String? = null,

    @SerialName("isLoanOfficer")
    val loanOfficer: Boolean? = null,

    @SerialName("isActive")
    val active: Boolean? = null,

    val joiningDate: List<Int>? = null,

)
