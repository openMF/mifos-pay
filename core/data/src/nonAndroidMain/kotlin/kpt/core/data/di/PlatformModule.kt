/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

import kpt.core.base.data.infra.TimeZoneMonitor
import kpt.core.base.data.infra.impl.TimeZoneMonitorImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/** Empty on every non-Android target: the only platform-dependent data binding needs an Android `Context`. */
actual val platformModule: Module
    get() = module {
        single<TimeZoneMonitor> { TimeZoneMonitorImpl() }
    }
