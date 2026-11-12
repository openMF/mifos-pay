/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.transferdetail

import kotlinx.serialization.json.Json
import kpt.core.model.savingsaccount.TransferDetail
/**
 * Entity ↔ domain mapper for the `wallet_transfer_details` table.
 *
 * The domain `TransferDetail` is a single `@Serializable` record the app renders
 * whole, so the entire model is persisted as ONE JSON string ([TransferDetailEntity.payload])
 * — a lighter variant of `SavingAccountDetailEntityMapper`'s per-nested-field JSON
 * columns (`TransferDetail` has no scalar field the app queries on).
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [TransferDetail] the app renders. */
fun TransferDetailEntity.toDomain(): TransferDetail =
    json.decodeFromString(TransferDetail.serializer(), payload)

/**
 * Convert a fetched domain [TransferDetail] into a persistable row.
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun TransferDetail.toEntity(fetchedAtEpochMs: Long): TransferDetailEntity =
    TransferDetailEntity(
        transferId = id,
        payload = json.encodeToString(TransferDetail.serializer(), this),
        fetchedAtEpochMs = fetchedAtEpochMs,
    )
