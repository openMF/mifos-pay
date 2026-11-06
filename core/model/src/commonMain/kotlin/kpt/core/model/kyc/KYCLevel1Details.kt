/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.kyc

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Serializable
@Parcelize
data class KYCLevel1Details(
    val firstName: String,
    val lastName: String,
    val addressLine1: String,
    val addressLine2: String,
    val mobileNo: String,
    val dob: String,
    val currentLevel: String,
) : Parcelable
