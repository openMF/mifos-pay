/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.selfaccounts

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single "self account" (own savings account) — the Room
 * mirror of the `org.mifospay.core.model.account.Account` domain model.
 *
 * Stored in the `wallet_self_accounts` table. LEDGER shape — many rows per
 * client, page-keyed by [clientId]. The Fineract endpoint
 * `GET /clients/{clientId}/accounts?fields=savings` is scoped to a specific
 * client on the server side, so [clientId] is both the API path parameter AND
 * the store's cache key.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is
 * populated exclusively by the Store5 `provideSelfAccountsStore` fetcher's
 * writer lambda. No user-facing write path touches [SelfAccountDao] directly.
 *
 * ## Column mapping
 *
 * Scalars are stored directly. Three nested payloads are JSON-encoded (same
 * pattern as `wallet_client_details`):
 * - `currency: Currency` (@Serializable) → [currencyJson]
 * - `status: Status` (@Serializable) → [statusJson]
 * - `accountType: AccountType?` (@Serializable, nullable) → [accountTypeJson]
 *   (JSON `"null"` literal for the nullable-absent case)
 *
 * Rationale: the `Account` domain model is a single-record snapshot the QR /
 * account-picker surfaces render whole; there is no per-nested-field query
 * need. This mirrors the treatment of `wallet_client_details.timelineJson`.
 *
 * `id + clientId` is the composite PK — the same Fineract account `id` paired
 * with the owning client so a logout-then-login as a different client cannot
 * conflate rows.
 *
 * @property id Fineract savings-account id (unique per client-list).
 * @property clientId Owning client id (the store key, doubles as page cursor).
 * @property image Domain "image" field (currently always `""` in the mapper).
 * @property name Account display name.
 * @property number Human-visible account number.
 * @property balance Account balance amount.
 * @property externalId Nullable external system id.
 * @property productName Nullable Fineract product name (e.g. "Regular Saver").
 * @property productId Fineract product id.
 * @property clientName Human-visible client name.
 * @property officeName Nullable owning office name.
 * @property officeId Nullable owning office id.
 * @property currencyJson JSON encoding of `Currency` (@Serializable payload).
 * @property statusJson JSON encoding of `Status` (@Serializable payload).
 * @property accountTypeJson JSON encoding of `AccountType?` (@Serializable, nullable).
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_self_accounts",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class SelfAccountEntity(
    val id: Long,
    val clientId: Long,
    val image: String,
    val name: String,
    val number: String,
    val balance: Double,
    val externalId: String?,
    val productName: String?,
    val productId: Long,
    val clientName: String,
    val officeName: String?,
    val officeId: Int?,
    val currencyJson: String,
    val statusJson: String,
    val accountTypeJson: String,
    val fetchedAtEpochMs: Long = 0L,
)
