/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kpt.core.base.common.di.CommonModule
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.data.user.UserLogoutManager
import kpt.core.data.user.impl.UserLogoutManagerImpl
import kpt.core.database.di.DatabaseModule
import kpt.core.datastore.di.DatastoreModule
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.network.di.NetworkModule
import kpt.core.store.prefs.impl.UserDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * DataModule — the INFRA-ONLY (framework) data aggregator, `owner: template` (E1 / C1).
 *
 * The repositories, outboxes and offline-submit syncers are no longer wired here OR in a
 * `Demo*Module`: each is declared by annotation next to the code it belongs to (`@RepositoryBinding`
 * on an implementation, `@DataProvider` on a factory function) and generated into
 * [GeneratedRepositoryBindings]. This aggregator therefore carries ZERO domain imports, so a
 * template sync can blind-copy it — and a stripped fork simply generates fewer bindings, because
 * deleting a package takes its annotations with it. There is no module left to unregister.
 */
val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    // Every repository's Koin binding, GENERATED from `@RepositoryBinding` on the implementation.
    // Emitted into this same package, so this file needs no import and keeps its zero-demo-reference
    // property — which is what lets a template sync blind-copy it. A stripped fork simply generates
    // fewer bindings, because the deleted implementations take their annotations with them.
    includes(GeneratedRepositoryBindings)

    single<NetworkMonitor> { NetworkMonitorProvider.install() }
    // Binds the read PORT declared by core/store — core/store cannot depend on core/datastore,
    // so this module (which owns UserPreferencesRepository) supplies the preferences flow.
    single<UserDataSource> { UserDataSource { get<UserPreferencesRepository>().userData } }
    // Framework FetchedAtRepository — durable lastFetchedAt persistence backing
    // DataFreshnessIndicator timestamps. Room-only by design (no in-memory fallback).

    // Framework DraftDao — backing store for SubmitOutbox / DraftSubmitHandler
    // Framework BookkeeperDao — backing store for the MutableStore retry ledger.

    // App-scoped CoroutineScope for cross-VM long-running coroutines (framework infra).
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
 * common code.
 */
expect val platformModule: Module
