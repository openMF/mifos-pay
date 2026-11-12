/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.di

import com.russhwolf.settings.Settings
import kpt.core.base.common.di.CommonModule
import kpt.core.base.datastore.di.DatastoreBaseModule
import kpt.core.base.datastore.infra.SettingsSyncStatePersister
import kpt.core.base.datastore.infra.SyncStatePersister
import kpt.core.datastore.prefs.AppReviewPromptStore
import kpt.core.datastore.prefs.SettingsAppReviewPromptStore
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for `core/datastore` — the fork's preference stores, over the plain and secure
 * `Settings` instances that `DatastoreBaseModule` binds.
 *
 * Preferences only. Room-backed rows belong in `core/database`; conflating the two is how a value
 * that must survive a reinstall ends up in storage that does not.
 */
val DatastoreModule = module {
    includes(CommonModule, DatastoreBaseModule)

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get(),
        )
    } bind UserPreferencesRepository::class

    // Review-prompt counters — Settings-backed, DEVICE-scoped (survives sign-out, unlike user prefs).
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
    single<SyncStatePersister> {
        SettingsSyncStatePersister(plainSettings = get<Settings>(named("plain")))
    }
}
