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

import kpt.core.base.database.platformDatabaseModule
import kpt.core.database.AppDatabase
import org.koin.core.module.Module

// Delegates to the template-owned platformDatabaseModule<T> (core-base/database), which owns
// the Android SQLite driver + IO dispatcher + destructive-migration fallback.
/**
 * Koin bindings for the android Room driver. The builder differs per platform (file path, bundled driver, in-memory),
 * which is why this is an actual rather than one shared module.
 */
actual val platformModule: Module = platformDatabaseModule<AppDatabase>(appDatabaseNaming)
