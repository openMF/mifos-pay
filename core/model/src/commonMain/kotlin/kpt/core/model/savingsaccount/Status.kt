/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savingsaccount

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Parcelize
@Serializable
data class Status(
    val id: Int,
    val code: String,
    val value: String,
    val submittedAndPendingApproval: Boolean,
    val approved: Boolean,
    val rejected: Boolean,
    val withdrawnByApplicant: Boolean,
    val active: Boolean,
    val closed: Boolean,
    val prematureClosed: Boolean,
    val transferInProgress: Boolean,
    val transferOnHold: Boolean,
    val matured: Boolean,
) : Parcelable
