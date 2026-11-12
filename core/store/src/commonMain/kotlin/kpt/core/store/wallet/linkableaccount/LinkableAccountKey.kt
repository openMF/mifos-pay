/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.linkableaccount

/**
 * Store5 key for the LEDGER `linkableAccounts` store — one page of accounts
 * eligible to link into the client's pocket, per authenticated client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`LinkableAccountDao.replacePage(clientId, entities)`) AND as the API path
 * parameter for the source client-accounts fetch
 * (`GET /clients/{clientId}/accounts`), so per-client scoping is enforced
 * end-to-end.
 *
 * ## Derived cache — filter runs inside the fetcher
 *
 * The linkable-accounts list is DERIVED (client-accounts minus already-linked
 * pockets). The store's fetcher snapshots the already-linked set from
 * `PocketDao.observeByClient(clientId).first()` at fetch time, runs the
 * per-account-type filter + share-market-price enrichment inline, and hands
 * the FILTERED result to the writer. On link/delink the ManagePocket VM
 * re-emits its `refreshTrigger`, which re-subscribes both `pocket` +
 * `linkableAccounts` streams — the fresh fetch on the linkable store then
 * observes the updated linked set (via the DAO snapshot) and emits a fresh
 * filtered page.
 *
 * ## Migration note
 *
 * This store REPLACES upstream PR #2057's multiplatform-settings
 * `linkable_accounts` cache in `PocketPreferencesDataSource` (JSON encoded
 * `List<LinkableAccountEntity>` via kotlinx.serialization). This branch's
 * Store5 architecture (Room SoT + CACHE_FIRST_SWR) serves the same read
 * offline-first without reintroducing an in-memory / prefs cache.
 *
 * @property clientId Client id the cached data belongs to.
 */
data class LinkableAccountKey(val clientId: Long)
