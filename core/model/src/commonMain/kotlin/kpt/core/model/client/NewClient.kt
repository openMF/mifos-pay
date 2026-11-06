/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Parcelize
data class NewClient(
    val firstname: String,
    val lastname: String,
    val externalId: String,
    val mobileNo: String,
    val address: ClientAddress,
    val savingsProductId: Int,
) : Parcelable
