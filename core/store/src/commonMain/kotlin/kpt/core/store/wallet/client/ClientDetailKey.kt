/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.client

/**
 * Store5 key for the SINGLE-ROW-PER-KEY `clientDetail` store — one client-info
 * record per key.
 *
 * The [clientId] doubles as the primary key at the DAO layer
 * (`ClientDetailDao.upsert(entity(id = clientId))`), so a fresh fetch for
 * client A never disturbs cached detail rows for client B (which arises for
 * an admin operator + client-picker flow, and simply for a logout-then-login
 * as a different client on the same device).
 *
 * @property clientId Client id the cached data belongs to.
 */
data class ClientDetailKey(val clientId: Long)
