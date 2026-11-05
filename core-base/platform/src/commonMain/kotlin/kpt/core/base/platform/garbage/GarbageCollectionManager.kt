/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.garbage

/**
 * A hint to the platform that now is a reasonable moment to reclaim memory.
 *
 * A HINT, never a guarantee — no runtime here promises to collect on request. Use it after
 * releasing something genuinely large (a decoded bitmap set, a closed database), not as a routine
 * step; calling it on a cadence costs pauses and buys nothing.
 */
interface GarbageCollectionManager {
    /**
     * Calls the garbage collector on the [Runtime] in an effort to clear the unused resources in
     * the heap.
     */
    fun tryCollect()
}

/**
 * The platform's collection hint, bound per target — `Runtime.getRuntime().gc()` on the JVM, a no-op
 * where the runtime exposes no such control.
 *
 * Prefer the injectable [GarbageCollectionManager] at call sites; this exists for its `actual`s.
 */
expect val garbageCollector: () -> Unit
