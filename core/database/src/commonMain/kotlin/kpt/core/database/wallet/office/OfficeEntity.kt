/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.office

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single Fineract office — the Room mirror of the
 * `kpt.core.model.office.Office` domain model.
 *
 * Stored in the `wallet_offices` table. Global reference-data LEDGER shape —
 * the office list is NOT client-scoped (the Fineract `GET /offices` endpoint
 * returns the entire organization tree visible to the current user); there is
 * no per-client page, so the store keys the entire list under a fixed singleton
 * key (see `OfficesKey`). The DAO's page semantics are therefore "delete-all +
 * upsert-all under a single @Transaction" — every fetch replaces the whole
 * table atomically.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is
 * populated exclusively by the Store5 `provideOfficeStore` fetcher's writer
 * lambda — no user-facing write path touches [OfficeDao] directly (office CRUD
 * is not a wallet-app surface).
 *
 * ## Column mapping
 *
 * All scalar columns are stored directly. `openingDate` is `List<Int>?` in the
 * domain (Fineract-flavor date components — `[year, month, day]`); it is
 * JSON-encoded as [openingDateJson] to keep the schema free of type converters
 * for List types (same pattern as `wallet_client_details.activationDateJson`).
 *
 * [id] is the PK — office ids are globally unique across the Fineract
 * organization tree.
 *
 * @property id Fineract office id — PK.
 * @property name Office display name (used as the sort key).
 * @property nameDecorated Nullable indented display name (hierarchy-decorated).
 * @property externalId Nullable external system id.
 * @property openingDateJson JSON encoding of `List<Int>?` — Fineract date parts
 *   (`null` in domain → `"null"` JSON literal, roundtripped by kotlinx.serialization).
 * @property hierarchy Nullable Fineract office hierarchy path (`.1.2.` style).
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_offices",
)
data class OfficeEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val nameDecorated: String?,
    val externalId: String?,
    val openingDateJson: String,
    val hierarchy: String?,
    val fetchedAtEpochMs: Long = 0L,
)
