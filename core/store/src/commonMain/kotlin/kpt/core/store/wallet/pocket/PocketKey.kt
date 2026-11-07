/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.pocket

/**
 * Store5 key for the LEDGER `pockets` store — one page of linked pocket
 * accounts per client.
 *
 * The [clientId] doubles as the page key at the DAO layer
 * (`PocketDao.replacePage(clientId, entities)`) AND as the API path parameter
 * for the client-accounts join (`GET /clients/{clientId}/accounts`), so
 * per-client scoping is enforced end-to-end.
 *
 * The Fineract `pocketApi.getPocketAccounts()` endpoint does NOT take a
 * `clientId` parameter — the list is scoped to the authenticated session on
 * the server side — but the derivation of `DetailedPocketAccount.balance` /
 * `productName` / `status` / `currencyCode` depends on the client-accounts
 * join, which IS per-client. So the store key is still `clientId` for cache
 * scoping + for the join.
 *
 * ## Migration note
 *
 * This store REPLACES the in-memory `MutableStateFlow<DataState<List<DetailedPocketAccount>>>`
 * cache in `PocketRepositoryImp` (`detailedPocketCache`) that was
 * Phase-4-deferred (see the pre-migration `TODO(phase-4)` comment on
 * `PocketRepositoryImp`). Room becomes the SoT.
 *
 * @property clientId Client id the cached data belongs to.
 */
data class PocketKey(val clientId: Long)
