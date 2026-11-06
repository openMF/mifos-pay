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

import kpt.core.model.notification.Notification
/**
 * Entity ↔ domain mapper for the `wallet_notifications` LEDGER table.
 *
 * All columns are scalar; no JSON encoding is required.
 */

/** Convert a persisted row back into the domain [Notification] the app renders. */
fun NotificationEntity.toDomain(): Notification = Notification(
    id = id,
    objectType = objectType,
    objectId = objectId,
    action = action,
    actorId = actorId,
    content = content,
    isRead = isRead,
    isSystemGenerated = isSystemGenerated,
    createdAt = createdAt,
)

/**
 * Convert a fetched domain [Notification] into a persistable row.
 *
 * The Fineract `notifications/?isRead=true` endpoint does NOT round-trip a
 * `clientId` per row (the list is scoped to the authenticated session), so the
 * caller passes the owning [clientId] from the store key (parity with the
 * `Beneficiary` mapper).
 *
 * @param clientId owning client id — the store's page key.
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Notification.toEntity(clientId: Long, fetchedAtEpochMs: Long): NotificationEntity =
    NotificationEntity(
        id = id,
        clientId = clientId,
        objectType = objectType,
        objectId = objectId,
        action = action,
        actorId = actorId,
        content = content,
        isRead = isRead,
        isSystemGenerated = isSystemGenerated,
        createdAt = createdAt,
        fetchedAtEpochMs = fetchedAtEpochMs,
    )
