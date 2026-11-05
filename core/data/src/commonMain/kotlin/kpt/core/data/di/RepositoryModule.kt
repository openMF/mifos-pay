/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kpt.core.base.common.di.CommonModule
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.infra.impl.RoomFetchedAtRepository
import kpt.core.data.user.UserDataRepository
import kpt.core.data.user.UserLogoutManager
import kpt.core.data.user.impl.UserDataRepositoryImpl
import kpt.core.data.user.impl.UserLogoutManagerImpl
import kpt.core.database.AppDatabase
import kpt.core.database.di.DatabaseModule
import kpt.core.datastore.di.DatastoreModule
import kpt.core.datastore.di.KptUserPreferencesQualifier
import kpt.core.network.di.NetworkModule
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * DataModule — the INFRA-ONLY (framework) data aggregator, `owner: template` (E1 / C1).
 *
 * The demo repositories / outboxes / offline-submit syncers relocated to the fork-owned
 * [kpt.core.data.demo.di.ProjectRepositoryModule]; this aggregator now carries ZERO `kpt.core.*.demo.*`
 * imports so a template sync can blind-copy it without re-introducing demo wiring a fork already
 * stripped. The demo module is installed via the fork-owned `FeatureRegistry.featureKoinModules`
 * demo block; both go away together on `customize.sh --clean`.
 */
val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    single<NetworkMonitor> { NetworkMonitorProvider.install() }
    single<UserDataRepository> { UserDataRepositoryImpl(get(KptUserPreferencesQualifier)) }

    // Framework FetchedAtRepository — durable lastFetchedAt persistence backing
    // DataFreshnessIndicator timestamps. Room-only by design (no in-memory fallback).
    single<FetchedAtRepository> { RoomFetchedAtRepository(get<AppDatabase>().fetchedAtDao) }

    // Framework DraftDao — backing store for SubmitOutbox / DraftSubmitHandler
    single { get<AppDatabase>().draftDao }

    // App-scoped CoroutineScope for cross-VM long-running coroutines (framework infra).
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(KptUserPreferencesQualifier), get(), get()) }
}

expect val platformModule: Module
