/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.shareAccount

import kotlinx.serialization.Serializable

/**
 * Dates and actors for each stage of a share account (submit, approve, activate).
 *
 * @property submittedOnDate Date submitted, as [year, month, day].
 * @property submittedByUsername Username of the submitting user.
 * @property submittedByFirstname First name of the submitting user.
 * @property submittedByLastname Last name of the submitting user.
 * @property approvedDate Date approved, as [year, month, day].
 * @property approvedByUsername Username of the approving user.
 * @property approvedByFirstname First name of the approving user.
 * @property approvedByLastname Last name of the approving user.
 * @property activatedDate Date activated, as [year, month, day].
 * @property activatedByUsername Username of the activating user.
 * @property activatedByFirstname First name of the activating user.
 * @property activatedByLastname Last name of the activating user.
 */
@Serializable
data class ShareTimelineResponseDto(

    val submittedOnDate: List<Int>? = null,

    val submittedByUsername: String? = null,

    val submittedByFirstname: String? = null,

    val submittedByLastname: String? = null,

    val approvedDate: List<Int>? = null,

    val approvedByUsername: String? = null,

    val approvedByFirstname: String? = null,

    val approvedByLastname: String? = null,

    val activatedDate: List<Int>? = null,

    val activatedByUsername: String? = null,

    val activatedByFirstname: String? = null,

    val activatedByLastname: String? = null,

)
