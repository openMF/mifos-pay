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
import androidx.navigation.NavHostController
import cmp.navigation.authenticatednavbar.AuthenticatedNavBarTabItem
import kpt.core.ui.navigation.NavigationItem

/**
 * ForkTabRegistry — the FORK-OWNED white-label seam for the authenticated bottom-nav tabs.
 *
 * The template navbar (`AuthenticatedNavbarNavigationScreenContent`) reads [tabs] and renders them
 * generically, so adding/removing a tab is a ONE-file edit here — no longer a scattered change across a
 * sealed-class + item list + click `when` + NavHost. The backbone Home + Profile tabs are always present
 * (they are the app shell); a fork appends its own via [extraTabs]. Ownership: `owner: fork` in
 * customization-surface.yaml (S6 heal, epic pure-white-label-store5-network T7).
 */
object ForkTabRegistry {
    /**
     * Feature-contributed bottom-nav tabs, DERIVED from `@FeatureTab`.
     *
     * `:cmp-navigation:generateFeatureTabs` collects every `@FeatureTab` object across the feature
     * modules into [GeneratedFeatureTabs], so a feature declares its own tab rather than the fork
     * editing this file. Empty until some feature annotates one.
     *
     * An INLINE tab still has to register its top screen on the inner NavHost via
     * [extraInlineTabDestinations] — this list puts the tab in the bar, not its destination.
     *
     */
    val extraTabs: List<NavigationItem> = GeneratedFeatureTabs

    /**
     * Fork registration of INLINE extra-tab TOP destinations into the navbar's INNER NavHost.
     *
     * Any extra tab whose [NavigationItem.inlineTab] is `true` (the default) MUST register its start
     * destination here, so that tapping the tab swaps content WITHIN the scaffold — the bottom bar stays
     * visible and the tab keeps its own back stack, exactly like the backbone Home/Profile tabs. Without
     * an inline registration an `inlineTab = true` tab would have nothing to render inline.
     *
     * The lambda runs with a [NavGraphBuilder] receiver (register destinations here) and receives:
     *  - `innerNav`  — the navbar's inner [NavHostController]; use it for tab-local navigation such as
     *                  returning to the Home tab (`innerNav.navigate(HomeRoute) { … }`).
     *  - `outerNav`  — the OUTER authenticated-graph [NavController]; use it for DRILL-DOWN navigation to
     *                  full-screen feature routes (`outerNav.navigateToItemDetail(id)`), which correctly
     *                  push above the scaffold and hide the bar — the standard drill-down affordance.
     *
     * Registering a tab's top screen here does NOT conflict with also registering it (or its drill-downs)
     * on the outer graph via `ForkFeatureRegistry.featureDestinations` — the two NavHosts are independent, so
     * a screen reachable both as a tab (inline) and as a push from another feature (full-screen) is fine.
     *
     * Template default = no-op (the template ships no inline extra tabs). `owner: fork`.
     */
    val extraInlineTabDestinations: NavGraphBuilder.(innerNav: NavHostController, outerNav: NavController) -> Unit =
        { _, _ -> }

    /** The full ordered tab list the navbar renders: backbone shell tabs + [extraTabs]. */
    val tabs: List<NavigationItem> = buildList {
        add(AuthenticatedNavBarTabItem.HomeTab)
        add(AuthenticatedNavBarTabItem.ProfileTab)
        addAll(extraTabs)
    }
}
