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

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifospay.core.common.MifosDispatchers
import org.mifospay.core.data.util.ConnectivityManagerNetworkMonitor
import org.mifospay.core.data.util.MifosNetworkMonitorQualifier
import org.mifospay.core.data.util.MifosTimeZoneMonitorQualifier
import org.mifospay.core.data.util.NetworkMonitor
import org.mifospay.core.data.util.TimeZoneBroadcastMonitor
import org.mifospay.core.data.util.TimeZoneMonitor

val AndroidDataModule = module {
    // Legacy fork NetworkMonitor (org.mifospay.core.data.util) for DataState `withNetworkCheck`.
    // The Store5 `kpt.core.data.infra.NetworkMonitor` is bound in DataModule via kmptoolkit's
    // `NetworkMonitorProvider.install()` (cmp-network-monitor 3.6.0+, which carries the
    // AndroidNetworkMonitor cold-start seed fix) — no per-platform override needed here.
    single<NetworkMonitor>(MifosNetworkMonitorQualifier) {
        ConnectivityManagerNetworkMonitor(androidContext(), get(named(MifosDispatchers.IO.name)))
    }

    single<TimeZoneMonitor>(MifosTimeZoneMonitorQualifier) {
        TimeZoneBroadcastMonitor(
            context = androidContext(),
            appScope = get(named("ApplicationScope")),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
        )
    }

    single {
        AndroidPlatformDependentDataModule(
            context = androidContext(),
            dispatcher = get(named(MifosDispatchers.IO.name)),
            scope = get(named("ApplicationScope")),
        )
    }
}

actual val platformModule: Module = AndroidDataModule

actual val getPlatformDataModule: PlatformDependentDataModule
    get() = org.koin.core.context.GlobalContext.get().get()
