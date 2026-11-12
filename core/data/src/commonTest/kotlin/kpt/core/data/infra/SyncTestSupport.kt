/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.infra

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kpt.core.base.data.infra.Synchronizer
import kpt.core.base.datastore.infra.ChangeListVersions
import kpt.core.base.store.infra.FetchedAtRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Minimal in-memory [Synchronizer] for repository `syncWith` tests. */
internal class RecordingSynchronizer : Synchronizer {
    var stored = ChangeListVersions()
        private set

    override suspend fun getChangeListVersions(): ChangeListVersions = stored

    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        stored = stored.update()
    }
}

/** In-memory [FetchedAtRepository] — mirrors the production contract without Room. */
@OptIn(ExperimentalTime::class)
internal class InMemoryFetchedAtRepository : FetchedAtRepository {
    private val map = mutableMapOf<String, Instant>()
    override suspend fun read(storeKey: String): Instant? = map[storeKey]
    override suspend fun write(storeKey: String, instant: Instant) {
        map[storeKey] = instant
    }
}

/** Always-online [NetworkMonitor] fake (copied from core-base/store's test pattern). */
internal fun onlineNetworkMonitor(): NetworkMonitor = object : NetworkMonitor {
    override val networkStatus: StateFlow<NetworkStatus> =
        MutableStateFlow(NetworkStatus.Available(NetworkInfo(type = NetworkType.WiFi, isMetered = false))).asStateFlow()
    override val isOnline: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    override val networkChanges: SharedFlow<NetworkChangeEvent> =
        MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()
    override fun close() = Unit
}
