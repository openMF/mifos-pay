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

/**
 * The Koin application, configured but not started — what a Compose `KoinApplication` takes and what a test can build
 * in isolation.
 */
fun koinConfiguration() = koinApplication {
    modules(KoinModules.allModules)
}

/**
 * Starts Koin with the app's modules.
 *
 * @param config extra declarations, e.g. a platform module or a test override.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(KoinModules.allModules)
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
