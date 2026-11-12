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

import kpt.core.data.JsPlatformDependentDataModule
import org.koin.core.module.Module
import org.koin.dsl.module
/** Desktop [PlatformDependentDataModule] instance. */
actual val getPlatformDataModule: PlatformDependentDataModule
    get() = JsPlatformDependentDataModule()

/** Koin module binding the desktop [PlatformDependentDataModule]. */
actual val platformModule: Module
    get() = module {
        single<PlatformDependentDataModule> { getPlatformDataModule }
    }
