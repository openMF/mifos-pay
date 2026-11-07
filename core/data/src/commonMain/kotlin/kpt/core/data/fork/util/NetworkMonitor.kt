/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.util

import kotlinx.coroutines.flow.Flow
import org.koin.core.qualifier.named

/**
 * Utility for reporting app connectivity status
 */
interface NetworkMonitor {
    /** Emits true while the device has network connectivity. */
    val isOnline: Flow<Boolean>
}

/**
 * Koin qualifier for this [NetworkMonitor]. On JS / Wasm Koin keys a binding by the type's simple
 * name, so without it this binding and kmptoolkit's `NetworkMonitor` (bound by the template's
 * DataModule) share one key and the app receives the wrong instance.
 */
val ForkNetworkMonitorQualifier = named("kpt.core.data.fork.util.NetworkMonitor")
