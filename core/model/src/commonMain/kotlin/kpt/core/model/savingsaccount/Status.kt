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
/**
 * Savings account status with the individual state flags reported by the server.
 *
 * @property id Status id.
 * @property code Status code.
 * @property value Human-readable status label.
 * @property submittedAndPendingApproval Submitted and awaiting approval.
 * @property approved Approved.
 * @property rejected Rejected.
 * @property withdrawnByApplicant Withdrawn by the applicant.
 * @property active Active.
 * @property closed Closed.
 * @property prematureClosed Closed before maturity.
 * @property transferInProgress A transfer is in progress.
 * @property transferOnHold A transfer is on hold.
 * @property matured Matured.
 */
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
