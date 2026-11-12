/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.fork.di

import com.russhwolf.settings.Settings
import kpt.core.common.ForkDispatchers
import kpt.core.datastore.AutoPayPreferencesDataSource
import kpt.core.datastore.AutoPayPreferencesRepository
import kpt.core.datastore.AutoPayPreferencesRepositoryImpl
import kpt.core.datastore.BillerDataSource
import kpt.core.datastore.UserPreferencesDataSource
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin module providing Settings and the settings-backed preference data sources. */
val PreferencesModule = module {
    factory<Settings> { Settings() }
    // Use the IO dispatcher name - ForkDispatchers.IO.name
    factory { UserPreferencesDataSource(get(), get(named(ForkDispatchers.IO.name))) }
    factory { AutoPayPreferencesDataSource(get(), get(named(ForkDispatchers.IO.name))) }
    factory { BillerDataSource(get(), get(named(ForkDispatchers.IO.name))) }
    // BillDataSource intentionally NOT bound — Phase-4 Batch-A migrated the
    // OFFLINE_LOCAL_ONLY Bill store to Room (`kpt.core.database.wallet.bill.BillDao`).
    // The multiplatform-settings-backed `BillDataSource` + `BillRepositoryImpl` in
    // this module are dead code kept only for one release; delete in Phase-6 follow-up.
    // NOTE: `BillerDataSource` is still bound above because — until Phase-6 GC —
    // the old `BillerRepositoryImpl` class (multiplatform-settings-backed) is
    // reachable on the classpath and constructs a `BillerDataSource` via its
    // primary constructor if a caller ever creates it directly. In the wired
    // app path nothing instantiates it (see the DI cut below).

    // manage-pocket linkable-accounts Store5 migration:
    //  - Upstream PR #2057 (manage-pocket) shipped `PocketPreferencesDataSource`
    //    (multiplatform-settings-backed cache of pocket_accounts,
    //    detailed_pocket_accounts, linkable_accounts) as its offline-cache layer
    //    for the pocket read path.
    //  - This branch's Store5 architecture already served pocket_accounts +
    //    detailed_pocket_accounts through the `pocket` Store5 store (Phase-5
    //    Batch-2 — Room SoT); the linkable_accounts read is NOW served through
    //    the new `linkableAccounts` Store5 store (Room SoT — see
    //    `AppStoreRegistry.LinkableAccounts` + `provideLinkableAccountsStore`).
    //  - The `PocketPreferencesDataSource` binding + backing DataSource +
    //    entities + mapper are DELETED; `UserPreferencesRepositoryImpl` no
    //    longer depends on it (logout cache-drain is handled uniformly by
    //    `StoreCacheManager.clearAll()`, which drains every registered store
    //    including the new `linkableAccounts` store).

    single<AutoPayPreferencesRepository> {
        AutoPayPreferencesRepositoryImpl(
            autoPayPreferencesDataSource = get(),
            ioDispatcher = get(named(ForkDispatchers.IO.name)),
            unconfinedDispatcher = get(named(ForkDispatchers.Unconfined.name)),
        )
    }

    // BillRepository binding moved to `core/data/di/RepositoryModule.kt` —
    // Phase-4 Batch-A OFFLINE_LOCAL_ONLY Room-backed impl
    // (`kpt.core.data.repositoryImpl.BillOfflineRepositoryImpl`, GOAL D12).
    // BillerRepository binding moved to `core/data/di/RepositoryModule.kt` —
    // Phase-5 Batch-4 OFFLINE_LOCAL_ONLY Room-backed impl
    // (`kpt.core.data.repositoryImpl.BillerOfflineRepositoryImpl`, GOAL D12).
    // The old multiplatform-settings-backed `BillerRepositoryImpl` in this module
    // is dead code kept only for one release; delete in Phase-6 follow-up.
}
