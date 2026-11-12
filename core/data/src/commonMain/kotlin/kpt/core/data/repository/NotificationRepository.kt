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
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.common.ScreenState
import kpt.core.model.notification.Notification
/** Repository for user notifications. */
interface NotificationRepository {
    // Phase-3 cutover: reads now return Flow<ScreenState<T>> per the CORE_DATA
    // recipe. Callers migrate via BaseViewModel.observeScreen / stateInAsScreen.
    /** Streams the user's notifications. */
    fun fetchNotifications(): Flow<ScreenState<List<Notification>>>

    /**
     * Phase-5 Batch-2 **LEDGER read** for the `notification` archetype (GOAL D13) —
     * returns an offline-first `Flow<ScreenState<List<Notification>>>` consumed
     * through the `notification` Store5
     * [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + atomic
     * [`replacePage`][kpt.core.database.wallet.notification.NotificationDao.replacePage]
     * writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream] — the
     * Store5-native bridge, distinct from the transitional
     * [`asScreenStateFlow`][kpt.core.common.asScreenStateFlow] the
     * legacy [fetchNotifications] path uses.
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default) — subscribers see the cached page instantly and a
     * background revalidation fires on the Stale/VeryStale band edge.
     *
     * ### Cache-key scoping
     *
     * The Fineract `notifications/?isRead=true` endpoint returns the list for
     * the authenticated session (no `clientId` parameter). The [clientId] here
     * is used purely for CACHE SCOPING — the store key partitions the Room SoT
     * so a logout-then-login as a different client never surfaces the previous
     * user's cached list.
     *
     * Returns the native [ScreenDataStream] (template idiom) — the ViewModel holds
     * it directly and exposes `stream.state` to the Screen (rendered by
     * `core-base/ui` `ScreenContent`) and `stream.refresh()` for pull-to-refresh /
     * retry. No fork-`ScreenState` bridge, no per-ViewModel `when` fold —
     * `DecisionEngine` inside the stream pre-decides every render branch.
     *
     * @param clientId owning client id — the store's page key.
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun fetchNotificationsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Notification>>
}
