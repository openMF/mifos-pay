/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions

/** Type-safe navigation destination of the home graph. */
@Serializable
data object HomeDestination

/** Type-safe route of the home screen. */
@Serializable
data object HomeRoute

/** Navigates to the home destination, optionally with [navOptions]. */
fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeDestination, navOptions)
}

/**
 * The backbone home graph. [homeBody] is the fork-owned home content (default supplied by
 * `cmp-navigation`'s `ForkBackboneRegistry.homeBody`); this template graph carries zero demo imports.
 */
fun NavGraphBuilder.homeGraph(
    onSettingsClick: () -> Unit,
    homeBody: @Composable () -> Unit = {},
) {
    navigation<HomeDestination>(
        startDestination = HomeRoute,
    ) {
        composableWithStayTransitions<HomeRoute> {
            HomeScreen(
                onSettingsClick = onSettingsClick,
                homeBody = homeBody,
            )
        }
    }
}
