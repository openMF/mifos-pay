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

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kpt.core.model.savedcards.SavedCard
/**
 * Entity ↔ domain mapper for the `wallet_saved_cards` LEDGER table.
 *
 * `createdAt` and `updatedAt` (both `List<Long>` — Fineract date parts) are
 * persisted as JSON string columns. See [SavedCardEntity]'s KDoc for the
 * rationale.
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [SavedCard] the app renders. */
fun SavedCardEntity.toDomain(): SavedCard = SavedCard(
    id = id,
    clientId = clientId,
    firstName = firstName,
    lastName = lastName,
    cardNumber = cardNumber,
    cvv = cvv,
    expiryDate = expiryDate,
    backgroundColor = backgroundColor,
    createdAt = json.decodeFromString(ListSerializer(Long.serializer()), createdAtJson),
    updatedAt = json.decodeFromString(ListSerializer(Long.serializer()), updatedAtJson),
)

/**
 * Convert a fetched domain [SavedCard] into a persistable row.
 *
 * The domain's [SavedCard.clientId] is authoritative — the store key's
 * `clientId` is the same value (the API path parameter), so there is no
 * separate `clientId` arg here (unlike [Beneficiary] whose API row does not
 * carry a `clientId` field).
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun SavedCard.toEntity(fetchedAtEpochMs: Long): SavedCardEntity = SavedCardEntity(
    id = id,
    clientId = clientId,
    firstName = firstName,
    lastName = lastName,
    cardNumber = cardNumber,
    cvv = cvv,
    expiryDate = expiryDate,
    backgroundColor = backgroundColor,
    createdAtJson = json.encodeToString(ListSerializer(Long.serializer()), createdAt),
    updatedAtJson = json.encodeToString(ListSerializer(Long.serializer()), updatedAt),
    fetchedAtEpochMs = fetchedAtEpochMs,
)
