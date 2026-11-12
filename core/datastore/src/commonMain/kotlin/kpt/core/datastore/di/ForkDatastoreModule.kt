/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.di

import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.common.ForkDispatchers
import kpt.core.datastore.UserPreferencesDataSource
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.datastore.prefs.ForkPreferencesRepositoryImpl
import kpt.core.datastore.prefs.UserPreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * THE FORK'S datastore DI seam. Empty on the neutral template — this is yours to fill.
 *
 * `DatastoreModule` beside it is `owner: template` and FULL-COPIES on a sync, so a binding added
 * there is replaced on the next adopt. This file is `owner: fork` and is never copied.
 *
 * Binds [ForkPreferencesRepository] — the fork's preferences, extending the framework's. The
 * delegate is the template's own implementation, bound by `DatastoreModule`, so every framework
 * preference is available through this one type without re-implementing any of them.
 */
val ForkDatastoreModule = module {
    single<ForkPreferencesRepository> {
        ForkPreferencesRepositoryImpl(
            delegate = get<UserPreferencesRepository>(),
            // The SAME instances the framework's impl uses — an ownership boundary, not a second
            // store. Namespace fork keys so they cannot collide with a future framework preference.
            plainSettings = get<Settings>(named("plain")),
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get<DispatcherManager>(),
            preferenceManager = get<UserPreferencesDataSource>(),
            ioDispatcher = get(named(ForkDispatchers.IO.name)),
            unconfinedDispatcher = get(named(ForkDispatchers.Unconfined.name)),
        ).also { prefs ->
            // A session signed in before the wallet wrote its token to the framework's store too: copy
            // it once, so core-base's auth bridge — which reads the framework's store — keeps sending it.
            val framework = get<UserPreferencesRepository>()
            val walletToken = prefs.authToken
            if (framework.authToken == null && walletToken != null) {
                get<DispatcherManager>().appScope.launch { framework.setAuthToken(walletToken) }
            }
        }
    }
}
