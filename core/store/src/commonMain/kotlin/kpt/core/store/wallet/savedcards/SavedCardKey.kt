/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.savedcards

/**
 * Store5 key for the LEDGER `savedCards` store — one page of saved payment
 * cards per client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`SavedCardDao.replacePage(clientId, entities)`) AND as the API path
 * parameter (`GET /datatables/saved_cards/{clientId}`), so per-client scoping
 * is enforced end-to-end.
 *
 * @property clientId Client id the cached data belongs to.
 */
data class SavedCardKey(val clientId: Long)
