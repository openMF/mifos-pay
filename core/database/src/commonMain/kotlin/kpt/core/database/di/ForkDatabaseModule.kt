/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.di

import org.koin.dsl.module

/**
 * THE FORK'S database DI seam. Empty on the neutral template — this is yours to fill.
 *
 * It lives outside `demo/` on purpose, so `scripts/remove-demo.sh` leaves it standing: a fork that
 * runs the customizer (which strips the demo BY DEFAULT — "forking = starting clean") keeps this file
 * and can wire DI immediately.
 *
 * You will rarely need it. DAO bindings come from `@DbDao` and converter installs from
 * `@DbConverters`, both generated — so this is for genuinely fork-specific wiring that no annotation
 * describes, not for anything the schema already declares.
 *
 * Example:
 * ```
 * single { MyBackupScheduler(get<AppDatabase>()) }
 * ```
 */
val ForkDatabaseModule = module {
    // Intentionally empty on the template — a fork adds its own bindings here.
}
