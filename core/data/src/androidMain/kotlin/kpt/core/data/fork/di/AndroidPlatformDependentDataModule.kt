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

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kpt.core.data.fork.util.ConnectivityManagerNetworkMonitor
import kpt.core.data.fork.util.NetworkMonitor
import kpt.core.data.fork.util.TimeZoneBroadcastMonitor
import kpt.core.data.fork.util.TimeZoneMonitor
/** Android implementation of [PlatformDependentDataModule] backed by the app Context, with lazily created monitors. */
class AndroidPlatformDependentDataModule(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : PlatformDependentDataModule {
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context, dispatcher)
    }

    override val timeZoneMonitor: TimeZoneMonitor by lazy {
        TimeZoneBroadcastMonitor(context, scope, dispatcher)
    }
}
