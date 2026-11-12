/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.standinginstruction

import kotlinx.serialization.json.Json
import kpt.core.model.standinginstruction.StandingInstruction
/**
 * Entity ↔ domain mapper for the `wallet_standing_instructions` table.
 *
 * `StandingInstruction` is a deeply nested `@Serializable` payload (five
 * distinct nested types plus a `List<Int>` for `recurrenceOnMonthDay`). The
 * whole payload is persisted as one JSON string column ([StandingInstructionEntity.payloadJson]);
 * only [StandingInstructionEntity.id] / `clientId` / [StandingInstructionEntity.name] /
 * `fetchedAtEpochMs` are pulled out as first-class columns for keyable /
 * queryable access. See [StandingInstructionEntity]'s KDoc for the rationale
 * (whole-payload read model, no per-field query need).
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [StandingInstruction] the app renders. */
fun StandingInstructionEntity.toDomain(): StandingInstruction =
    json.decodeFromString(StandingInstruction.serializer(), payloadJson)

/**
 * Convert a fetched domain [StandingInstruction] into a persistable row.
 *
 * The Fineract `getAllStandingInstructions(clientId)` endpoint scopes the list
 * to a specific client, so the caller passes the owning [clientId] from the
 * store key (parity with the `Beneficiary` mapper).
 *
 * Rows without a server id (only possible during a create round-trip; the
 * response echo carries an id) are coalesced to `id = 0L` defensively — in
 * practice, the store's writer only persists server-returned lists, so this
 * fallback never fires.
 *
 * @param clientId owning client id — the store's page key.
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun StandingInstruction.toEntity(clientId: Long, fetchedAtEpochMs: Long): StandingInstructionEntity =
    StandingInstructionEntity(
        id = id ?: 0L,
        clientId = clientId,
        name = name ?: "",
        payloadJson = json.encodeToString(StandingInstruction.serializer(), this),
        fetchedAtEpochMs = fetchedAtEpochMs,
    )
