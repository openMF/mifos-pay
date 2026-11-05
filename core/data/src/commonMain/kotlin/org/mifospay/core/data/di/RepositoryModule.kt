/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.core.data.di

import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.authenticator.biometrics.BiometricStorageAdapter
import org.mifos.authenticator.passcode.PasscodeStorageAdapter
import org.mifospay.core.common.MifosDispatchers
import org.mifospay.core.data.repository.AccountRepository
import org.mifospay.core.data.repository.AppLockRepository
import org.mifospay.core.data.repository.AssetRepository
import org.mifospay.core.data.repository.AuthenticationRepository
import org.mifospay.core.data.repository.AutoPayHistoryRepository
import org.mifospay.core.data.repository.AutoPayRepository
import org.mifospay.core.data.repository.BeneficiaryRepository
import org.mifospay.core.data.repository.BillerRepository
import org.mifospay.core.data.repository.ClientRepository
import org.mifospay.core.data.repository.DocumentRepository
import org.mifospay.core.data.repository.InterBankRepository
import org.mifospay.core.data.repository.InvoiceRepository
import org.mifospay.core.data.repository.KycLevelRepository
import org.mifospay.core.data.repository.LocalAssetRepository
import org.mifospay.core.data.repository.NotificationRepository
import org.mifospay.core.data.repository.OfficeRepository
import org.mifospay.core.data.repository.PocketRepository
import org.mifospay.core.data.repository.RecentPayeeRepository
import org.mifospay.core.data.repository.RegistrationRepository
import org.mifospay.core.data.repository.RunReportRepository
import org.mifospay.core.data.repository.SavedCardRepository
import org.mifospay.core.data.repository.SavingsAccountRepository
import org.mifospay.core.data.repository.SearchRepository
import org.mifospay.core.data.repository.SelfServiceRepository
import org.mifospay.core.data.repository.StandingInstructionRepository
import org.mifospay.core.data.repository.ThirdPartyTransferRepository
import org.mifospay.core.data.repository.TwoFactorAuthRepository
import org.mifospay.core.data.repository.UserRepository
import org.mifospay.core.data.repository.UserVerificationRepository
import org.mifospay.core.data.repositoryImpl.AccountRepositoryImpl
import org.mifospay.core.data.repositoryImpl.AppLockRepositoryImpl
import org.mifospay.core.data.repositoryImpl.AssetRepositoryImpl
import org.mifospay.core.data.repositoryImpl.AuthenticationRepositoryImpl
import org.mifospay.core.data.repositoryImpl.AutoPayHistoryRepositoryImpl
import org.mifospay.core.data.repositoryImpl.AutoPayRepositoryImpl
import org.mifospay.core.data.repositoryImpl.BeneficiaryRepositoryImpl
import org.mifospay.core.data.repositoryImpl.BillOfflineRepositoryImpl
import org.mifospay.core.data.repositoryImpl.BillerOfflineRepositoryImpl
import org.mifospay.core.data.repositoryImpl.BillerRepositoryImpl
import org.mifospay.core.data.repositoryImpl.BiometricsSetupAdapterImpl
import org.mifospay.core.data.repositoryImpl.ClientRepositoryImpl
import org.mifospay.core.data.repositoryImpl.DocumentRepositoryImpl
import org.mifospay.core.data.repositoryImpl.InterBankRepositoryImpl
import org.mifospay.core.data.repositoryImpl.InvoiceRepositoryImpl
import org.mifospay.core.data.repositoryImpl.KycLevelRepositoryImpl
import org.mifospay.core.data.repositoryImpl.LocalAssetRepositoryImpl
import org.mifospay.core.data.repositoryImpl.MifosPasscodeAdapterImpl
import org.mifospay.core.data.repositoryImpl.NotificationRepositoryImpl
import org.mifospay.core.data.repositoryImpl.OfficeRepositoryImpl
import org.mifospay.core.data.repositoryImpl.PocketRepositoryImp
import org.mifospay.core.data.repositoryImpl.RecentPayeeRepositoryImpl
import org.mifospay.core.data.repositoryImpl.RegistrationRepositoryImpl
import org.mifospay.core.data.repositoryImpl.RunReportRepositoryImpl
import org.mifospay.core.data.repositoryImpl.SavedCardRepositoryImpl
import org.mifospay.core.data.repositoryImpl.SavingsAccountRepositoryImpl
import org.mifospay.core.data.repositoryImpl.SearchRepositoryImpl
import org.mifospay.core.data.repositoryImpl.SelfServiceRepositoryImpl
import org.mifospay.core.data.repositoryImpl.StandingInstructionRepositoryImpl
import org.mifospay.core.data.repositoryImpl.ThirdPartyTransferRepositoryImpl
import org.mifospay.core.data.repositoryImpl.TwoFactorAuthRepositoryImpl
import org.mifospay.core.data.repositoryImpl.UserRepositoryImpl
import org.mifospay.core.data.repositoryImpl.UserVerificationRepositoryImpl
import org.mifospay.core.data.util.NetworkMonitor
import org.mifospay.core.data.util.QrTransferRouter
import org.mifospay.core.data.util.TimeZoneMonitor

private val ioDispatcher = named(MifosDispatchers.IO.name)
private val unconfined = named(MifosDispatchers.Unconfined.name)

val RepositoryModule = module {
    single<Json> { Json { ignoreUnknownKeys = true } }

    single<AssetRepository> { AssetRepositoryImpl() }
    single<AccountRepository> {
        AccountRepositoryImpl(
            apiManager = get(),
            selfManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-3 LEDGER store wiring (GOAL D13). Named-qualifier
            // from AppStoreRegistry.SelfAccounts; NetworkMonitor +
            // FetchedAtRepository bound by DataModule.
            selfAccountsStore = get(kpt.core.store.config.AppStoreRegistry.SelfAccounts),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
            // transfer-detail Store5 vertical (GOAL D13) — makes the
            // transaction-detail drill-down offline-first. Named-qualifier from
            // AppStoreRegistry.TransferDetail; the TransactionDao is bound by
            // StoreModule (`kpt.core.store.di.StoreModule`) via
            // `get<AppDatabase>().transactionDao` and is used by getTransaction(...)
            // to read the cached ledger row offline-first.
            transferDetailStore = get(kpt.core.store.config.AppStoreRegistry.TransferDetail),
            transactionDao = get(),
        )
    }
    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(get(), get(ioDispatcher))
    }
    single<BeneficiaryRepository> { BeneficiaryRepositoryImpl(get(), get(ioDispatcher)) }
    single<ClientRepository> {
        ClientRepositoryImpl(
            apiManager = get(),
            fineractApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 SINGLE-ROW-PER-KEY store wiring (GOAL D13). Named-qualifier
            // from AppStoreRegistry.ClientDetail; NetworkMonitor + FetchedAtRepository
            // bound by DataModule (kpt.core.data.di.DataModule).
            clientDetailStore = get(kpt.core.store.config.AppStoreRegistry.ClientDetail),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<DocumentRepository> { DocumentRepositoryImpl(get(), get(ioDispatcher)) }
    single<InvoiceRepository> {
        InvoiceRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Invoice; NetworkMonitor + FetchedAtRepository
            // bound by DataModule.
            invoiceStore = get(kpt.core.store.config.AppStoreRegistry.Invoice),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<InterBankRepository> { InterBankRepositoryImpl(get(), get(ioDispatcher)) }
    single<KycLevelRepository> { KycLevelRepositoryImpl(get(), get(ioDispatcher)) }
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Notification; NetworkMonitor + FetchedAtRepository
            // bound by DataModule.
            notificationStore = get(kpt.core.store.config.AppStoreRegistry.Notification),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<RecentPayeeRepository> {
        RecentPayeeRepositoryImpl(
            selfServiceApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-4 LOCAL-DERIVED store wiring (GOAL D12/D13
            // hybrid). The DAO is bound by StoreModule
            // (`kpt.core.store.di.StoreModule`) via
            // `get<AppDatabase>().recentPayeeDao`.
            recentPayeeDao = get(),
        )
    }
    single<RegistrationRepository> { RegistrationRepositoryImpl(get(), get(ioDispatcher)) }
    single<RunReportRepository> { RunReportRepositoryImpl(get(), get(ioDispatcher)) }
    single<SavedCardRepository> {
        SavedCardRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-1 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.SavedCards; NetworkMonitor + FetchedAtRepository
            // bound by DataModule (kpt.core.data.di.DataModule).
            savedCardStore = get(kpt.core.store.config.AppStoreRegistry.SavedCards),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<SavingsAccountRepository> {
        SavingsAccountRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-1 SINGLE-ROW-PER-KEY store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.AccountDetail; NetworkMonitor + FetchedAtRepository
            // bound by DataModule (kpt.core.data.di.DataModule).
            accountDetailStore = get(kpt.core.store.config.AppStoreRegistry.AccountDetail),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<SearchRepository> { SearchRepositoryImpl(get(), get(ioDispatcher)) }
    single<SelfServiceRepository> {
        SelfServiceRepositoryImpl(
            apiManager = get(),
            dispatcher = get(ioDispatcher),
            // Phase-4 Batch-A LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.History; NetworkMonitor + FetchedAtRepository bound by
            // DataModule (kpt.core.data.di.DataModule).
            historyStore = get(kpt.core.store.config.AppStoreRegistry.History),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
            // Phase-5 Batch-1 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Beneficiary; the same NetworkMonitor + FetchedAtRepository
            // instances are re-used by every store-backed method on this impl.
            beneficiaryStore = get(kpt.core.store.config.AppStoreRegistry.Beneficiary),
        )
    }
    single<StandingInstructionRepository> {
        StandingInstructionRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-3 LEDGER store wiring (GOAL D13). Named-qualifier
            // from AppStoreRegistry.StandingInstruction; NetworkMonitor +
            // FetchedAtRepository bound by DataModule.
            standingInstructionStore = get(kpt.core.store.config.AppStoreRegistry.StandingInstruction),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<ThirdPartyTransferRepository> {
        ThirdPartyTransferRepositoryImpl(get(), get(ioDispatcher))
    }
    single<TwoFactorAuthRepository> { TwoFactorAuthRepositoryImpl(get(), get(ioDispatcher)) }
    single<UserRepository> { UserRepositoryImpl(get(), get(ioDispatcher)) }
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
    single<OfficeRepository> {
        OfficeRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-3 LEDGER store wiring (GOAL D13). Named-qualifier
            // from AppStoreRegistry.Offices; NetworkMonitor + FetchedAtRepository
            // bound by DataModule.
            officesStore = get(kpt.core.store.config.AppStoreRegistry.Offices),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<PocketRepository> {
        PocketRepositoryImp(
            dataManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Pocket; NetworkMonitor + FetchedAtRepository bound
            // by DataModule. The PocketDao is bound by StoreModule
            // (`kpt.core.store.di.StoreModule`) via `get<AppDatabase>().pocketDao`
            // and is used by `getAvailableAccountsToLink` to read the already-linked
            // set from the Room SoT snapshot (pre-store this used the in-memory
            // detailedPocketCache which is now removed).
            pocketStore = get(kpt.core.store.config.AppStoreRegistry.Pocket),
            linkableAccountsStore = get(kpt.core.store.config.AppStoreRegistry.LinkableAccounts),
            storeNetworkMonitor = get(),
            fetchedAtRepository = get(),
            pocketDao = get(),
        )
    }

    // Passcode/biometrics surface — the four bindings below are required by the
    // mifos-authenticator-passcode and mifos-authenticator-biometrics libraries
    // (the two adapters) plus the in-app re-auth machinery (lock + verification
    // token). See KDoc on the bound types for the contracts.
    singleOf(::MifosPasscodeAdapterImpl).bind<PasscodeStorageAdapter>()
    singleOf(::BiometricsSetupAdapterImpl).bind<BiometricStorageAdapter>()
    singleOf(::AppLockRepositoryImpl).bind<AppLockRepository>()
    singleOf(::UserVerificationRepositoryImpl).bind<UserVerificationRepository>()

    // QR Transfer Router for smart intra/inter-bank routing
    single { QrTransferRouter(userPreferencesRepository = get()) }
    single<AutoPayHistoryRepository> { AutoPayHistoryRepositoryImpl(get(), get(ioDispatcher)) }

    // NOTE: `core/data/BillerRepository` (this line) is the NETWORK-based
    // Fineract biller interface. The binding is REGISTERED but currently
    // has NO CONSUMERS in the app — feature ViewModels resolve the
    // grandfathered `org.mifospay.core.datastore.BillerRepository`
    // interface, which is bound to the OFFLINE store-backed
    // `BillerOfflineRepositoryImpl` below. Keeping this binding registered
    // (a) preserves the imports for the day the Fineract biller-management
    // endpoints materialize (the `TODO` in `BillerRepositoryImpl`), and
    // (b) is a no-op at runtime because Koin does not instantiate a
    // singleton until it is resolved. When the Fineract endpoint lands, a
    // hybrid impl can bridge both interfaces.
    single<BillerRepository> { BillerRepositoryImpl(get(), get(ioDispatcher)) }

    // Phase-4 Batch-A OFFLINE_LOCAL_ONLY Bill store binding (GOAL D12).
    // Replaces the multiplatform-settings impl that was previously bound in
    // `core/datastore/di/PreferenceModule.kt` — that binding is REMOVED in
    // the same commit to avoid a double-registration on `BillRepository`.
    single<org.mifospay.core.datastore.BillRepository> {
        BillOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher))
    }

    // Phase-5 Batch-4 OFFLINE_LOCAL_ONLY Biller store binding (GOAL D12).
    // Replaces the multiplatform-settings impl that was previously bound in
    // `core/datastore/di/PreferenceModule.kt` (line 53) — that binding is
    // REMOVED in the same commit to avoid a double-registration on
    // `org.mifospay.core.datastore.BillerRepository`. Mirrors the
    // `BillOfflineRepositoryImpl` cutover above.
    single<org.mifospay.core.datastore.BillerRepository> {
        BillerOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher))
    }

    includes(platformModule)
    single<PlatformDependentDataModule> { getPlatformDataModule }
    single<NetworkMonitor> { getPlatformDataModule.networkMonitor }
    single<TimeZoneMonitor> { getPlatformDataModule.timeZoneMonitor }
    single<LocalAssetRepository> {
        LocalAssetRepositoryImpl(
            ioDispatcher = get(qualifier = ioDispatcher),
            unconfinedDispatcher = get(unconfined),
            networkJson = get(),
        )
    }
}
