/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

/**
 * Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.
 */
interface NavigationItem {
    /**
     * The resource ID for the icon representing the tab when it is selected.
     */
    val selectedIcon: ImageVector

    /**
     * Resource id for the icon representing the tab.
     */
    val icon: ImageVector

    /**
     * Resource id for the label describing the tab.
     */
    val labelRes: StringResource

    /**
     * Resource id for the content description describing the tab.
     */
    val contentDescriptionRes: StringResource

    /**
     * Route of the tab's graph.
     */
    val graphRoute: String

    /**
     * Route of the tab's start destination.
     */
    val startDestinationRoute: String

    /**
     * The test tag of the tab.
     */
    val testTag: String

    /**
     * Whether this tab renders INLINE inside the navbar scaffold — its content swaps within the inner
     * NavHost so the bottom bar stays visible and the tab keeps its own back stack (exactly like the
     * backbone Home/Profile tabs) — versus a FULL-SCREEN destination pushed on the outer authenticated
     * graph (the bottom bar is hidden; e.g. an immersive focus-timer that declares
     * `bottom_navigation_visible: false`). Default `true` (inline). A full-screen tab overrides to `false`.
     *
     * An inline extra tab (beyond Home/Profile) MUST also register its start destination via
     * `ForkTabRegistry.extraInlineTabDestinations` so the inner NavHost can host it; otherwise the tab has
     * nothing to render inline.
     */
    val inlineTab: Boolean get() = true
}
