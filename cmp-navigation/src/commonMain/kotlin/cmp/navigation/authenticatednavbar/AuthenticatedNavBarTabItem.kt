/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

import androidx.compose.ui.graphics.vector.ImageVector
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.home
import cmp.navigation.generated.resources.profile
import cmp.navigation.utils.toObjectNavigationRoute
import kpt.core.designsystem.icon.AppIcons
import kpt.core.ui.navigation.NavigationItem
import kpt.feature.home.HomeDestination
import kpt.feature.home.HomeRoute
import kpt.feature.profile.ProfileRoute
import org.jetbrains.compose.resources.StringResource

/**
 * The bottom-bar tabs. A sealed hierarchy rather than a list so the bar and the nav graph cannot drift — adding a tab
 * is a compile error until both are updated.
 */
sealed class AuthenticatedNavBarTabItem : NavigationItem {

    /** The dashboard tab — the bar's start destination, and where a back press from any other tab lands. */
    data object HomeTab : AuthenticatedNavBarTabItem() {
        override val selectedIcon: ImageVector
            get() = AppIcons.HomeBoarder
        override val icon: ImageVector
            get() = AppIcons.Home
        override val labelRes: StringResource
            get() = Res.string.home
        override val contentDescriptionRes: StringResource
            get() = Res.string.home
        override val graphRoute: String
            get() = HomeDestination.toObjectNavigationRoute()
        override val startDestinationRoute: String
            get() = HomeRoute.toObjectNavigationRoute()
        override val testTag: String
            get() = "HomeTab"
    }

    /**
     * The profile tab. In the template this is a signed-out placeholder; a fork swaps the screen behind the route
     * without touching the bar.
     */
    data object ProfileTab : AuthenticatedNavBarTabItem() {
        override val selectedIcon: ImageVector
            get() = AppIcons.ProfileBoarder
        override val icon: ImageVector
            get() = AppIcons.Profile
        override val labelRes: StringResource
            get() = Res.string.profile
        override val contentDescriptionRes: StringResource
            get() = Res.string.profile
        override val graphRoute: String
            get() = ProfileRoute.toObjectNavigationRoute()
        override val startDestinationRoute: String
            get() = ProfileRoute.toObjectNavigationRoute()
        override val testTag: String
            get() = "ProfileTab"
    }
}
