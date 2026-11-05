/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOn
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.office.OfficesKey
import org.mifospay.core.common.ScreenStateStream
import org.mifospay.core.common.asScreenStateFlow
import org.mifospay.core.data.repository.OfficeRepository
import org.mifospay.core.model.office.Office
import org.mifospay.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

class OfficeRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-3 LEDGER wiring — injected by RepositoryModule so the
    // store-backed getOfficesScreen(...) can consume Store5 via asScreenStream(...).
    // Nullable-default so existing unit tests without the store harness continue
    // to compile; getOffices() — the legacy asScreenStateFlow path — is unaffected.
    private val officesStore: Store<OfficesKey, List<Office>>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
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
