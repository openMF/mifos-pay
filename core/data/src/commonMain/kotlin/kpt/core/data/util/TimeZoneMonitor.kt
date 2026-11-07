/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import org.koin.core.qualifier.named

/**
 * Utility for reporting current timezone the device has set.
 * It always emits at least once with default setting and then for each TZ change.
 */

interface TimeZoneMonitor {
    /** Emits the device's current time zone, then each change. */
    val currentTimeZone: Flow<TimeZone>
}

/**
 * Koin qualifier for this [TimeZoneMonitor]. On JS / Wasm Koin keys a binding by the type's simple
 * name, so without it this binding and the template's `kpt.core.base.data.infra.TimeZoneMonitor`
 * share one key and the app receives the wrong instance.
 */
val MifosTimeZoneMonitorQualifier = named("kpt.core.data.util.TimeZoneMonitor")
