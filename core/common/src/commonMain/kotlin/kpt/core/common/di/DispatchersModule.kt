/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kpt.core.common.DefaultStringProvider
import kpt.core.common.MifosDispatchers
import kpt.core.common.StringProvider
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
/** Koin module binding the default [StringProvider]. */
val stringProviderModule = module {
    single<StringProvider> { DefaultStringProvider() }
}

/**
 * Koin module providing the Default and Unconfined dispatchers and the application scope (IO comes from
 * [ioDispatcherModule]).
 */
val DispatchersModule = module {
    includes(ioDispatcherModule)
    single<CoroutineDispatcher>(named(MifosDispatchers.Default.name)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(MifosDispatchers.Unconfined.name)) { Dispatchers.Unconfined }
    single<CoroutineScope>(named("ApplicationScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

/** Platform-specific Koin module binding the IO dispatcher. */
expect val ioDispatcherModule: Module
