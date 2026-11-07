/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.notification

/**
 * Store5 key for the LEDGER `notifications` store — one page of notifications
 * per authenticated client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`NotificationDao.replacePage(clientId, entities)`), so a logout-then-login
 * as a different client never disturbs the previous client's cached list.
 *
 * The Fineract `notifications/?isRead=true` endpoint does NOT take a
 * `clientId` path or query parameter (the list is scoped to the authenticated
 * session on the server side); the key is used purely for cache scoping, and
 * the mapper stamps the [clientId] into each persisted row at write time
 * (parity with `BeneficiaryKey`).
 *
 * @property clientId Client id the cached data belongs to.
 */
data class NotificationKey(val clientId: Long)
