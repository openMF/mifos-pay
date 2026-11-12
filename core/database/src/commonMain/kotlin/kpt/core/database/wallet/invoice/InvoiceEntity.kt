/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.invoice

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single invoice — the Room mirror of the
 * `kpt.core.model.datatables.invoice.Invoice` domain model.
 *
 * Stored in the `wallet_invoices` table. LEDGER shape — many rows per client,
 * page-keyed by [clientId]. The Fineract datatable API
 * `GET /datatables/invoice/{clientId}` is already scoped to a specific client
 * on the server side, so [clientId] is both the API path parameter AND the
 * store's cache key.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideInvoiceStore` fetcher's writer lambda — no
 * user-facing write path (`InvoiceRepository.createInvoice` / `updateInvoice` /
 * `deleteInvoice`) ever calls `InvoiceDao.upsert*` directly. Server-echoed rows
 * appear here on the next refresh cycle.
 *
 * ## Column mapping
 *
 * All scalar columns are stored directly. `createdAt` and `updatedAt` are
 * `List<Long>` in the domain (Fineract-flavor date components — `[year, month,
 * day, hour, min, sec, ...]`); they are JSON-encoded as `createdAtJson` /
 * `updatedAtJson`. Storing them as a single serialized string keeps Room's
 * schema free of type converters for List types and matches the pattern used
 * in `wallet_saved_cards` (nested List<Long> payloads).
 *
 * `id + clientId` is the composite PK — the same invoice `id` from Fineract
 * paired with the owning client so a global unique-id collision across clients
 * cannot conflate rows.
 *
 * @property id Fineract invoice datatable id (unique per client-list).
 * @property clientId Owning client id (the store key, doubles as page cursor).
 * @property consumerId Human-visible consumer id string.
 * @property consumerName Human-visible consumer name.
 * @property amount Signed invoice amount.
 * @property itemsBought Free-text purchase description.
 * @property status Fineract invoice-status code.
 * @property transactionId Human-visible transaction id string.
 * @property invoiceId Business invoice id (distinct from the row [id] datatable PK).
 * @property title Human-visible title.
 * @property date Server-provided date string (`YYYY-MM-DD` shape from Fineract).
 * @property createdAtJson JSON encoding of `List<Long>` — Fineract date parts.
 * @property updatedAtJson JSON encoding of `List<Long>` — Fineract date parts.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_invoices",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class InvoiceEntity(
    val id: Long,
    val clientId: Long,
    val consumerId: String,
    val consumerName: String,
    val amount: Double,
    val itemsBought: String,
    val status: Long,
    val transactionId: String,
    val invoiceId: Long,
    val title: String,
    val date: String,
    val createdAtJson: String,
    val updatedAtJson: String,
    val fetchedAtEpochMs: Long = 0L,
)
