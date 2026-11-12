/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.selfaccounts

/**
 * Store5 key for the LEDGER `selfAccounts` store — one page of the
 * authenticated user's own accounts per client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`SelfAccountDao.replacePage(clientId, entities)`) AND as the API path
 * parameter (`GET /clients/{clientId}/accounts?fields=savings`), so per-client
 * scoping is enforced end-to-end. A logout-then-login as a different client
 * therefore keys into a distinct cache slot; the previous client's rows sit
 * dormant in the table (with their `clientId` column) until [StoreCacheManager]
 * clears them on logout.
 *
 * @property clientId Client id the cached data belongs to.
 */
data class SelfAccountsKey(val clientId: Long)
