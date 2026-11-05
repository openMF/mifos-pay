/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.common.manager

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainCoroutineDispatcher

/**
 * Injectable access to the app's coroutine dispatchers.
 *
 * Exists so nothing has to reference `Dispatchers.*` directly: a test binds a fake whose members all
 * return one `TestDispatcher`, which makes coroutine work deterministic without a scheduler race.
 * Code that hardcodes `Dispatchers.IO` cannot be controlled that way, which is why the indirection
 * is worth the injection.
 */
interface DispatcherManager {
    /**
     * The default [CoroutineDispatcher] for the app.
     */
    val default: CoroutineDispatcher

    /**
     * The [MainCoroutineDispatcher] for the app.
     */
    val main: MainCoroutineDispatcher

    /**
     * The IO [CoroutineDispatcher] for the app.
     */
    val io: CoroutineDispatcher

    /**
     * The unconfined [CoroutineDispatcher] for the app.
     */
    val unconfined: CoroutineDispatcher

    /** Application-lifetime scope for work that must outlive any screen — cancelled only at process end. */
    val appScope: CoroutineScope
}
