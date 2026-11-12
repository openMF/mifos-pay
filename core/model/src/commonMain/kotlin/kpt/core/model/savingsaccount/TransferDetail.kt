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

/**
 * Full detail of an account transfer, including source and destination parties.
 *
 * @property id Unique identifier.
 * @property reversed Whether the transfer has been reversed.
 * @property currency Currency of the transfer.
 * @property transferAmount Amount transferred.
 * @property transferDate Date of the transfer.
 * @property transferDescription Description of the transfer.
 * @property fromOffice Office of the paying client.
 * @property fromClient Paying client.
 * @property fromAccountType Type of the paying account.
 * @property fromAccount Paying account.
 * @property toOffice Office of the receiving client.
 * @property toClient Receiving client.
 * @property toAccountType Type of the receiving account.
 * @property toAccount Receiving account.
 */
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
    /**
     * Currency of a transfer.
     *
     * @property code ISO currency code.
     * @property name Currency name.
     * @property decimalPlaces Number of decimal places.
     * @property inMultiplesOf Amounts must be multiples of this value.
     * @property displaySymbol Symbol shown for the currency.
     * @property nameCode Localisation key of the currency name.
     * @property displayLabel Label shown for the currency.
     */
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

    /**
     * Office of the sending client.
     *
     * @property id Unique identifier.
     * @property name Office name.
     */
    @Serializable
    data class FromOffice(
        val id: Long = 0,
        val name: String = "",
    )

    /**
     * Sending client of a transfer.
     *
     * @property id Unique identifier.
     * @property displayName Display name.
     * @property isStaff Whether the client is a staff member.
     * @property officeId Identifier of the office.
     * @property officeName Name of the office.
     */
    @Serializable
    data class FromClient(
        val id: Long = 0,
        val displayName: String = "",
        val isStaff: Boolean = false,
        val officeId: Long = 0,
        val officeName: String = "",
    )

    /**
     * Account type of the sending account.
     *
     * @property id Unique identifier.
     * @property code Code identifying the item.
     * @property value Human-readable label.
     */
    @Serializable
    data class FromAccountType(
        val id: Long = 0,
        val code: String = "",
        val value: String = "",
    )

    /**
     * Sending account of a transfer.
     *
     * @property id Unique identifier.
     * @property accountNo Account number.
     */
    @Serializable
    data class FromAccount(
        val id: Long = 0,
        val accountNo: String = "",
    )

    /**
     * Office of the receiving client.
     *
     * @property id Unique identifier.
     * @property name Office name.
     */
    @Serializable
    data class ToOffice(
        val id: Long = 0,
        val name: String = "",
    )

    /**
     * Receiving client of a transfer.
     *
     * @property id Unique identifier.
     * @property displayName Display name.
     * @property isStaff Whether the client is a staff member.
     * @property officeId Identifier of the office.
     * @property officeName Name of the office.
     */
    @Serializable
    data class ToClient(
        val id: Long = 0,
        val displayName: String = "",
        val isStaff: Boolean = false,
        val officeId: Long = 0,
        val officeName: String = "",
    )

    /**
     * Account type of the receiving account.
     *
     * @property id Unique identifier.
     * @property code Code identifying the item.
     * @property value Human-readable label.
     */
    @Serializable
    data class ToAccountType(
        val id: Long = 0,
        val code: String = "",
        val value: String = "",
    )

    /**
     * Receiving account of a transfer.
     *
     * @property id Unique identifier.
     * @property accountNo Account number.
     */
    @Serializable
    data class ToAccount(
        val id: Long = 0,
        val accountNo: String = "",
    )
}
