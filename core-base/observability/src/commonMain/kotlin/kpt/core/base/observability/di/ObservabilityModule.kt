/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.observability.di

import kpt.core.base.observability.ConsoleCrashReporter
import kpt.core.base.observability.CrashReporter
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module exposing the default [CrashReporter] binding (stdout console).
 *
 * Forks override this binding in their app-level Koin module:
 *
 * ```kotlin
 * // In fork's app module
 * single<CrashReporter> { FirebaseCrashlyticsReporter() }
 * ```
 *
 * Koin's last-binding-wins resolution lets the fork override without touching framework files.
 */
val observabilityModule = module {
    singleOf(::ConsoleCrashReporter) bind CrashReporter::class
}
