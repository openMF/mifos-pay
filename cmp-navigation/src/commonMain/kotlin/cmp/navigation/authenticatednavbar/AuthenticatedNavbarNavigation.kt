/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package cmp.navigation.authenticatednavbar

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions

/** Route for the signed-in shell that hosts the bottom bar. */
@Serializable
data object AuthenticatedNavbarRoute

internal fun NavController.navigateToAuthenticatedNavBar(navOptions: NavOptions? = null) {
    navigate(route = AuthenticatedNavbarRoute, navOptions = navOptions)
}

internal fun NavGraphBuilder.authenticatedNavbarGraph(
    navigateToSettingsScreen: () -> Unit,
    homeBody: @Composable () -> Unit,
    // The OUTER authenticated-graph controller, threaded down to the navbar screen so full-screen fork
    // tabs push above the scaffold and inline fork tabs can drill down full-screen. Passed from
    // authenticatedGraph (which owns it) — keeps this graph + the shell feature-free.
    outerNavController: NavController,
) {
    composableWithStayTransitions<AuthenticatedNavbarRoute> {
        AuthenticatedNavbarNavigationScreen(
            navigateToSettingsScreen = navigateToSettingsScreen,
            homeBody = homeBody,
            outerNavController = outerNavController,
        )
    }
}
