/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.account

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Serializable
@Parcelize
data class AccountTransferPayload(
    val fromOfficeId: Long,
    val fromClientId: Long,
    val fromAccountType: Long,
    val fromAccountId: Long,

    val toOfficeId: Long,
    val toClientId: Long,
    val toAccountType: Long,
    val toAccountId: Long,

    val transferAmount: String,
    val transferDescription: String,

    val locale: String,
    val dateFormat: String,
    val transferDate: String,
) : Parcelable
