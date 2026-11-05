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

/**
 * Phase 2 nav-chunk (skeleton): the template's authenticated navbar is now a
 * bare BRIDGE for content injected from `:cmp-shared` (`SharedApp`) via the
 * `authenticatedContent` callback plumbed through `ComposeApp` → `RootNavScreen`
 * → [authenticatedGraph].
 *
 * The template's four internal pieces here (bottom-nav scaffold with
 * HomeTab/ProfileTab items, `AuthenticatedNavbarNavigationViewModel`,
 * `AuthenticatedNavBarTabItem`, and `AuthenticatedNavbarNavigationScreen*`)
 * were **deleted** because their `kpt.feature.{home,profile}.*` imports don't
 * exist in the fork's classpath (fork feature modules live under
 * `org.mifospay.feature.*`). The wholesale bridge composes the fork's own
 * `MifosApp` scaffold — which already owns its Scaffold + 4-tab bottom nav
 * (HOME/PAYMENTS/FINANCE/HISTORY per `org.mifospay.shared.utils.TopLevelDestination`)
 * + top-app-bar + `MifosNavHost` — inside this single composable slot.
 *
 * TODO(phase-per-feature): convert to typed routes — each of the ~60 fork
 * destinations currently mounted wholesale inside `MifosNavHost` migrates to a
 * `composable<AppRoute.X>` registration at THIS layer, replacing the wholesale
 * bridge with typed-route registrations one feature at a time.
 */
internal fun NavGraphBuilder.authenticatedNavbarGraph(
    authenticatedContent: @Composable () -> Unit,
) {
    composableWithStayTransitions<AuthenticatedNavbarRoute> {
        authenticatedContent()
    }
}
