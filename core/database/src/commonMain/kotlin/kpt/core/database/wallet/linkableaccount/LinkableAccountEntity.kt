/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.linkableaccount

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single account that a client CAN link into a pocket — the
 * Room mirror of the `org.mifospay.core.model.pocket.LinkableAccount` domain model.
 *
 * Stored in the `wallet_linkable_accounts` table. LEDGER shape — many rows per
 * client, page-keyed by [clientId]. Content is a **derived read cache**: the
 * store's fetcher pulls the client's full account envelope
 * (`clientsApi.getClientAccounts`), enriches SHARE rows with market-price
 * (per-account round-trip), and filters against the already-linked set the
 * `wallet_pockets` LEDGER holds — the result is the list of accounts that are
 * NOT yet linked and are therefore eligible to link.
 *
 * ## Store shape
 *
 * `createStore` (Store5 read-cache, NOT `createOfflineStore`) — the data is
 * network-derived; the LEDGER Room table exists purely to serve offline-first
 * reads and to survive process death between "open manage-pocket sheet" and
 * "tap link". Writes stay online (`PocketRepository.linkAccounts` /
 * `delinkAccounts`); the next re-subscribe pulls fresh derived data through the
 * store (parity with the Pocket / Beneficiary write paths — GOAL D1).
 *
 * ## Column mapping
 *
 * Every domain field is flat-packed as a scalar column — no JSON encoding is
 * needed because `LinkableAccount` is a small stable value type and no field is
 * `@Serializable`. Enums are stored as their `.name` string (portable across
 * schema versions and free of Room type converters).
 *
 * REPLACES upstream PR #2057's `org.mifospay.core.datastore.model.LinkableAccountEntity`
 * (multiplatform-settings JSON, kotlinx.serialization) — this branch's Store5
 * architecture serves this data through Room SoT instead (matches
 * `PocketStore` / `BeneficiaryStore` / `SelfAccountsStore` recipe).
 *
 * @property accountId Fineract account id (part of the composite PK).
 * @property clientId Owning client id (the store key, doubles as page cursor;
 *   stamped by the mapper at write time — the network row does not carry it).
 * @property accountType `AccountType.name` — `LOAN` / `SAVINGS` / `SHARE`. Part
 *   of the composite PK because Fineract uses independent id sequences per
 *   account-type table; the same numeric `accountId` could theoretically appear
 *   across two of the three types.
 * @property productName Nullable Fineract product name (e.g. "Regular Saver").
 * @property accountNumber Nullable human-visible account number.
 * @property balance Nullable balance amount (SHARE = approvedShares * currentMarketPrice).
 * @property currencyCode Nullable ISO-like currency code.
 * @property currencyDisplaySymbol Nullable per-currency display glyph (e.g. `"$"` / `"₹"`).
 *   Persisted end-to-end (unlike the sibling `PocketEntity` which surfaces it as
 *   `null` from SoT reads — this is a fresh table so we take the field from day one).
 * @property decimalPlaces Nullable currency decimal places.
 * @property status Nullable `AccountStatus.name` — `PENDING`/`APPROVED`/`ACTIVE`/…/`UNKNOWN`.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's
 *   writer for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_linkable_accounts",
    primaryKeys = ["clientId", "accountType", "accountId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class LinkableAccountEntity(
    val accountId: Long,
    val clientId: Long,
    val accountType: String,
    val productName: String?,
    val accountNumber: String?,
    val balance: Double?,
    val currencyCode: String?,
    val currencyDisplaySymbol: String?,
    val decimalPlaces: Int?,
    val status: String?,
    val fetchedAtEpochMs: Long = 0L,
)
