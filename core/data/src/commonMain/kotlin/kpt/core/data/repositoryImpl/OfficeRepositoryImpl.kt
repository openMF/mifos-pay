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
import kotlinx.coroutines.flow.flowOn
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.common.ScreenStateStream
import kpt.core.common.asScreenStateFlow
import kpt.core.data.repository.OfficeRepository
import kpt.core.model.office.Office
import kpt.core.network.SelfServiceApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.office.OfficesKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

/** Default [OfficeRepository] backed by the office API. */
@RepositoryBinding(binds = OfficeRepository::class)
class OfficeRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Offices) private val officesStore: Store<OfficesKey, List<Office>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : OfficeRepository {
    override fun getOffices(): ScreenStateStream<List<Office>> {
        return apiManager.officeApi.getOffices()
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three
    // store-adapter dependencies (officesStore + NetworkMonitor +
    // FetchedAtRepository). If any is null (test wiring), we IllegalState —
    // production DI in RepositoryModule wires all three unconditionally.
    override fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>> {
        val store = checkNotNull(officesStore) {
            "getOfficesStream requires the `offices` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.Offices and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getOfficesStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getOfficesStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = OfficesKey,
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.Offices.ALL,
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.OFFICES,
        )
    }
}
