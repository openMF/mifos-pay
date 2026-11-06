/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.standinginstruction

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
@Serializable
@Parcelize
data class StandingInstruction(
    val id: Long? = null,
    val accountDetailId: Long? = null,
    val name: String? = null,
    val fromOffice: FromAndToOffice? = null,
    val fromClient: FromAndToClient? = null,
    val fromAccountType: String? = null,
    val fromAccount: FromAndToAccount? = null,
    val toOffice: FromAndToOffice? = null,
    val toClient: FromAndToClient? = null,
    val toAccountType: String? = null,
    val toAccount: FromAndToAccount? = null,
    val transferType: String? = null,
    val priority: Option? = null,
    val instructionType: String? = null,
    val status: Option? = null,
    val amount: Double? = null,
    val validFrom: String? = null,
    val validTill: String? = null,
    val recurrenceType: String? = null,
    val recurrenceFrequency: String? = null,
    val recurrenceInterval: Long? = null,
    val recurrenceOnMonthDay: List<Int>? = emptyList(),
) : Parcelable {

    @Serializable
    @Parcelize
    data class FromAndToAccount(
        val id: Long,
        val accountNo: String,
        val productId: Long,
        val productName: String,
    ) : Parcelable

    @Serializable
    @Parcelize
    data class FromAndToOffice(
        val id: Long,
        val name: String,
    ) : Parcelable

    @Serializable
    @Parcelize
    data class FromAndToClient(
        val id: Long,
        val displayName: String,
        val officeId: Long,
        val officeName: String,
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Option(
        val id: Long,
        val code: String,
        val value: String,
    ) : Parcelable
}
