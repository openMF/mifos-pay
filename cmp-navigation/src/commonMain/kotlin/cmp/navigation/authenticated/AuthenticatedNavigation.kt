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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.authenticatednavbar.authenticatedNavbarGraph
import cmp.navigation.registry.ForkBackboneRegistry
import cmp.navigation.registry.ForkFeatureRegistry
import cmp.navigation.registry.ForkShowcaseRegistry
import kotlinx.serialization.Serializable
// NOTE: this merge-owned shell carries zero direct feature imports — all backbone, fork-feature,
// and dev-only destinations flow through the three registry seams imported above.

@Serializable
internal data object AuthenticatedGraphRoute

internal fun NavController.navigateToAuthenticatedGraph(navOptions: NavOptions? = null) {
    navigate(route = AuthenticatedGraphRoute, navOptions = navOptions)
}

internal fun NavGraphBuilder.authenticatedGraph(navController: NavController) {
    navigation<AuthenticatedGraphRoute>(
        startDestination = AuthenticatedNavbarRoute,
    ) {
        authenticatedNavbarGraph(
            navigateToSettingsScreen = { ForkBackboneRegistry.navigateToSettings(navController) },
            // Home body from the fork-owned ForkBackboneRegistry seam (default: demo dashboard). The
            // template shell carries zero demo imports — a fork edits ForkBackboneRegistry, not this file.
            homeBody = { ForkBackboneRegistry.homeBody(navController) },
            // The outer graph's controller — the navbar uses it to push full-screen fork tabs above the
            // scaffold and to hand inline fork tabs a controller for their drill-downs. This IS the same
            // graph the fork tabs' destinations register on (ForkFeatureRegistry.featureDestinations below).
            outerNavController = navController,
        )

        // Backbone (framework) destinations — settings, notification, sync-and-drafts.
        ForkBackboneRegistry.backboneDestinations.invoke(this, navController)

        // Fork feature routes — from the fork-owned ForkFeatureRegistry seam (white-label: the template infra
        // never edits this; a fork adds/removes routes in cmp-navigation/registry/ForkFeatureRegistry.kt).
        ForkFeatureRegistry.featureDestinations.invoke(this, navController)

        // Dev-only showcase destinations — from the template-owned ForkShowcaseRegistry seam. A `--clean`
        // fork strips its fenced content, reducing this to a no-op.
        ForkShowcaseRegistry.devDestinations.invoke(this, navController)
    }
}
