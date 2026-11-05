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

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single standing instruction — the Room mirror of the
 * `org.mifospay.core.model.standinginstruction.StandingInstruction` domain model.
 *
 * Stored in the `wallet_standing_instructions` table. LEDGER shape — many rows
 * per client, page-keyed by [clientId]. The Fineract standing-instruction
 * endpoint (`GET /standinginstructions?clientId=<clientId>`) already scopes the
 * server response to a specific client, so [clientId] is both the API query
 * parameter AND the store's cache key.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideStandingInstructionStore` fetcher's writer
 * lambda — no user-facing write path (`StandingInstructionRepository.createStandingInstruction`
 * / `updateStandingInstruction` / `deleteStandingInstruction`) ever calls
 * `StandingInstructionDao.upsert*` directly. Server-echoed rows appear here on
 * the next refresh cycle.
 *
 * ## Column mapping
 *
 * `StandingInstruction` is a deeply nested `@Serializable` payload — five
 * distinct nested types (`FromAndToOffice`, `FromAndToClient`, `FromAndToAccount`
 * twice for from/to, `Option` twice for priority/status) plus a `List<Int>` for
 * `recurrenceOnMonthDay`. Rather than flat-pack each field (which would produce
 * ~30 nullable columns and duplicate the model shape on the SQLite side), the
 * whole `@Serializable` payload is JSON-encoded into a single `payloadJson`
 * column. Only [id], [clientId], sort-key [name], and observability
 * [fetchedAtEpochMs] are pulled out as first-class columns. This mirrors the
 * pattern used by `wallet_client_details` for its 3 nested `@Serializable`
 * payloads (`ClientTimeline` + two `ClientStatus`); the difference is that
 * `Client`'s scalars are still queryable — for `StandingInstruction` we never
 * query individual scalars (the SI list screen renders whole rows), so pushing
 * the whole payload into one column is safe and materially simpler.
 *
 * The domain model's [id] is `Long?` (nullable — server-generated on create).
 * Cache rows are only written for server-returned records (which always carry a
 * non-null `id`), so we store [id] as a non-null `Long` here; the mapper
 * coalesces the incoming nullable to 0 defensively but the DAO's page write
 * never actually sees a null-id row in practice.
 *
 * @property id Fineract standing-instruction id (unique per client-list).
 * @property clientId Owning client id (the store key, doubles as page cursor).
 * @property name Instruction name (nullable in the domain — coalesced to "" for
 *   sorting) — kept as a first-class column so the DAO can `ORDER BY name`.
 * @property payloadJson JSON encoding of the whole `StandingInstruction`
 *   @Serializable payload.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_standing_instructions",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class StandingInstructionEntity(
    val id: Long,
    val clientId: Long,
    val name: String,
    val payloadJson: String,
    val fetchedAtEpochMs: Long = 0L,
)
