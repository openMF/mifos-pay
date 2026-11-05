/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.shared.utils

import cmp.navigation.di.KoinModules
import cmp.navigation.privacy.PrivacyConsentApplier
import cmp.navigation.registry.ForkInitializers
import cmp.shared.generated.WorkerKmpAuto
import kpt.sync.infra.initSyncNotifier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.koinApplication
import org.koin.mp.KoinPlatform
import org.mifospay.shared.di.KoinModules as ForkKoinModules

// Fork feature Koin surface. The template shell's `cmp.navigation.di.KoinModules.allModules`
// carries the fork's core/network/data/domain/passcode/preferences modules. The pieces MISSING
// from that list — and REQUIRED by the bridged fork `MifosApp` which composes ~60 fork feature
// screens with `koinViewModel()`-resolved VMs — are the fork's per-feature Koin modules aggregate
// (`featureModules`: Auth, Home, Payments, History, AutoPay, Accounts, Beneficiary, Pocket,
// Profile, IntraBank, interbankTransfer, MpayQr, MpayQrScan, FastMpay, Merchants, UpiSetup,
// SendMoney, LoanApplication, Notification, SavedCards, KYC, Invoices, EditPassword, Faq,
// Settings, StandingInstruction, Receipt + MifosAuthenticatorModule — Koin's `includes()` is
// identity-deduplicated so re-registration is a no-op) plus the fork's shared VM module
// (`sharedModule`: MifosPayViewModel, InstanceSelectorViewModel, TransferOptionsViewModel). Both
// are `internal val` in `org.mifospay.shared.di.KoinModules` (same `:cmp-shared` module) and are
// imported below aliased as `ForkKoinModules`.

// One splice list — kept as a `val` (not inlined below) so `koinConfiguration()` and
// `initKoin()` share the exact same module set. Order: template modules first (they establish
// the core/network/data/domain graph the fork feature modules depend on), then fork
// feature/shared modules layered on top.
private val allModulesForNewShell = KoinModules.allModules +
    listOf(ForkKoinModules.featureModules, ForkKoinModules.sharedModule)

/**
 * The Koin application, configured but not started — what a Compose `KoinApplication` takes and what a test can build
 * in isolation.
 */
fun koinConfiguration() = koinApplication {
    modules(allModulesForNewShell)
}

/**
 * Starts Koin with the app's modules.
 *
 * @param config extra declarations, e.g. a platform module or a test override.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(allModulesForNewShell)
    }

    // Privacy consent first: analytics starts OFF (core-base) and only the user's stored choice may turn it
    // on. commonMain, so every platform follows the same consent.
    KoinPlatform.getKoin().get<PrivacyConsentApplier>().start()

    // worker-kmp single-API: ONE commonMain call wires the workers on EVERY platform
    // (Android / iOS / Desktop / Web). It runs after startKoin so the codegen-emitted
    // per-platform installer sees a started Koin — on Android the generated actual reads
    // the Application from the `androidContext(...)` the caller bound inside `config`, so
    // NO per-platform (cmp-android) worker code is required. This is the whole point of the
    // single-API commonMain design: platform-neutral wiring lives here, not in a platform
    // app class. Declared worker set: cmp-shared/WorkerDeclarations.kt (@WorkerKmpWorkers).
    WorkerKmpAuto.install()

    // KMPNotifier one-time setup — commonMain, every platform. NotificationWorker posts
    // local notifications through NotifierManager.getLocalNotifier() (all worker code lives in sync/).
    initSyncNotifier()

    // Fork-owned app-startup hooks (analytics, crash reporting, remote-config, …) from the
    // white-label ForkInitializers seam. commonMain, so EVERY platform (Android / iOS / Desktop /
    // Web) runs them once after Koin init — no per-platform app-class edit (S3/S4 heal, T7,
    // epic pure-white-label-store5-network). A fork registers hooks in cmp-navigation's
    // ForkInitializers; the template entry points stay untouched.
    ForkInitializers.runAll()
}
