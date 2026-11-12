/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

import kpt.core.base.common.di.CommonModule
import kpt.core.base.data.infra.TimeZoneMonitor
import kpt.core.base.data.infra.impl.TimeZoneMonitorImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Android-only data bindings — the ones needing a `Context`. Every other platform has nothing to add, hence the empty
 * `nonAndroidMain` actual.
 */
actual val platformModule: Module = module {
    includes(CommonModule)

    singleOf(::TimeZoneMonitorImpl) bind TimeZoneMonitor::class
}
