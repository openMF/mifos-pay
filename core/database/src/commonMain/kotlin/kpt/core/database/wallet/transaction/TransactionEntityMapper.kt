/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.transaction

import kotlinx.serialization.json.Json
import kpt.core.common.DateHelper
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
/**
 * Entity ↔ domain mapper for the `wallet_transactions` LEDGER table.
 *
 * The nested `Transaction.Transfer` + `Transaction.PaymentDetailData` payloads
 * are persisted as JSON strings — see [TransactionEntity]'s KDoc for the
 * rationale (read-only server-echoed detail, no need to normalize into
 * relation tables).
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server
 * DTO doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [Transaction] the app renders. */
fun TransactionEntity.toDomain(): Transaction = Transaction(
    reversed = reversed,
    accountId = accountId,
    amount = amount,
    date = dateString,
    currency = Currency(
        code = currencyCode,
        name = currencyName,
        decimalPlaces = currencyDecimalPlaces,
        inMultiplesOf = currencyInMultiplesOf,
        displaySymbol = currencyDisplaySymbol,
        nameCode = currencyNameCode,
        displayLabel = currencyDisplayLabel,
    ),
    transactionType = runCatching { TransactionType.valueOf(transactionType) }
        .getOrDefault(TransactionType.OTHER),
    transactionId = transactionId,
    accountNo = accountNo,
    transferId = transferId,
    originalTransactionId = originalTransactionId,
    paymentDetailId = paymentDetailId,
    description = description,
    transfer = transferJson?.let { runCatching { json.decodeFromString<Transaction.Transfer>(it) }.getOrNull() },
    paymentDetailData = paymentDetailJson?.let {
        runCatching { json.decodeFromString<Transaction.PaymentDetailData>(it) }.getOrNull()
    },
)

/**
 * Convert a fetched domain [Transaction] into a persistable row.
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Transaction.toEntity(fetchedAtEpochMs: Long): TransactionEntity = TransactionEntity(
    transactionId = transactionId,
    accountId = accountId,
    accountNo = accountNo,
    amount = amount,
    dateString = date,
    // DateHelper.parseDateToMillis returns Long? — the DAO's `latestDateForAccount`
    // MAX() skips nulls automatically, so an unparseable row's cursor influence is 0.
    dateEpochMs = DateHelper.parseDateToMillis(date),
    transactionType = transactionType.name,
    reversed = reversed,
    originalTransactionId = originalTransactionId,
    transferId = transferId,
    paymentDetailId = paymentDetailId,
    description = description,
    currencyCode = currency.code,
    currencyName = currency.name,
    currencyDecimalPlaces = currency.decimalPlaces,
    currencyInMultiplesOf = currency.inMultiplesOf,
    currencyDisplaySymbol = currency.displaySymbol,
    currencyNameCode = currency.nameCode,
    currencyDisplayLabel = currency.displayLabel,
    transferJson = transfer?.let { json.encodeToString(Transaction.Transfer.serializer(), it) },
    paymentDetailJson = paymentDetailData?.let {
        json.encodeToString(Transaction.PaymentDetailData.serializer(), it)
    },
    fetchedAtEpochMs = fetchedAtEpochMs,
)
