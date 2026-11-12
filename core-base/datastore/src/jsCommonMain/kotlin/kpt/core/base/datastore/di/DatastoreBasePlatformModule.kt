/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.datastore.di

import kpt.core.base.datastore.SecureSettingsFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/** `datastoreBasePlatformModule` on Web (JS/WasmJS). */
actual val datastoreBasePlatformModule: Module = module {
    single { SecureSettingsFactory() }
}
