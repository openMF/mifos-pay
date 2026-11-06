/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.common.ScreenState
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.NotificationRepository
import kpt.core.model.notification.Notification
import kpt.core.network.FineractApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.notification.NotificationKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

class NotificationRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-2 LEDGER wiring — injected by RepositoryModule so the
    // store-backed fetchNotificationsScreen(...) can consume Store5 via
    // asScreenStream(...). Nullable-default so existing unit tests without
    // the store harness continue to compile; fetchNotifications() — the legacy
    // asScreenStateFlow path — is unaffected.
    private val notificationStore: Store<NotificationKey, List<Notification>>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
) : NotificationRepository {
    override fun fetchNotifications(): Flow<ScreenState<List<Notification>>> {
        return apiManager.notificationApi
            .fetchNotifications(true)
            .map { it.pageItems }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-2 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three store-adapter
    // dependencies (notificationStore + NetworkMonitor + FetchedAtRepository).
    // If any is null (test wiring), we IllegalState — production DI in
    // RepositoryModule wires all three unconditionally.
    override fun fetchNotificationsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Notification>> {
        val store = checkNotNull(notificationStore) {
            "fetchNotificationsStream requires the `notification` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.Notification and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "fetchNotificationsStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "fetchNotificationsStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        // Template idiom: return the native ScreenDataStream directly — no
        // `.state.toForkScreenStateFlow()` bridge. The ViewModel exposes
        // `stream.state` to `ScreenContent` and `stream.refresh()` for retry.
        return store.asScreenStream(
            key = NotificationKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.Notification.of(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.NOTIFICATION,
        )
    }
}
