/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import kpt.core.data.fork.di.PlatformDependentDataModule
import kpt.core.data.fork.util.NetworkMonitor
import kpt.core.data.fork.util.TimeZoneMonitor
/** JS [PlatformDependentDataModule] that assumes the device is online and uses the system time zone. */
class JsPlatformDependentDataModule : PlatformDependentDataModule {
    override val networkMonitor: NetworkMonitor by lazy {
        object : NetworkMonitor {
            override val isOnline: Flow<Boolean> = flowOf(true)
        }
    }

    override val timeZoneMonitor: TimeZoneMonitor by lazy {
        object : TimeZoneMonitor {
            override val currentTimeZone: Flow<TimeZone> = flowOf(TimeZone.currentSystemDefault())
        }
    }
}
