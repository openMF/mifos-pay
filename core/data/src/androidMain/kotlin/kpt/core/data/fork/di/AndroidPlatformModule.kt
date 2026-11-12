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

import kpt.core.common.ForkDispatchers
import kpt.core.data.fork.util.ConnectivityManagerNetworkMonitor
import kpt.core.data.fork.util.ForkNetworkMonitorQualifier
import kpt.core.data.fork.util.ForkTimeZoneMonitorQualifier
import kpt.core.data.fork.util.NetworkMonitor
import kpt.core.data.fork.util.TimeZoneBroadcastMonitor
import kpt.core.data.fork.util.TimeZoneMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
/** Koin module binding the Android network and time-zone monitors and [AndroidPlatformDependentDataModule]. */
val AndroidDataModule = module {
    // Legacy fork NetworkMonitor (kpt.core.data.util) for DataState `withNetworkCheck`.
    // The Store5 `kpt.core.data.infra.NetworkMonitor` is bound in DataModule via kmptoolkit's
    // `NetworkMonitorProvider.install()` (cmp-network-monitor 3.6.0+, which carries the
    // AndroidNetworkMonitor cold-start seed fix) — no per-platform override needed here.
    single<NetworkMonitor>(ForkNetworkMonitorQualifier) {
        ConnectivityManagerNetworkMonitor(androidContext(), get(named(ForkDispatchers.IO.name)))
    }

    single<TimeZoneMonitor>(ForkTimeZoneMonitorQualifier) {
        TimeZoneBroadcastMonitor(
            context = androidContext(),
            appScope = get(named("ApplicationScope")),
            ioDispatcher = get(named(ForkDispatchers.IO.name)),
        )
    }

    single {
        AndroidPlatformDependentDataModule(
            context = androidContext(),
            dispatcher = get(named(ForkDispatchers.IO.name)),
            scope = get(named("ApplicationScope")),
        )
    }
}

/** Android actual of the platform module: exposes [AndroidDataModule]. */
actual val platformModule: Module = AndroidDataModule

/** Resolves the registered [PlatformDependentDataModule] from the global Koin context. */
actual val getPlatformDataModule: PlatformDependentDataModule
    get() = org.koin.core.context.GlobalContext.get().get()
