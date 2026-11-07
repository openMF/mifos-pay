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
/**
 * A saved payee the user can transfer money to.
 *
 * @property id Unique id of the beneficiary.
 * @property name Display name of the beneficiary.
 * @property officeName Name of the beneficiary's office.
 * @property clientName Name of the client that owns the beneficiary account.
 * @property accountType Type of the beneficiary account.
 * @property accountNumber Account number of the beneficiary.
 * @property transferLimit Maximum amount allowed per transfer.
 * @property officeId Id of the beneficiary's office.
 */
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

    /**
     * Type of a beneficiary account as returned by the API.
     *
     * @property id Id of the account type.
     * @property code Machine-readable code.
     * @property value Human-readable label.
     */
    @Serializable
    @Parcelize
    data class AccountType(
        val id: Int,
        val code: String,
        val value: String,
    ) : Parcelable
}
