/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.interbank

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to register or look up a party as an interbank participant.
 *
 * @property partyId Id of the party.
 * @property partyIdType Type of the party id.
 * @property currencyCode Currency code.
 */
@Serializable
data class InterBankParticipantRequest(
    @SerialName("partyId")
    val partyId: String,
    @SerialName("partyIdType")
    val partyIdType: String,
    @SerialName("currencyCode")
    val currencyCode: String,
)

/**
 * Response to an interbank participant request.
 *
 * @property partyId Id of the party.
 * @property fspId Id of the financial service provider holding the party.
 * @property executionStatus Whether the request executed successfully.
 * @property systemMessage Status message from the system.
 */
@Serializable
data class InterBankParticipantResponse(
    @SerialName("partyId")
    val partyId: String,
    @SerialName("fspId")
    val fspId: String,
    @SerialName("executionStatus")
    val executionStatus: Boolean,
    @SerialName("systemMessage")
    val systemMessage: String,
)
