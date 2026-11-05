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

// ── Fork DI-surface import notes (consolidated ABOVE the import list so ktlint
//    standard:import-ordering stays green — comments must not interrupt the sorted block):
//  • org.mifos.authenticator.passcode.PasscodeManager — required by AppViewModel +
//    PlatformAuthenticatorGate (see MifosPasscodeModule below; PasscodeStorageAdapter ctor
//    arg bound by the fork RepositoryModule).
//  • org.mifos.feature.passcode.MifosAuthenticatorModule — :feature:passcode ViewModel bindings
//    (re-auth passcode + biometric-setup destinations).
//  • ForkDispatchersModule (aliased DispatchersModule) + stringProviderModule — core:common
//    (named MifosDispatchers.* qualifiers + resource-id → string resolution).
//  • RepositoryModule — AppLockRepository / BiometricStorageAdapter / PasscodeStorageAdapter +
//    every fork repository impl. PreferencesModule — UserPreferencesRepository (coexists with
//    kpt DatastoreModule). DomainModule — fork use-cases.
//  • LocalModule / NetworkModule — Supabase InstanceConfigLoader + Ktor client + KtorInterceptor.
//  • cmp.navigation.registry.ForkFeatureRegistry — adopted at the 90ff6da6 template sync (template feature
//    Koin surface + generated feature bindings); the fork-specific modules below stay wired explicitly
//    alongside it.
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
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.feature.passcode.MifosAuthenticatorModule
import org.mifospay.core.common.di.stringProviderModule
import org.mifospay.core.data.di.RepositoryModule
import org.mifospay.core.datastore.di.PreferencesModule
import org.mifospay.core.domain.di.DomainModule
import org.mifospay.core.network.di.LocalModule
import org.mifospay.core.network.di.NetworkModule
import org.mifospay.core.common.di.DispatchersModule as ForkDispatchersModule

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

    // ── Fork Koin surface (Phase 2 T6/T7 — see `cmp-navigation/build.gradle.kts`
    // comments + `AppViewModel` KDoc). Grouped by layer so this reads like a
    // mirror of `cmp-shared/.../org/mifospay/shared/di/KoinModules.kt`, which
    // is the AUTHORITATIVE fork-side module list.
    // ──────────────────────────────────────────────────────────────────────
    private val forkCommonModule = module {
        includes(stringProviderModule, ForkDispatchersModule)
    }

    private val forkNetworkModule = module {
        includes(LocalModule, NetworkModule)
    }

    private val forkDataModule = module {
        includes(RepositoryModule)
    }

    private val forkDomainModule = module {
        includes(DomainModule)
    }

    private val forkFeatureModule = module {
        includes(MifosAuthenticatorModule)
    }

    /**
     * Provides the fork's passcode-library [PasscodeManager] as a process-wide
     * singleton — required by [AppViewModel] (background-lock re-auth gate
     * reads `passcodeManager.state.value.passcodeStep`) and every passcode
     * screen composed downstream.
     */
    private val MifosPasscodeModule = module {
        single { PasscodeManager(get()) }
    }

    /** The full module list. Order is irrelevant to Koin but kept layer-by-layer so a missing module is visible. */
    val allModules = listOf(
        SecurityModule,
        dataModule,
        DatabaseModule,
        dispatcherModule,
        firebaseModule,
        DatastoreModule,
        // Fork's UserPreferencesRepository / AutoPay / Bill / Biller bindings.
        PreferencesModule,
        featureModule,
        AppModule,
        SyncModule,
        // ── Fork Koin surface (Phase 2 T6/T7) ───────────────────────────────
        forkCommonModule,
        forkNetworkModule,
        forkDataModule,
        forkDomainModule,
        forkFeatureModule,
        MifosPasscodeModule,
    )
}
