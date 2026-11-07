/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.di

import kpt.core.network.main.localAssets.JvmLocalAssetManager
import kpt.core.network.main.localAssets.LocalAssetManager
import org.koin.dsl.module
/** Koin module providing the local asset manager and data source. */
val LocalModule = module {
    single<LocalAssetManager> {
        JvmLocalAssetManager
    }
}
