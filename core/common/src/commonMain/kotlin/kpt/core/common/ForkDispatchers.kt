/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import org.koin.core.annotation.Qualifier

/**
 * Koin qualifier selecting a coroutine dispatcher by [ForkDispatchers] type.
 * @property forkDispatcher The dispatcher kind to inject.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val forkDispatcher: ForkDispatchers)

/** Kinds of coroutine dispatchers the app injects. */
enum class ForkDispatchers {
    /** Dispatcher for CPU-bound work. */
    Default,

    /** Dispatcher for blocking I/O such as disk and network. */
    IO,

    /** Dispatcher that runs in the caller thread until the first suspension. */
    Unconfined,
}

/** Koin qualifier for the application-wide [kotlinx.coroutines.CoroutineScope]. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
