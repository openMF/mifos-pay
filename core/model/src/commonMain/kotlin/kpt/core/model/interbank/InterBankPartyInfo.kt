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
 * Request for the details of a party in another bank.
 *
 * @property partyId Id of the party.
 * @property partyIdType Type of the party id.
 * @property currencyCode Currency code.
 * @property ownerFspId Id of the provider that owns the party.
 */
@Serializable
data class InterBankPartyInfoRequest(
    @SerialName("partyId")
    val partyId: String,
    @SerialName("partyIdType")
    val partyIdType: String,
    @SerialName("currencyCode")
    val currencyCode: String,
    @SerialName("ownerFspId")
    val ownerFspId: String,
)

/**
 * Details of a party in another bank, or an error describing why lookup failed.
 *
 * @property sourceFspId Id of the requesting provider.
 * @property destinationFspId Id of the provider holding the party.
 * @property requestId Id of the request.
 * @property partyId Id of the party.
 * @property partySubIdOrType Optional sub-id or type of the party.
 * @property currencyCode Currency code.
 * @property firstName First name of the party.
 * @property middleName Optional middle name.
 * @property lastName Last name of the party.
 * @property dateOfBirth Optional date of birth.
 * @property systemMessage Status message from the system.
 * @property executionStatus Whether the lookup succeeded.
 * @property errorCode Error code when the lookup failed.
 * @property errorMessage Error message when the lookup failed.
 * @property partyIdType Type of the party id.
 */
@Serializable
data class InterBankPartyInfoResponse(
    @SerialName("sourceFspId")
    val sourceFspId: String,

    @SerialName("destinationFspId")
    val destinationFspId: String,

    @SerialName("requestId")
    val requestId: String,

    @SerialName("partyId")
    val partyId: String,

    @SerialName("partySubIdOrType")
    val partySubIdOrType: String? = null,

    @SerialName("currencyCode")
    val currencyCode: String,

    @SerialName("firsName")
    val firstName: String,

    @SerialName("middleName")
    val middleName: String? = null,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("dateOfBirth")
    val dateOfBirth: String? = null,

    @SerialName("systemMessage")
    val systemMessage: String,

    @SerialName("executionStatus")
    val executionStatus: Boolean,

    @SerialName("errorCode")
    val errorCode: String? = null,

    @SerialName("errorMessage")
    val errorMessage: String? = null,

    @SerialName("partyIdType")
    val partyIdType: String,
)
