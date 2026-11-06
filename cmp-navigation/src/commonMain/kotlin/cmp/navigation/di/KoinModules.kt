/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.di

import cmp.navigation.AppViewModel
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarNavigationViewModel
import cmp.navigation.privacy.PrivacyConsentApplier
import cmp.navigation.registry.ForkFeatureRegistry
import cmp.navigation.rootnav.RootNavViewModel
import kpt.core.base.common.di.CommonModule
import kpt.core.base.firebase.di.firebaseModule
import kpt.core.base.platform.di.platformModule
import kpt.core.base.security.di.SecurityModule
import kpt.core.data.di.DataModule
import kpt.core.database.di.DatabaseModule
import kpt.core.datastore.di.DatastoreModule
import kpt.core.store.di.appStoreModule
import kpt.sync.di.SyncModule
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Every Koin module the app installs, in one place — what the platform entry points hand to `startKoin`. */
object KoinModules {
    private val dataModule = module {
        includes(DataModule, appStoreModule)
    }

    private val dispatcherModule = module {
        includes(CommonModule)
    }

    private val AppModule = module {
        includes(platformModule)

        viewModelOf(::AppViewModel)
        viewModelOf(::AuthenticatedNavbarNavigationViewModel)
        viewModelOf(::RootNavViewModel)

        // Applies the stored analytics consent to the SDK; started once by initKoin.
        singleOf(::PrivacyConsentApplier)
    }

    private val featureModule = module {
        // No feature module is listed here any more. Every one — including the backbone's
        // HomeModule and SettingsModule — is picked up by GeneratedFeatureKoinBindings and reaches
        // Koin through ForkFeatureRegistry.featureKoinModules.
        includes(ForkFeatureRegistry.featureKoinModules)
    }

    /** The full module list. Order is irrelevant to Koin but kept layer-by-layer so a missing module is visible. */
    val allModules = listOf(
        SecurityModule,
        dataModule,
        DatabaseModule,
        dispatcherModule,
        firebaseModule,
        DatastoreModule,
        featureModule,
        AppModule,
        SyncModule,
    )
}
