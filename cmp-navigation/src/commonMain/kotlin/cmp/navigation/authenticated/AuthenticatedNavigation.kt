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

package cmp.navigation.authenticated

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.authenticatednavbar.authenticatedNavbarGraph
import kotlinx.serialization.Serializable

@Serializable
internal data object AuthenticatedGraphRoute

internal fun NavController.navigateToAuthenticatedGraph(navOptions: NavOptions? = null) {
    navigate(route = AuthenticatedGraphRoute, navOptions = navOptions)
}

/**
 * Phase 2 nav-chunk (skeleton). Template-demo destinations (bills, calculators,
 * crypto, currency-rates, emi-calculator, loans, macro, rates, showcase, etc.)
 * were stripped — their `kpt.feature.*` modules do not exist in the fork's
 * classpath. Notification/settings top-level registrations were also stripped;
 * those destinations continue to live inside the fork's `MifosNavHost` (see
 * `cmp-shared/.../MifosNavHost.kt`), which is mounted wholesale via the
 * [authenticatedContent] callback supplied by `SharedApp`.
 *
 * The graph STRUCTURE (authenticatedGraph → authenticatedNavbarGraph) is kept
 * intact so future per-feature typed-route conversion can add siblings back
 * beside `authenticatedNavbarGraph` without another shape change.
 */
internal fun NavGraphBuilder.authenticatedGraph(
    navController: NavController,
    authenticatedContent: @Composable () -> Unit,
) {
    navigation<AuthenticatedGraphRoute>(
        startDestination = AuthenticatedNavbarRoute,
    ) {
        authenticatedNavbarGraph(
            authenticatedContent = authenticatedContent,
        )

        // TODO(phase-per-feature): once we convert the fork's ~60 destinations
        // to typed routes at this layer, register them as siblings here — e.g.
        //   notificationDestination(onBackClick = { navController.popBackStackSafely() })
        //   settingsDestination(onBackClick = { navController.popBackStackSafely() })
        //   sendMoneyGraph(navController = navController)
        //   ...
        // Until then, all fork feature destinations are reachable via the
        // wholesale-bridged `MifosNavHost` inside `authenticatedContent`.
    }
}
