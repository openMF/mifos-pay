/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.ui

import kotlinx.collections.immutable.ImmutableList
import kpt.core.ui.navigation.NavigationItem

/** Everything the shell needs to draw its navigation surface, bundled so the bar, rail and drawer share one input. */
data class ScaffoldNavigationData(
    /**
     * Invoked with the chosen item. The shell does NOT navigate itself — it reports the choice and lets the caller
     * decide, so the same bar drives a nav-graph push in the app and a no-op in a preview.
     */
    val onNavigationClick: (NavigationItem) -> Unit,
    /** The items to show. Immutable so Compose can treat it as a stable parameter. */
    val navigationItems: ImmutableList<NavigationItem>,
    /** The current item, or null while none is resolved. */
    val selectedNavigationItem: NavigationItem?,
    /** Whether to draw the navigation surface at all — false on a full-screen destination. */
    val shouldShowNavigation: Boolean,
)
