/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.di

import kotlinx.coroutines.Dispatchers
import kpt.core.base.database.AppDatabaseFactory
import kpt.core.database.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val testPlatformModule: Module = module {
    factory<AppDatabase> {
        // Go through AppDatabaseFactory rather than calling Room directly: it owns the
        // web-worker SQLite driver, and Room 3 KMP has no default driver on js/wasmJs. Building
        // the builder here by hand omitted it, so the DB failed to construct and surfaced as an
        // opaque Koin `InstanceCreationException` naming AppDatabase rather than SQLite.
        AppDatabaseFactory()
            .createInMemoryDatabase<AppDatabase>()
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
}
