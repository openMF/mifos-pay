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
 * Request body for creating a standing instruction.
 *
 * @property fromOfficeId Identifier of the paying office.
 * @property fromClientId Identifier of the paying client.
 * @property fromAccountType Type of the paying account.
 * @property fromAccountId Identifier of the paying account.
 * @property toOfficeId Identifier of the receiving office.
 * @property toClientId Identifier of the receiving client.
 * @property toAccountType Type of the receiving account.
 * @property toAccountId Identifier of the receiving account.
 * @property name Name of the instruction.
 * @property amount Amount transferred on each run, as a string.
 * @property transferType Transfer type code.
 * @property instructionType Instruction type code.
 * @property priority Priority code of the instruction.
 * @property status Status code of the instruction.
 * @property recurrenceType Recurrence type code.
 * @property recurrenceFrequency Recurrence frequency code.
 * @property recurrenceInterval Interval between recurrences.
 * @property locale Locale used to parse numbers and dates.
 * @property validFrom Start of the validity period.
 * @property validTill End of the validity period.
 * @property dateFormat Format used for date values.
 * @property monthDayFormat Format used for the month-day value.
 * @property recurrenceOnMonthDay Day of the month on which the instruction recurs.
 */
@Serializable
@Parcelize
data class StandingInstructionPayload(
    val fromOfficeId: Long = 0,
    val fromClientId: Long = 0,
    val fromAccountType: String = "",
    val fromAccountId: Long = 0,

    val toOfficeId: Long = 0,
    val toClientId: Long = 0,
    val toAccountType: String = "",
    val toAccountId: Long = 0,

    val name: String = "",
    val amount: String = "",
    val transferType: Long = 0,
    val instructionType: Long = 0,
    val priority: Long? = 0,
    val status: Long? = 0,
    val recurrenceType: Long = 0,
    val recurrenceFrequency: Long = 0,
    val recurrenceInterval: String = "",

    val locale: String = "",
    val validFrom: String = "",
    val validTill: String = "",
    val dateFormat: String = "",
    val monthDayFormat: String = "",
    val recurrenceOnMonthDay: String = "",
) : Parcelable

/** Converts this payload into the [SIUpdatePayload] used to update an existing instruction. */
fun StandingInstructionPayload.toSIUploadPayload(): SIUpdatePayload {
    return SIUpdatePayload(
        priority = priority ?: 0L,
        status = status ?: 0L,
        locale = locale,
        validFrom = validFrom,
        validTill = validTill,
        dateFormat = dateFormat,
    )
}
