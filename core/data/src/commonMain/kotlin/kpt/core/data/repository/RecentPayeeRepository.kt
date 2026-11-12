/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.common.ScreenState
import kpt.core.model.account.RecentPayee
/**
 * Repository for fetching recent paid beneficiaries.
 * Derives recent payees from transaction history by:
 * 1. Fetching DEBIT transactions from savings account
 * 2. Getting transfer details for each transaction
 * 3. Extracting unique recipients sorted by most recent
 */
interface RecentPayeeRepository {

    /**
     * Phase-5 Batch-4 **store-backed derived read** for recent-payees
     * (GOAL D12 — LOCAL-DERIVED persist-on-derive variant of the
     * OFFLINE_LOCAL_ONLY archetype).
     *
     * Returns an offline-first `Flow<ScreenState<List<RecentPayee>>>` sourced
     * from the `recentPayee` Store5 offline store
     * ([`provideRecentPayeeStore`][kpt.core.store.wallet.recentpayee.provideRecentPayeeStore])
     * backed by the Room `wallet_recent_payees` table. On subscription the
     * repository kicks a background derive-and-persist coroutine (the classic
     * N+1 walk over savings-with-associations + per-transfer detail), which
     * writes the freshly-derived list via
     * `notifyingWrite("wallet_recent_payees") { dao.replacePage(sourceAccountId, rows) }`.
     *
     * ### Cache-first semantics
     *
     * The Room reader emits IMMEDIATELY if the table has cached rows for
     * [accountId] — no N+1 walk needed to render the screen. The background
     * derive fires in parallel and its `notifyingWrite` re-emits fresh data
     * once complete. First-time (cold) subscribers see a brief `Loading` →
     * `Empty` → `Content` sequence; warm subscribers see cache
     * `Content` immediately (and a refreshed `Content` a moment later).
     *
     * ### Limit
     *
     * [limit] is a client-side display cap applied AFTER the DAO emission
     * (see `RecentPayeeKey` KDoc for why limit is not part of the store key).
     * Pass `10` for the intra-bank hub's default cap.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. The derive kick is a local computation over
     * ALREADY-FETCHED transaction history — it never invents new server
     * data. There is no bookkeeper.
     *
     * @param accountId Owning source-savings-account id (the store's page key).
     * @param limit Maximum number of recent payees to return.
     * @param scope Coroutine scope for the background derive-and-persist
     *   coroutine (typically `viewModelScope`).
     */
    fun getRecentPayeesScreen(
        accountId: Long,
        limit: Int,
        scope: CoroutineScope,
    ): Flow<ScreenState<List<RecentPayee>>>
}
