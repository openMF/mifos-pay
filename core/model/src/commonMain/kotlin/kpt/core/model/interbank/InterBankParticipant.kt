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

@Serializable
data class InterBankParticipantRequest(
    @SerialName("partyId")
    val partyId: String,
    @SerialName("partyIdType")
    val partyIdType: String,
    @SerialName("currencyCode")
    val currencyCode: String,
)

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
