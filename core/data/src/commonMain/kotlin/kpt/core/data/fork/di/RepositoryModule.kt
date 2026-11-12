/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.di

import kotlinx.serialization.json.Json
import kpt.core.data.fork.util.ForkNetworkMonitorQualifier
import kpt.core.data.fork.util.ForkTimeZoneMonitorQualifier
import kpt.core.data.fork.util.NetworkMonitor
import kpt.core.data.fork.util.QrTransferRouter
import kpt.core.data.fork.util.TimeZoneMonitor
import org.koin.dsl.module

/**
 * Koin module for the shared data-layer dependencies that are not repositories (the repositories bind themselves
 * through `@RepositoryBinding`).
 */
val RepositoryModule = module {
    single<Json> { Json { ignoreUnknownKeys = true } }

    // Every repository binds itself through @RepositoryBinding / @DataProvider (GeneratedRepositoryBindings,
    // included by DataModule). What stays here is not a repository.

    // QR Transfer Router for smart intra/inter-bank routing
    single { QrTransferRouter(userPreferencesRepository = get()) }

    includes(platformModule)
    single<PlatformDependentDataModule> { getPlatformDataModule }
    single<NetworkMonitor>(ForkNetworkMonitorQualifier) { getPlatformDataModule.networkMonitor }
    single<TimeZoneMonitor>(ForkTimeZoneMonitorQualifier) { getPlatformDataModule.timeZoneMonitor }
}
