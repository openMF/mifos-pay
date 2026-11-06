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
