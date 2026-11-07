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
/**
 * A recurring transfer (standing instruction) between two accounts.
 *
 * @property id Unique identifier.
 * @property accountDetailId Identifier of the account detail record.
 * @property name Name of the instruction.
 * @property fromOffice Office of the paying client.
 * @property fromClient Paying client.
 * @property fromAccountType Type of the paying account.
 * @property fromAccount Paying account.
 * @property toOffice Office of the receiving client.
 * @property toClient Receiving client.
 * @property toAccountType Type of the receiving account.
 * @property toAccount Receiving account.
 * @property transferType Type of the transfer.
 * @property priority Priority of the instruction.
 * @property instructionType Type of the instruction.
 * @property status Current status.
 * @property amount Amount transferred on each run.
 * @property validFrom Date from which the instruction is valid.
 * @property validTill Date until which the instruction is valid.
 * @property recurrenceType Recurrence type.
 * @property recurrenceFrequency Recurrence frequency.
 * @property recurrenceInterval Interval between recurrences.
 * @property recurrenceOnMonthDay Day of the month on which the instruction recurs.
 */
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

    /**
     * Account at either end of a standing instruction.
     *
     * @property id Unique identifier.
     * @property accountNo Account number.
     * @property productId Identifier of the savings product.
     * @property productName Name of the savings product.
     */
    @Serializable
    @Parcelize
    data class FromAndToAccount(
        val id: Long,
        val accountNo: String,
        val productId: Long,
        val productName: String,
    ) : Parcelable

    /**
     * Office at either end of a standing instruction.
     *
     * @property id Unique identifier.
     * @property name Display name.
     */
    @Serializable
    @Parcelize
    data class FromAndToOffice(
        val id: Long,
        val name: String,
    ) : Parcelable

    /**
     * Client at either end of a standing instruction.
     *
     * @property id Unique identifier.
     * @property displayName Display name.
     * @property officeId Identifier of the office.
     * @property officeName Name of the office.
     */
    @Serializable
    @Parcelize
    data class FromAndToClient(
        val id: Long,
        val displayName: String,
        val officeId: Long,
        val officeName: String,
    ) : Parcelable

    /**
     * A coded option (id, code and label) such as priority or status.
     *
     * @property id Unique identifier.
     * @property code Code identifying the item.
     * @property value Human-readable label.
     */
    @Serializable
    @Parcelize
    data class Option(
        val id: Long,
        val code: String,
        val value: String,
    ) : Parcelable
}
