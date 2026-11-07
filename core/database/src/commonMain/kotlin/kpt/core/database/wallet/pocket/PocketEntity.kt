/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.pocket

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single linked pocket account — the Room mirror of the
 * `kpt.core.model.pocket.DetailedPocketAccount` domain aggregate
 * (which wraps `PocketAccount` + derived detail fields).
 *
 * Stored in the `wallet_pockets` table. LEDGER shape — many rows per client,
 * page-keyed by [clientId]. The domain's `PocketAccount.id` is the pocket-to-account
 * mapping id issued by the Fineract `pocketApi.getPocketAccounts()` endpoint;
 * that is used as the row [id] here (composite PK with [clientId] so a global
 * unique-id collision across clients cannot conflate rows).
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is
 * populated exclusively by the Store5 `providePocketStore` fetcher's writer
 * lambda — no user-facing write path (`PocketRepository.linkAccounts` /
 * `delinkAccounts`) ever calls `PocketDao.upsert*` directly. Server-echoed
 * rows appear here on the next refresh cycle (SWR or explicit refresh).
 *
 * ## Column mapping
 *
 * Every domain field is flat-packed as a scalar column — no JSON encoding is
 * needed because none of `PocketAccount` / `DetailedPocketAccount` /
 * `AccountStatus` / `AccountType` is `@Serializable`, and the shape is small
 * and stable. Enums are stored as their `.name` string (portable across
 * schema versions and free of Room type converters).
 *
 * The pre-store `PocketRepositoryImp` used to keep the WHOLE
 * `List<DetailedPocketAccount>` in an in-memory `MutableStateFlow` cache
 * (`detailedPocketCache`) — that cache is REMOVED by this store rollout, and
 * this table becomes the single source of truth (Store5 SoT).
 *
 * @property id Fineract pocket-mapping id (`PocketAccount.id`, unique per client).
 * @property clientId Owning client id (the store key, doubles as page cursor;
 *   stamped by the mapper at write time — the server row does not carry it).
 * @property pocketId Fineract pocket id (`PocketAccount.pocketId`).
 * @property accountId Underlying account id (savings / loan / share).
 * @property accountType `AccountType.name` — `LOAN` / `SAVINGS` / `SHARE`.
 * @property accountNumber Human-visible account number.
 * @property productName Nullable Fineract product name (e.g. "Regular Saver").
 * @property balance Nullable balance amount (SHARE = approvedShares * currentMarketPrice).
 * @property currencyCode Nullable ISO-like currency code.
 * @property decimalPlaces Nullable currency decimal places.
 * @property status Nullable `AccountStatus.name` — `PENDING`/`APPROVED`/`ACTIVE`/…/`UNKNOWN`.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 *
 * @property currencyDisplaySymbol Nullable currency display symbol.
 * @property syncStatus Sync state: SYNCED, PENDING_LINK or PENDING_DELINK.
 */
@DbEntity
@Entity(
    tableName = "wallet_pockets",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class PocketEntity(
    val id: Long,
    val clientId: Long,
    val pocketId: Long,
    val accountId: Long,
    val accountType: String,
    val accountNumber: String,
    val productName: String?,
    val balance: Double?,
    val currencyCode: String?,
    val currencyDisplaySymbol: String?,
    val decimalPlaces: Int?,
    val status: String?,
    val fetchedAtEpochMs: Long = 0L,
    @ColumnInfo(defaultValue = "'SYNCED'")
    val syncStatus: String = "SYNCED",
)
