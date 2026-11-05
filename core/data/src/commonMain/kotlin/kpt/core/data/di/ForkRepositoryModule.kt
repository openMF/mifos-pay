/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

import org.koin.dsl.module

/**
 * THE FORK'S repository DI seam. Empty on the neutral template — this is yours to fill.
 *
 * It lives outside `demo/` on purpose, so `scripts/remove-demo.sh` leaves it standing: a fork that
 * runs the customizer (which strips the demo BY DEFAULT — "forking = starting clean") keeps this file
 * and can wire DI immediately. Its demo counterpart is deleted by that same strip.
 *
 * Example:
 * ```
 * single<MyRepository> { MyRepositoryImpl(get(), get()) }
 * ```
 */
val ForkRepositoryModule = module {
    // Intentionally empty on the template — a fork adds its own bindings here.
}
