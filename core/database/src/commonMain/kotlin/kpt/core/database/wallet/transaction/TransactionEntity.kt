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

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single financial transaction — the Room mirror of the
 * `org.mifospay.core.model.savingsaccount.Transaction` domain model.
 *
 * Stored in the `wallet_transactions` table. Each row represents one transaction
 * on one savings account (LEDGER shape).
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideHistoryStore` fetcher's writer lambda — no user-facing
 * write path (transfer / deposit / withdrawal) ever calls `TransactionDao.upsert*`
 * directly. Money movement flows through the online repositories
 * (`ThirdPartyTransfer*Repository`, `SavingsAccount*Repository`, `AccountRepository`)
 * and the server-echoed ledger row appears here via the next refresh cycle.
 *
 * ## Column mapping
 *
 * The domain `Transaction` model has two nested nullable payloads —
 * [`Transaction.Transfer`][org.mifospay.core.model.savingsaccount.Transaction.Transfer]
 * and
 * [`Transaction.PaymentDetailData`][org.mifospay.core.model.savingsaccount.Transaction.PaymentDetailData].
 * Those are persisted as JSON strings in [transferJson] / [paymentDetailJson]
 * (see `TransactionEntityMapper`) rather than as normalized relation tables — the
 * two payloads are read-only from the app's perspective (server-echoed ledger detail)
 * and are only decoded on the transaction-detail screen. A `@TypeConverter` was
 * intentionally NOT used for these so the JSON encode/decode stays visible in the
 * mapper file and Room's schema stays free of custom converters for a nullable
 * data class.
 *
 * The nested `Currency` payload is flat-packed inline (`currency*` columns) so the
 * per-transaction currency is queryable without a join and doesn't require a
 * type converter (Room3 doesn't auto-embed multi-field data classes without
 * `@Embedded`, which we don't need here since Currency has a small stable shape).
 *
 * @property transactionId Fineract transaction id — PK (unique per savings-account transaction).
 * @property accountId Fineract savings-account id this transaction belongs to (page key for LEDGER).
 * @property accountNo Human-visible account number.
 * @property amount Signed transaction amount.
 * @property dateString Domain-format date (from `Transaction.date` — already a
 *   parsed string via `DateHelper.getDateAsString(...)`; kept as-is for pass-through).
 * @property dateEpochMs Parsed epoch-millis derived from [dateString] via
 *   `DateHelper.parseDateToMillis(...)`; used as the SORT + `latestDateForAccount`
 *   cursor. Nullable if parse fails (never expected for server-issued rows).
 * @property transactionType `TransactionType.name` — `CREDIT`/`DEBIT`/`OTHER`.
 * @property reversed True if this transaction was reversed on the server.
 * @property originalTransactionId Fineract original-transaction pointer (0 if none).
 * @property transferId Nullable transfer id when this transaction is one leg of a
 *   two-account transfer.
 * @property paymentDetailId Nullable payment-detail id.
 * @property description Human description (typically the transfer description).
 * @property currencyCode / [currencyName] / [currencyDecimalPlaces] /
 *   [currencyInMultiplesOf] / [currencyDisplaySymbol] / [currencyNameCode] /
 *   [currencyDisplayLabel] — flat-packed `Currency` fields.
 * @property transferJson Nullable JSON encoding of `Transaction.Transfer`.
 * @property paymentDetailJson Nullable JSON encoding of `Transaction.PaymentDetailData`.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future "stale-row eviction" prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_transactions",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["accountId", "dateEpochMs"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey val transactionId: Long,
    val accountId: Long,
    val accountNo: String,
    val amount: Double,
    val dateString: String,
    val dateEpochMs: Long?,
    val transactionType: String,
    val reversed: Boolean,
    val originalTransactionId: Long,
    val transferId: Long?,
    val paymentDetailId: Long?,
    val description: String,
    val currencyCode: String,
    val currencyName: String,
    val currencyDecimalPlaces: Int,
    val currencyInMultiplesOf: Int?,
    val currencyDisplaySymbol: String,
    val currencyNameCode: String,
    val currencyDisplayLabel: String,
    val transferJson: String?,
    val paymentDetailJson: String?,
    val fetchedAtEpochMs: Long = 0L,
)
