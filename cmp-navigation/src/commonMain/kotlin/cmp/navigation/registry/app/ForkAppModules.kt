/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import cmp.navigation.registry.app.instance.InstanceSelectorViewModel
import kpt.core.common.di.DispatchersModule
import kpt.core.common.di.stringProviderModule
import kpt.core.data.di.fork.RepositoryModule
import kpt.core.datastore.di.PreferencesModule
import kpt.core.domain.di.DomainModule
import kpt.core.network.di.LocalModule
import kpt.core.network.di.fork.NetworkModule
import kpt.feature.accounts.di.AccountsModule
import kpt.feature.auth.di.AuthModule
import kpt.feature.autopay.di.AutoPayModule
import kpt.feature.beneficiary.di.BeneficiaryModule
import kpt.feature.editpassword.di.EditPasswordModule
import kpt.feature.faq.di.FaqModule
import kpt.feature.fastmpay.di.FastMpayModule
import kpt.feature.history.di.HistoryModule
import kpt.feature.home.di.fork.MifosHomeModule
import kpt.feature.invoices.di.InvoicesModule
import kpt.feature.kyc.di.KYCModule
import kpt.feature.merchants.di.MerchantsModule
import kpt.feature.mpay.qr.di.MpayQrModule
import kpt.feature.mpay.qr.scan.di.MpayQrScanModule
import kpt.feature.notification.di.NotificationModule
import kpt.feature.payments.di.PaymentsModule
import kpt.feature.pocket.di.PocketModule
import kpt.feature.profile.di.ProfileModule
import kpt.feature.receipt.di.ReceiptModule
import kpt.feature.savedcards.di.SavedCardsModule
import kpt.feature.send.money.di.SendMoneyModule
import kpt.feature.settings.di.SettingsModule
import kpt.feature.standing.instruction.di.StandingInstructionModule
import kpt.feature.transfer.interbank.di.interbankTransferModule
import kpt.feature.transfer.intrabank.di.IntraBankModule
import kpt.feature.upi.setup.di.UpiSetupModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.feature.passcode.MifosAuthenticatorModule
import org.mifos.lib.loan.di.LoanApplicationModule

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
object ForkAppModules {
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
        viewModelOf(::ForkAppViewModel)
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
     * call site, and `ForkAppViewModel.logOut()` all share state through this
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
