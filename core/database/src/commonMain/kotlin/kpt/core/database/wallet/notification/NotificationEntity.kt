/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.notification

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single notification — the Room mirror of the
 * `kpt.core.model.notification.Notification` domain model.
 *
 * Stored in the `wallet_notifications` table. LEDGER shape — many rows per client.
 *
 * ## Cache-key scoping (no server-side per-client filter)
 *
 * The Fineract `notifications/?isRead=true` endpoint returns the list scoped to
 * the authenticated session (no `clientId` path or query parameter). This store
 * still keys on [clientId] via `NotificationKey(clientId)` so the DAO's per-client
 * page semantics prevent a logout-then-login as a different client from surfacing
 * the previous user's cached list. The mapper stamps the [clientId] into each
 * persisted row at write time (see `Notification.toEntity(clientId, ...)`).
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideNotificationStore` fetcher's writer lambda —
 * no user-facing write path ever calls `NotificationDao.upsert*` directly.
 *
 * ## Column mapping
 *
 * All columns are scalar. `createdAt` is stored as the server-provided String
 * (Fineract emits an ISO-like date-time string that the domain uses directly
 * via `toFormattedDateTime()` — no need to parse to epoch millis).
 *
 * `id + clientId` is the composite PK — the same notification `id` from Fineract
 * paired with the owning client so a global unique-id collision across clients
 * cannot conflate rows.
 *
 * @property id Fineract notification id (unique per session).
 * @property clientId Owning client id (the store key, doubles as page cursor;
 *   stamped by the mapper at write time — the server row does not carry it).
 * @property objectType Domain object type (e.g. "loan", "savings").
 * @property objectId Domain object id.
 * @property action Notification action code.
 * @property actorId Actor user id.
 * @property content Human-readable content.
 * @property isRead Read/unread flag from the server.
 * @property isSystemGenerated True if the notification was system-generated.
 * @property createdAt Server-provided created-at string (ISO-like).
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_notifications",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class NotificationEntity(
    val id: Long,
    val clientId: Long,
    val objectType: String,
    val objectId: Long,
    val action: String,
    val actorId: Long,
    val content: String,
    val isRead: Boolean,
    val isSystemGenerated: Boolean,
    val createdAt: String,
    val fetchedAtEpochMs: Long = 0L,
)
