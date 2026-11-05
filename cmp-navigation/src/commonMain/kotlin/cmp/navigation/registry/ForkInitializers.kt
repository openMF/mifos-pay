/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

/**
 * ForkInitializers — the FORK-OWNED white-label seam for app-startup hooks.
 *
 * Each platform app entry point (Android `Application.onCreate`, iOS/desktop `main`) calls [runAll]
 * ONCE, right after Koin is initialized, so a fork can register startup work (analytics init, crash
 * reporting, remote-config prefetch, feature-flag warmup) WITHOUT editing the template-owned entry
 * points. Before this seam a fork had to edit `AndroidApp.onCreate` directly (S3/S4 GAP — no seam);
 * now it adds a hook here. Ownership: `owner: fork` in customization-surface.yaml (epic
 * pure-white-label-store5-network, T7). Template default = no hooks.
 */
object ForkInitializers {
    /**
     * Startup hooks, run in order after Koin init at every platform entry point. Koin is already
     * started when these run, so a hook may resolve dependencies via `KoinComponent`/`getKoin()`.
     */
    val onAppStart: List<() -> Unit> = emptyList()

    /** Invoked once by each platform app entry point after Koin init. Safe when the list is empty. */
    fun runAll() {
        onAppStart.forEach { it() }
    }
}
