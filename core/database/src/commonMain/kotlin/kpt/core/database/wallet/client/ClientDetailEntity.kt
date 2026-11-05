/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.client

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single client-info record — the Room mirror of the
 * `org.mifospay.core.model.client.Client` domain model.
 *
 * Stored in the `wallet_client_details` table. One row per client
 * (SINGLE-ROW-PER-KEY archetype — the DAO's PK is [id] which doubles as the
 * store's page key).
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideClientDetailStore` fetcher's writer lambda —
 * no user-facing write path (`ClientRepository.updateClient` / `updateClientImage`)
 * ever calls `ClientDetailDao.upsert` directly. The server-echoed record appears
 * here via the next refresh cycle.
 *
 * ## Column mapping
 *
 * Scalar columns are stored directly. Three nested payloads are stored as JSON
 * strings (see [ClientDetailEntityMapper]):
 * - `activationDate: List<Long>` → [activationDateJson]
 * - `dateOfBirth: List<Long>` → [dateOfBirthJson]
 * - `timeline: ClientTimeline` (@Serializable) → [timelineJson]
 * - `status: ClientStatus` (@Serializable) → [statusJson]
 * - `legalForm: ClientStatus` (@Serializable) → [legalFormJson]
 *
 * Rationale: `Client` is a single-record snapshot the app renders whole; there
 * is no per-nested-field query. Same treatment as
 * `wallet_saving_account_details`.
 *
 * The client image path is NOT persisted here — it flows through its own
 * transitional `getClientImage` stream (blob URL response). Adding it later
 * would be a purely additive column bump.
 *
 * @property id Fineract client id — PK.
 * @property accountNo Human-visible client account number.
 * @property externalId External-system id.
 * @property active Active flag.
 * @property firstname / [lastname] / [displayName] Name parts.
 * @property mobileNo / [emailAddress] Contact details.
 * @property isStaff Staff flag.
 * @property officeId Owning office id.
 * @property officeName Owning office display name.
 * @property savingsProductName Default savings product name.
 * @property activationDateJson JSON encoding of `List<Long>` — Fineract date parts.
 * @property dateOfBirthJson JSON encoding of `List<Long>` — Fineract date parts.
 * @property timelineJson JSON encoding of `ClientTimeline`.
 * @property statusJson JSON encoding of `ClientStatus`.
 * @property legalFormJson JSON encoding of `ClientStatus` (legal form).
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_client_details",
)
data class ClientDetailEntity(
    @PrimaryKey val id: Long,
    val accountNo: String,
    val externalId: String,
    val active: Boolean,
    val firstname: String,
    val lastname: String,
    val displayName: String,
    val mobileNo: String,
    val emailAddress: String,
    val isStaff: Boolean,
    val officeId: Long,
    val officeName: String,
    val savingsProductName: String,
    val activationDateJson: String,
    val dateOfBirthJson: String,
    val timelineJson: String,
    val statusJson: String,
    val legalFormJson: String,
    val fetchedAtEpochMs: Long = 0L,
)
