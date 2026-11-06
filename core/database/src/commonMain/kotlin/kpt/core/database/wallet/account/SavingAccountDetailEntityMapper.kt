/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.account

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.DepositType
import kpt.core.model.savingsaccount.SavingAccountDetail
import kpt.core.model.savingsaccount.Status
import kpt.core.model.savingsaccount.Summary
import kpt.core.model.savingsaccount.Timeline
import kpt.core.model.savingsaccount.Transaction
/**
 * Entity ↔ domain mapper for the `wallet_saving_account_details` table.
 *
 * All six nested `@Serializable` payloads (`DepositType`, `Status`, `Timeline`,
 * `Currency`, `Summary`, `List<Transaction>`) — plus `List<Long>` for
 * `lastActiveTransactionDate` — are persisted as JSON strings. See
 * [SavingAccountDetailEntity]'s KDoc for the rationale (single-record whole-payload
 * read model, no per-field query need).
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [SavingAccountDetail] the app renders. */
fun SavingAccountDetailEntity.toDomain(): SavingAccountDetail = SavingAccountDetail(
    id = id,
    accountNo = accountNo,
    clientId = clientId,
    clientName = clientName,
    savingsProductId = savingsProductId,
    savingsProductName = savingsProductName,
    fieldOfficerId = fieldOfficerId,
    nominalAnnualInterestRate = nominalAnnualInterestRate,
    withdrawalFeeForTransfers = withdrawalFeeForTransfers,
    allowOverdraft = allowOverdraft,
    enforceMinRequiredBalance = enforceMinRequiredBalance,
    lienAllowed = lienAllowed,
    withHoldTax = withHoldTax,
    isDormancyTrackingActive = isDormancyTrackingActive,
    depositType = json.decodeFromString(DepositType.serializer(), depositTypeJson),
    status = json.decodeFromString(Status.serializer(), statusJson),
    timeline = json.decodeFromString(Timeline.serializer(), timelineJson),
    currency = json.decodeFromString(Currency.serializer(), currencyJson),
    summary = json.decodeFromString(Summary.serializer(), summaryJson),
    lastActiveTransactionDate = json.decodeFromString(
        ListSerializer(Long.serializer()),
        lastActiveTransactionDateJson,
    ),
    transactions = json.decodeFromString(
        ListSerializer(Transaction.serializer()),
        transactionsJson,
    ),
)

/**
 * Convert a fetched domain [SavingAccountDetail] into a persistable row.
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun SavingAccountDetail.toEntity(fetchedAtEpochMs: Long): SavingAccountDetailEntity =
    SavingAccountDetailEntity(
        id = id,
        accountNo = accountNo,
        clientId = clientId,
        clientName = clientName,
        savingsProductId = savingsProductId,
        savingsProductName = savingsProductName,
        fieldOfficerId = fieldOfficerId,
        nominalAnnualInterestRate = nominalAnnualInterestRate,
        withdrawalFeeForTransfers = withdrawalFeeForTransfers,
        allowOverdraft = allowOverdraft,
        enforceMinRequiredBalance = enforceMinRequiredBalance,
        lienAllowed = lienAllowed,
        withHoldTax = withHoldTax,
        isDormancyTrackingActive = isDormancyTrackingActive,
        depositTypeJson = json.encodeToString(DepositType.serializer(), depositType),
        statusJson = json.encodeToString(Status.serializer(), status),
        timelineJson = json.encodeToString(Timeline.serializer(), timeline),
        currencyJson = json.encodeToString(Currency.serializer(), currency),
        summaryJson = json.encodeToString(Summary.serializer(), summary),
        lastActiveTransactionDateJson = json.encodeToString(
            ListSerializer(Long.serializer()),
            lastActiveTransactionDate,
        ),
        transactionsJson = json.encodeToString(
            ListSerializer(Transaction.serializer()),
            transactions,
        ),
        fetchedAtEpochMs = fetchedAtEpochMs,
    )
