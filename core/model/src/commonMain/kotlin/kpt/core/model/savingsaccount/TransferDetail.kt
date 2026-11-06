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

@Serializable
data class TransferDetail(
    val id: Long,
    val reversed: Boolean = false,
    val currency: Currency = Currency(),
    val transferAmount: Double = 0.0,
    val transferDate: String = "",
    val transferDescription: String? = null,
    val fromOffice: FromOffice,
    val fromClient: FromClient,
    val fromAccountType: FromAccountType,
    val fromAccount: FromAccount,
    val toOffice: ToOffice,
    val toClient: ToClient,
    val toAccountType: ToAccountType,
    val toAccount: ToAccount,
) {
    @Serializable
    data class Currency(
        val code: String = "",
        val name: String = "",
        val decimalPlaces: Long = 0L,
        val inMultiplesOf: Long = 0L,
        val displaySymbol: String = "",
        val nameCode: String = "",
        val displayLabel: String = "",
    )

    @Serializable
    data class FromOffice(
        val id: Long = 0,
        val name: String = "",
    )

    @Serializable
    data class FromClient(
        val id: Long = 0,
        val displayName: String = "",
        val isStaff: Boolean = false,
        val officeId: Long = 0,
        val officeName: String = "",
    )

    @Serializable
    data class FromAccountType(
        val id: Long = 0,
        val code: String = "",
        val value: String = "",
    )

    @Serializable
    data class FromAccount(
        val id: Long = 0,
        val accountNo: String = "",
    )

    @Serializable
    data class ToOffice(
        val id: Long = 0,
        val name: String = "",
    )

    @Serializable
    data class ToClient(
        val id: Long = 0,
        val displayName: String = "",
        val isStaff: Boolean = false,
        val officeId: Long = 0,
        val officeName: String = "",
    )

    @Serializable
    data class ToAccountType(
        val id: Long = 0,
        val code: String = "",
        val value: String = "",
    )

    @Serializable
    data class ToAccount(
        val id: Long = 0,
        val accountNo: String = "",
    )
}
