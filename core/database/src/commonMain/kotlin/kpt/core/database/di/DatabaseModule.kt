/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.di

import kpt.core.base.database.DatabaseNaming
import kpt.core.database.AppDatabase
import kpt.core.database.config.DatabaseConfig
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Per-fork database naming, sourced from the syncForkConfig-generated [DatabaseConfig]
 * (both values derive from `app-profile/app.yaml`). Threaded into every platform actual
 * of [platformModule] so the on-disk file + desktop directory are unique per fork.
 */
internal val appDatabaseNaming = DatabaseNaming(
    fileName = DatabaseConfig.NAME,
    desktopDirName = DatabaseConfig.DESKTOP_DIR_NAME,
)

/**
 * Koin module that provides the [AppDatabase] instance and the framework-infra DAO singletons.
 *
 * Delegates platform-specific database construction to [platformModule], whose actuals defer
 * entirely to the template-owned `platformDatabaseModule<T>` in core-base/database — so this
 * module and its actuals carry ZERO driver/dispatcher/fallback boilerplate.
 *
 * INFRA-ONLY, owner: template (E1 / C3). The demo DAO providers + the ChargeTypeConverters install
 * derived instead: every declared DAO's binding is GENERATED into [GeneratedDaoBindings]. This aggregator carries
 * ZERO `kpt.core.*.demo.*` imports so a template sync can blind-copy it without re-introducing demo
 * wiring a fork already stripped.
 */
val DatabaseModule = module {
    // Every DAO's Koin binding (GENERATED from `@DbDao`) and every converter's one-shot install
    // (GENERATED from `@DbConverters`). Both are emitted into this same package, so this file needs
    // no import and keeps its zero-domain-reference property — which is what lets a template sync
    // blind-copy it.
    includes(GeneratedDaoBindings, GeneratedConverterBindings)

    includes(platformModule)
    // infra (framework) — always kept
}

/**
 * Platform-specific Koin module that provides the [AppDatabase] singleton.
 *
 * Each source set's actual is a one-line delegation to the template-owned
 * [platformDatabaseModule][kpt.core.base.database.platformDatabaseModule] (core-base/database),
 * which owns the correct [SQLiteDriver][androidx.sqlite.SQLiteDriver], coroutine dispatcher and
 * destructive-migration fallback per platform. The fork supplies only its concrete [AppDatabase]
 * type and the generated [appDatabaseNaming].
 */
expect val platformModule: Module
