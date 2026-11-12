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

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single account-transfer detail record — the Room mirror of the
 * `kpt.core.model.savingsaccount.TransferDetail` domain model.
 *
 * Stored in the `wallet_transfer_details` table. One row per transfer
 * (SINGLE-ROW-PER-KEY archetype — the DAO's PK is [transferId] which doubles as the
 * store's page key). Mirrors `SavingAccountDetailEntity`.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideTransferDetailStore` fetcher's writer lambda —
 * no user-facing write path ever calls `TransferDetailDao.upsert` directly. The
 * server-echoed record appears here via the next refresh cycle.
 *
 * ## Column mapping
 *
 * The domain `TransferDetail` is a `@Serializable` single-record snapshot the app
 * renders whole (there is no per-nested-field query the app performs), so the entire
 * payload is persisted as ONE JSON string column [payload] (see
 * `TransferDetailEntityMapper`) rather than as normalized relation tables. This is a
 * lighter variant of `SavingAccountDetailEntity`'s per-nested-field JSON columns —
 * `TransferDetail` has no scalar field the app filters on, so a single JSON blob
 * suffices.
 *
 * @property transferId Fineract account-transfer id — PK (unique per transfer).
 * @property payload JSON encoding of the whole `TransferDetail` domain model.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(tableName = "wallet_transfer_details")
data class TransferDetailEntity(
    @PrimaryKey val transferId: Long,
    val payload: String,
    val fetchedAtEpochMs: Long = 0L,
)
