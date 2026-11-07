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

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single savings-account detail record — the Room mirror of the
 * `kpt.core.model.savingsaccount.SavingAccountDetail` domain model.
 *
 * Stored in the `wallet_saving_account_details` table. One row per savings account
 * (SINGLE-ROW-PER-KEY archetype — the DAO's PK is [id] which doubles as the store's
 * page key).
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideAccountDetailStore` fetcher's writer lambda —
 * no user-facing write path (`SavingsAccountRepository.blockAccount` / `unblockAccount`
 * / `createSavingsAccount` / `updateSavingsAccount`) ever calls
 * `SavingAccountDetailDao.upsert` directly. The server-echoed record appears here
 * via the next refresh cycle.
 *
 * ## Column mapping
 *
 * The domain `SavingAccountDetail` model has SIX nested `@Serializable` payloads —
 * [`DepositType`][kpt.core.model.savingsaccount.DepositType],
 * [`Status`][kpt.core.model.savingsaccount.Status],
 * [`Timeline`][kpt.core.model.savingsaccount.Timeline],
 * [`Currency`][kpt.core.model.savingsaccount.Currency],
 * [`Summary`][kpt.core.model.savingsaccount.Summary],
 * and `List<Transaction>` — each persisted as a JSON string column (see
 * `SavingAccountDetailEntityMapper`) rather than as normalized relation tables.
 * Rationale: `SavingAccountDetail` is a single-record snapshot the app renders
 * whole; there is no per-nested-field query the app performs. `List<Long>`
 * (`lastActiveTransactionDate`) is JSON-encoded for the same reason.
 *
 * `transactionsJson` intentionally overlaps with the LEDGER `wallet_transactions`
 * table populated by `provideHistoryStore` — the detail's transactions field is a
 * snapshot from the SAME server `getSavingsWithAssociations` endpoint, but the
 * detail store owns its own JSON copy so a detail-only refresh doesn't need to
 * consult the LEDGER. The two paths are consumed independently: the detail screen
 * renders `SavingAccountDetail.transactions` as-is; the history screen streams
 * `wallet_transactions`. No merge logic exists.
 *
 * @property id Fineract savings-account id — PK (unique per savings account).
 * @property accountNo Human-visible account number.
 * @property clientId Owning client id (indexed for future per-client filters).
 * @property clientName Owning client display name.
 * @property savingsProductId Product id.
 * @property savingsProductName Product name.
 * @property fieldOfficerId Assigned field officer id.
 * @property nominalAnnualInterestRate Signed rate.
 * @property withdrawalFeeForTransfers / [allowOverdraft] / [enforceMinRequiredBalance] /
 *   [lienAllowed] / [withHoldTax] / [isDormancyTrackingActive] — scalar flags.
 * @property depositTypeJson JSON encoding of `DepositType`.
 * @property statusJson JSON encoding of `Status`.
 * @property timelineJson JSON encoding of `Timeline`.
 * @property currencyJson JSON encoding of `Currency`.
 * @property summaryJson JSON encoding of `Summary`.
 * @property lastActiveTransactionDateJson JSON encoding of `List<Long>`.
 * @property transactionsJson JSON encoding of `List<Transaction>` — snapshot only.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 *
 * @property allowOverdraft Whether the account may go into overdraft.
 * @property enforceMinRequiredBalance Whether the product's minimum required balance is enforced.
 * @property lienAllowed Whether a lien may be placed on the account.
 * @property withHoldTax Whether withholding tax applies to interest.
 * @property isDormancyTrackingActive Whether dormancy tracking is active for the account.
 */
@DbEntity
@Entity(
    tableName = "wallet_saving_account_details",
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class SavingAccountDetailEntity(
    @PrimaryKey val id: Long,
    val accountNo: String,
    val clientId: Long,
    val clientName: String,
    val savingsProductId: Long,
    val savingsProductName: String,
    val fieldOfficerId: Long,
    val nominalAnnualInterestRate: Double,
    val withdrawalFeeForTransfers: Boolean,
    val allowOverdraft: Boolean,
    val enforceMinRequiredBalance: Boolean,
    val lienAllowed: Boolean,
    val withHoldTax: Boolean,
    val isDormancyTrackingActive: Boolean,
    val depositTypeJson: String,
    val statusJson: String,
    val timelineJson: String,
    val currencyJson: String,
    val summaryJson: String,
    val lastActiveTransactionDateJson: String,
    val transactionsJson: String,
    val fetchedAtEpochMs: Long = 0L,
)
