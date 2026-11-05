/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.data.infra.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import kpt.core.base.data.infra.TimeZoneMonitor

/** Non-Android targets implementation of `TimeZoneMonitorImpl`. */
class TimeZoneMonitorImpl : TimeZoneMonitor {
    override val currentTimeZone: Flow<TimeZone>
        get() = flowOf(TimeZone.currentSystemDefault())
}
