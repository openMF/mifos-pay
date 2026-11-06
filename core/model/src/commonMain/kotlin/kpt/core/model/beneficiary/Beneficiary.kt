/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.beneficiary

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Serializable
@Parcelize
data class Beneficiary(
    val id: Long = 0L,
    val name: String = "",
    val officeName: String,
    val clientName: String,
    val accountType: AccountType,
    val accountNumber: String,
    val transferLimit: Int = 0,
    val officeId: Long? = null,
) : Parcelable {

    @Serializable
    @Parcelize
    data class AccountType(
        val id: Int,
        val code: String,
        val value: String,
    ) : Parcelable
}
