/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.di

import kpt.core.datastore.PreferencesHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val CoreDataStoreModule = module {

    factory {
        PreferencesHelper(context = androidContext())
    }
}
