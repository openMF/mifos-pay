/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.savedcards

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single saved payment card — the Room mirror of the
 * `kpt.core.model.savedcards.SavedCard` domain model.
 *
 * Stored in the `wallet_saved_cards` table. LEDGER shape — many rows per client,
 * page-keyed by [clientId]. The Fineract datatable API
 * `/datatables/saved_cards/{clientId}` is already scoped to a specific client on
 * the server side, so [clientId] is both the API path parameter AND the store's
 * cache key.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideSavedCardStore` fetcher's writer lambda — no
 * user-facing write path (`SavedCardRepository.addSavedCard` / `updateCard` /
 * `deleteCard`) ever calls `SavedCardDao.upsert*` directly. Server-echoed rows
 * appear here on the next refresh cycle.
 *
 * ## Column mapping
 *
 * `createdAt` + `updatedAt` are `List<Long>` in the domain (Fineract-flavor
 * date components — `[year, month, day, hour, min, sec, ...]`). Because they are
 * fixed-shape but not scalar, they are JSON-encoded as `createdAtJson` /
 * `updatedAtJson`; storing them as a single serialized string keeps Room's
 * schema free of type converters for List types and matches the pattern used in
 * `wallet_transactions` (nested JSON payloads).
 *
 * `id + clientId` is the composite PK — the same card `id` from Fineract paired
 * with the owning client so a global unique-id collision across clients cannot
 * conflate rows.
 *
 * ## Card data sensitivity note
 *
 * `cardNumber` and `cvv` are stored as-is here to preserve read parity with the
 * existing server API (Fineract exposes both). Encrypting them at rest is a
 * follow-up item for the Phase-5 security pass — do NOT block the read-cache
 * store rollout on it (the caching layer only mirrors what the API returns to
 * the authenticated client's own session). The existing datastore path did not
 * encrypt either; this rollout is behavior-parity, not a regression.
 *
 * @property id Fineract card id.
 * @property clientId Owning client id (the store key, doubles as page cursor).
 * @property firstName / [lastName] Cardholder name components.
 * @property cardNumber PAN (as returned by the datatable).
 * @property cvv Card verification value (as returned).
 * @property expiryDate `MMYY` string as returned by the datatable.
 * @property backgroundColor Hex-color chip for the UI card affordance.
 * @property createdAtJson JSON encoding of `List<Long>` — Fineract date parts.
 * @property updatedAtJson JSON encoding of `List<Long>` — Fineract date parts.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_saved_cards",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class SavedCardEntity(
    val id: Long,
    val clientId: Long,
    val firstName: String,
    val lastName: String,
    val cardNumber: String,
    val cvv: String,
    val expiryDate: String,
    val backgroundColor: String,
    val createdAtJson: String,
    val updatedAtJson: String,
    val fetchedAtEpochMs: Long = 0L,
)
