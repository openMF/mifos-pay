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
import kotlinx.coroutines.Dispatchers
import kpt.core.common.ForkDispatchers
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
/** Koin module binding the IO [kotlinx.coroutines.CoroutineDispatcher] qualified by ForkDispatchers.IO. */
actual val ioDispatcherModule: Module
    get() = module {
        single<CoroutineDispatcher>(named(ForkDispatchers.IO.name)) { Dispatchers.IO }
    }
