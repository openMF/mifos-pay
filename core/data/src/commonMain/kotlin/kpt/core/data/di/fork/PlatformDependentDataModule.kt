/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di.fork

import kpt.core.data.util.NetworkMonitor
import kpt.core.data.util.TimeZoneMonitor
import org.koin.core.module.Module
/** Platform-specific data dependencies (network and time-zone monitors) supplied by each target. */
interface PlatformDependentDataModule {
    /** Monitor that reports whether the device currently has network connectivity. */
    val networkMonitor: NetworkMonitor

    /** Monitor that emits the device's current time zone. */
    val timeZoneMonitor: TimeZoneMonitor
}

/** Platform-specific Koin module with the monitors and bindings each target needs. */
expect val platformModule: Module

/** Platform-specific [PlatformDependentDataModule] instance, resolved per target. */
expect val getPlatformDataModule: PlatformDependentDataModule
