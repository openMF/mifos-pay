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
@Serializable
@Parcelize
data class Timeline(
    val submittedOnDate: List<Int> = emptyList(),
    val submittedByUsername: String = "",
    val submittedByFirstname: String = "",
    val submittedByLastname: String = "",
    val approvedOnDate: List<Int> = emptyList(),
    val approvedByUsername: String = "",
    val approvedByFirstname: String = "",
    val approvedByLastname: String = "",
    val activatedOnDate: List<Int> = emptyList(),
    val activatedByUsername: String = "",
    val activatedByFirstname: String = "",
    val activatedByLastname: String = "",
) : Parcelable
