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
 * Share account status with its lifecycle flags.
 *
 * @property id Unique id.
 * @property code Machine-readable code.
 * @property value Human-readable display value.
 * @property submittedAndPendingApproval Submitted and pending approval.
 * @property approved Whether it is approved.
 * @property rejected Whether it is rejected.
 * @property active Whether it is active.
 * @property closed Whether it is closed.
 */
@Serializable
data class ShareStatusResponseDto(

    val id: Int? = null,

    val code: String? = null,

    val value: String? = null,

    val submittedAndPendingApproval: Boolean? = null,

    val approved: Boolean? = null,

    val rejected: Boolean? = null,

    val active: Boolean? = null,

    val closed: Boolean? = null,

)
