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
data class InterBankTransferRequest(
    @SerialName("homeTransactionId")
    val homeTransactionId: String,
    @SerialName("from")
    val from: Party,
    @SerialName("to")
    val to: Party,
    @SerialName("amountType")
    val amountType: String,
    @SerialName("amount")
    val amount: Amount,
    @SerialName("transactionType")
    val transactionType: TransactionType,
    @SerialName("note")
    val note: String,
)

@Serializable
data class Party(
    @SerialName("fspId")
    val fspId: String,
    @SerialName("idType")
    val idType: String,
    @SerialName("idValue")
    val idValue: String,
)

@Serializable
data class Amount(
    @SerialName("currencyCode")
    val currencyCode: String,
    @SerialName("amount")
    val amount: Double,
)

@Serializable
data class TransactionType(
    @SerialName("scenario")
    val scenario: String,
    @SerialName("subScenario")
    val subScenario: String,
    @SerialName("initiator")
    val initiator: String,
    @SerialName("initiatorType")
    val initiatorType: String,
)

@Serializable
data class InterBankTransferResponse(
    @SerialName("homeTransactionId")
    val homeTransactionId: String,
    @SerialName("transactionId")
    val transactionId: String,
    @SerialName("systemMessage")
    val systemMessage: String,
    @SerialName("executionStatus")
    val executionStatus: Boolean,
)
