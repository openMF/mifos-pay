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
 * Request to transfer money to a party in another bank.
 *
 * @property homeTransactionId Transaction id assigned by the sending bank.
 * @property from Sending party.
 * @property to Receiving party.
 * @property amountType Whether the amount is a send or receive amount.
 * @property amount Amount and currency to transfer.
 * @property transactionType Scenario and initiator of the transaction.
 * @property note Note attached to the transfer.
 */
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

/**
 * A party in an interbank transfer, identified by provider and id.
 *
 * @property fspId Id of the financial service provider.
 * @property idType Type of the party id.
 * @property idValue Value of the party id.
 */
@Serializable
data class Party(
    @SerialName("fspId")
    val fspId: String,
    @SerialName("idType")
    val idType: String,
    @SerialName("idValue")
    val idValue: String,
)

/**
 * An amount of money with its currency.
 *
 * @property currencyCode Currency code.
 * @property amount Numeric amount.
 */
@Serializable
data class Amount(
    @SerialName("currencyCode")
    val currencyCode: String,
    @SerialName("amount")
    val amount: Double,
)

/**
 * Describes the scenario and initiator of an interbank transaction.
 *
 * @property scenario Transaction scenario.
 * @property subScenario Transaction sub-scenario.
 * @property initiator Who initiates the transaction.
 * @property initiatorType Type of the initiator.
 */
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

/**
 * Response to an interbank transfer request.
 *
 * @property homeTransactionId Transaction id assigned by the sending bank.
 * @property transactionId Transaction id assigned by the switch.
 * @property systemMessage Status message from the system.
 * @property executionStatus Whether the transfer executed successfully.
 */
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
