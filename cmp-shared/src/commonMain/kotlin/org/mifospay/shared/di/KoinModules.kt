/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.shared.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.feature.passcode.MifosAuthenticatorModule
import org.mifos.lib.loan.di.LoanApplicationModule
import org.mifospay.core.common.di.DispatchersModule
import org.mifospay.core.common.di.stringProviderModule
import org.mifospay.core.data.di.RepositoryModule
import org.mifospay.core.datastore.di.PreferencesModule
import org.mifospay.core.domain.di.DomainModule
import org.mifospay.core.network.di.LocalModule
import org.mifospay.core.network.di.NetworkModule
import org.mifospay.feature.accounts.di.AccountsModule
import org.mifospay.feature.auth.di.AuthModule
import org.mifospay.feature.autopay.di.AutoPayModule
import org.mifospay.feature.beneficiary.di.BeneficiaryModule
import org.mifospay.feature.editpassword.di.EditPasswordModule
import org.mifospay.feature.faq.di.FaqModule
import org.mifospay.feature.fastmpay.di.FastMpayModule
import org.mifospay.feature.history.di.HistoryModule
import org.mifospay.feature.home.di.MifosHomeModule
import org.mifospay.feature.invoices.di.InvoicesModule
import org.mifospay.feature.kyc.di.KYCModule
import org.mifospay.feature.merchants.di.MerchantsModule
import org.mifospay.feature.mpay.qr.di.MpayQrModule
import org.mifospay.feature.mpay.qr.scan.di.MpayQrScanModule
import org.mifospay.feature.notification.di.NotificationModule
import org.mifospay.feature.payments.di.PaymentsModule
import org.mifospay.feature.pocket.di.PocketModule
import org.mifospay.feature.profile.di.ProfileModule
import org.mifospay.feature.receipt.di.ReceiptModule
import org.mifospay.feature.savedcards.di.SavedCardsModule
import org.mifospay.feature.send.money.di.SendMoneyModule
import org.mifospay.feature.settings.di.SettingsModule
import org.mifospay.feature.standing.instruction.di.StandingInstructionModule
import org.mifospay.feature.transfer.interbank.di.interbankTransferModule
import org.mifospay.feature.transfer.intrabank.di.IntraBankModule
import org.mifospay.feature.upi.setup.di.UpiSetupModule
import org.mifospay.shared.MifosPayViewModel
import org.mifospay.shared.TransferOptionsViewModel
import org.mifospay.shared.instance.InstanceSelectorViewModel

/**
 * Aggregator object that bundles every Koin module the app needs. Consumed by
 * [koinConfiguration] (test/preview) and [initKoin] (production app start).
 *
 * The passcode/biometrics surface is wired across three of the included
 * modules:
 *  - [RepositoryModule] (in `:core:data`) binds `MifosPasscodeAdapterImpl` to
 *    `PasscodeStorageAdapter` and `BiometricsSetupAdapterImpl` to
 *    `BiometricStorageAdapter` (the two storage contracts the libraries
 *    require).
 *  - `MifosAuthenticatorModule` (in `:feature:passcode`) registers
 *    `MifosPasscodeViewModel` and `BiometricSetupScreenViewmodel`.
 *  - [MifosPasscodeModule] below constructs the singleton `PasscodeManager`
 *    that the library exposes; it must be a `single` so the same instance is
 *    shared by every passcode screen and the logout call site.
 */
object KoinModules {
    private val commonModules = module {
        includes(stringProviderModule)
        includes(DispatchersModule)
    }
    private val dataModules = module {
        includes(RepositoryModule)
    }
    private val domainModules = module {
        includes(DomainModule)
    }
    private val coreDataStoreModules = module {
        includes(PreferencesModule)
    }
    private val networkModules = module {
        includes(LocalModule, NetworkModule)
    }

    // Visibility widened to `internal` so the NEW app shell's initKoin
    // (`cmp.shared.utils.initKoin`, `cmp-shared/utils/KoinExt.kt`) can splice
    // this list onto `cmp.navigation.di.KoinModules.allModules` at start-up.
    // The new shell wholesale-bridges the fork's `MifosApp` (which composes
    // ~60 fork feature screens with `koinViewModel()`-resolved fork VMs); the
    // template shell's Koin graph carries the fork core/network/data/domain/
    // passcode modules already (see `cmp-navigation/.../KoinModules.kt`
    // `forkCommonModule`, `forkNetworkModule`, `forkDataModule`,
    // `forkDomainModule`, `forkFeatureModule`, `MifosPasscodeModule`,
    // `PreferencesModule`), but NOT this featureModules aggregate nor the
    // shared VMs — those get spliced in by `KoinExt.initKoin`.
    internal val sharedModule = module {
        viewModelOf(::MifosPayViewModel)
        viewModelOf(::InstanceSelectorViewModel)
        viewModelOf(::TransferOptionsViewModel)
    }
    internal val featureModules = module {
        includes(
            AuthModule,
            MifosHomeModule,
            SettingsModule,
            FaqModule,
            EditPasswordModule,
            ProfileModule,
            HistoryModule,
            PaymentsModule,
            AccountsModule,
            BeneficiaryModule,
            InvoicesModule,
            KYCModule,
            NotificationModule,
            SavedCardsModule,
            ReceiptModule,
            StandingInstructionModule,
            IntraBankModule,
            interbankTransferModule,
            MpayQrModule,
            MpayQrScanModule,
            FastMpayModule,
            MerchantsModule,
            UpiSetupModule,
            MifosAuthenticatorModule,
            AutoPayModule,
            SendMoneyModule,
            LoanApplicationModule,
            PocketModule,
        )
    }

    /**
     * Provides the library's [PasscodeManager] as a process-wide singleton.
     *
     * Must be `single`: every passcode screen, the settings change-passcode
     * call site, and `MifosPayViewModel.logOut()` all share state through this
     * one instance. The constructor parameter is the
     * [PasscodeStorageAdapter] bound by [RepositoryModule]; the manager reads
     * it once in its `init` block to seed the initial step
     * (`Enter` if a passcode exists, `Create` otherwise).
     */
    private val MifosPasscodeModule = module {
        single { PasscodeManager(get()) }
    }

    val allModules = listOf(
        commonModules,
        dataModules,
        domainModules,
        coreDataStoreModules,
        networkModules,
        featureModules,
        sharedModule,
        MifosPasscodeModule,
    )
}

/**
 * Builds a [koinApplication] without starting it — used by Compose Previews
 * and tests that need to resolve dependencies without touching the global
 * Koin state.
 */
fun koinConfiguration() = koinApplication {
    modules(KoinModules.allModules)
}

/**
 * Starts the global Koin instance. Called once per process from each
 * platform's entrypoint (Android `MifosPayApplication.onCreate`, desktop
 * `main`, etc.).
 *
 * @param config Optional platform-specific extras (e.g. `androidContext` on
 *        Android) merged in before the modules list.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(KoinModules.allModules)
    }
}
