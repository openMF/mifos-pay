/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.common.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for `core-base/common` — currently the platform [dispatcherManagerModule] binding.
 *
 * Include it once from the app's module graph; every other core module assumes a `DispatcherManager`
 * is already resolvable.
 */
val CommonModule = module {
    includes(dispatcherManagerModule)
}

/**
 * The per-platform `DispatcherManager` binding, supplied by each target's `actual`.
 *
 * Separate from [CommonModule] because the dispatcher set is the one part of this module that cannot
 * be expressed in common code.
 */
expect val dispatcherManagerModule: Module
