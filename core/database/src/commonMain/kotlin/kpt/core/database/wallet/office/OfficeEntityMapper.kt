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

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kpt.core.model.office.Office
/**
 * Entity ↔ domain mapper for the `wallet_offices` table.
 *
 * Only `openingDate: List<Int>?` is JSON-encoded to keep the schema free of
 * type converters for List types (same pattern as `wallet_client_details`'s
 * `activationDateJson`); every other field is a plain scalar column.
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }
private val openingDateSerializer = ListSerializer(Int.serializer()).nullable

/** Convert a persisted row back into the domain [Office] the app renders. */
fun OfficeEntity.toDomain(): Office = Office(
    id = id,
    name = name,
    nameDecorated = nameDecorated,
    externalId = externalId,
    openingDate = json.decodeFromString(openingDateSerializer, openingDateJson),
    hierarchy = hierarchy,
)

/**
 * Convert a fetched domain [Office] into a persistable row.
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Office.toEntity(fetchedAtEpochMs: Long): OfficeEntity = OfficeEntity(
    id = id,
    name = name,
    nameDecorated = nameDecorated,
    externalId = externalId,
    openingDateJson = json.encodeToString(openingDateSerializer, openingDate),
    hierarchy = hierarchy,
    fetchedAtEpochMs = fetchedAtEpochMs,
)
