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

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kpt.core.model.datatables.invoice.Invoice
/**
 * Entity ↔ domain mapper for the `wallet_invoices` LEDGER table.
 *
 * `createdAt` and `updatedAt` (both `List<Long>` — Fineract date parts) are
 * persisted as JSON string columns. See [InvoiceEntity]'s KDoc for the
 * rationale.
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [Invoice] the app renders. */
fun InvoiceEntity.toDomain(): Invoice = Invoice(
    id = id,
    clientId = clientId,
    consumerId = consumerId,
    consumerName = consumerName,
    amount = amount,
    itemsBought = itemsBought,
    status = status,
    transactionId = transactionId,
    invoiceId = invoiceId,
    title = title,
    date = date,
    createdAt = json.decodeFromString(ListSerializer(Long.serializer()), createdAtJson),
    updatedAt = json.decodeFromString(ListSerializer(Long.serializer()), updatedAtJson),
)

/**
 * Convert a fetched domain [Invoice] into a persistable row.
 *
 * The domain's [Invoice.clientId] is authoritative — the store key's
 * `clientId` is the same value (the API path parameter), so there is no
 * separate `clientId` arg here (unlike [Beneficiary] whose API row does not
 * carry a `clientId` field).
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Invoice.toEntity(fetchedAtEpochMs: Long): InvoiceEntity = InvoiceEntity(
    id = id,
    clientId = clientId,
    consumerId = consumerId,
    consumerName = consumerName,
    amount = amount,
    itemsBought = itemsBought,
    status = status,
    transactionId = transactionId,
    invoiceId = invoiceId,
    title = title,
    date = date,
    createdAtJson = json.encodeToString(ListSerializer(Long.serializer()), createdAt),
    updatedAtJson = json.encodeToString(ListSerializer(Long.serializer()), updatedAt),
    fetchedAtEpochMs = fetchedAtEpochMs,
)
