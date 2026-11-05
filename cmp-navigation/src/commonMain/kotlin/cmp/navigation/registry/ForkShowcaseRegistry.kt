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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.feature.settings.DevMenuEntry

/**
 * ForkShowcaseRegistry — template-shipped dev-only demo entry points (Transition Gallery,
 * State Gallery). Content is fully fenced with demo begin/end markers so
 * `remove-demo.sh` / `scripts/white-label/customize.sh --clean` reduces the two members
 * to empty stubs (list returns empty, graph body is empty) — the shell still resolves
 * the object; the dev menu simply hides on the neutralized fork.
 *
 * Ownership: `owner: template` (dev-only shipped content — sync-reachable).
 */
object ForkShowcaseRegistry {
    /**
     * Dev-menu entries surfaced in `SettingsScreen`'s Developer section. Returns an
     * empty list on release builds (dev menu hidden) and, on a `--clean` fork where the
     * fenced block is stripped, the trailing `return emptyList()` is the whole body.
     */
    fun devSettingsEntries(navController: NavController): List<DevMenuEntry> {
        return emptyList()
    }

    /** Dev-only nav destinations registered inside the authenticated graph. */
    val devDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
    }
}
