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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import cmp.navigation.registry.ForkBackboneRegistry
import cmp.navigation.registry.ForkTabRegistry
import cmp.navigation.ui.KptRootScaffold
import cmp.navigation.ui.ScaffoldNavigationData
import cmp.navigation.ui.logDestinationChanged
import cmp.navigation.ui.rememberKptNavController
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.rememberAnalyticsHelper
import kotlinx.collections.immutable.toImmutableList
import kpt.core.base.designsystem.theme.motion
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.util.RootTransitionProviders
import kpt.feature.home.HomeDestination
import kpt.feature.home.homeGraph
import kpt.feature.home.navigateToHome
import kpt.feature.profile.navigateToProfile
import kpt.feature.profile.profileDestination
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AuthenticatedNavbarNavigationScreen(
    navigateToSettingsScreen: () -> Unit,
    homeBody: @Composable () -> Unit,
    // The OUTER authenticated-graph controller. Used to (a) push a FULL-SCREEN tab (inlineTab = false,
    // e.g. an immersive focus modal) above the scaffold, and (b) hand INLINE fork tabs a controller for
    // their drill-downs via ForkTabRegistry.extraInlineTabDestinations. Threaded from authenticatedGraph so
    // this merge-owned shell stays free of feature imports.
    outerNavController: NavController,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberKptNavController(
        name = "AuthenticatedNavbarScreen",
    ),
    viewModel: AuthenticatedNavbarNavigationViewModel = koinViewModel(),
) {
    val analyticsHelper = rememberAnalyticsHelper()

    EventsEffect(eventFlow = viewModel.eventFlow) { event ->
        navController.apply {
            when (event) {
                AuthenticatedNavBarEvent.NavigateToHomeScreen -> {
                    analyticsHelper.logDestinationChanged(event.tab.startDestinationRoute)
                    navigateToTabOrRoot(tabToNavigateTo = event.tab) {
                        navigateToHome(navOptions = it)
                    }
                }

                AuthenticatedNavBarEvent.NavigateToProfileScreen -> {
                    analyticsHelper.logDestinationChanged(event.tab.startDestinationRoute)
                    navigateToTabOrRoot(tabToNavigateTo = event.tab) {
                        navigateToProfile(navOptions = it)
                    }
                }
            }
        }
    }

    AuthenticatedNavbarNavigationScreenContent(
        navController = navController,
        outerNavController = outerNavController,
        modifier = modifier,
        navigateToSettingsScreen = navigateToSettingsScreen,
        homeBody = homeBody,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
internal fun AuthenticatedNavbarNavigationScreenContent(
    navController: NavHostController,
    outerNavController: NavController,
    navigateToSettingsScreen: () -> Unit,
    homeBody: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (AuthenticatedNavBarAction) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Tabs from the fork-owned ForkTabRegistry seam (backbone Home/Profile + fork extraTabs) — the
    // navbar renders the list generically instead of a hardcoded item list (S6 heal, T7).
    val navigationItems = ForkTabRegistry.tabs.toImmutableList()

    KptRootScaffold(
        contentWindowInsets = WindowInsets(0.dp),
        navigationData = ScaffoldNavigationData(
            navigationItems = navigationItems,
            selectedNavigationItem = navigationItems.find {
                navBackStackEntry.isCurrentRoute(route = it.graphRoute)
            },
            onNavigationClick = { navigationItem ->
                when (navigationItem) {
                    is AuthenticatedNavBarTabItem.HomeTab -> {
                        onAction(AuthenticatedNavBarAction.HomeTabClick)
                    }

                    is AuthenticatedNavBarTabItem.ProfileTab -> {
                        onAction(AuthenticatedNavBarAction.SettingsTabClick)
                    }

                    // Fork extra tabs. INLINE tabs (inlineTab = true — the default) swap content inside
                    // THIS inner NavHost so the bottom bar persists and the tab keeps its own back stack
                    // (their top screen is registered by ForkTabRegistry.extraInlineTabDestinations below).
                    // FULL-SCREEN tabs (inlineTab = false, e.g. an immersive focus modal that declares
                    // bottom_navigation_visible: false) push their route on the OUTER graph, hiding the bar.
                    else -> if (navigationItem.inlineTab) {
                        navController.navigateToInlineTab(navigationItem.startDestinationRoute)
                    } else {
                        outerNavController.navigate(
                            navigationItem.startDestinationRoute,
                            navOptions { launchSingleTop = true },
                        )
                    }
                }
            },
            shouldShowNavigation = navigationItems.any {
                navBackStackEntry.isCurrentRoute(route = it.startDestinationRoute)
            },
        ),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
    ) {
        // Because this Scaffold has a bottom navigation bar, the NavHost will:
        // - consume the vertical navigation bar insets.
        // - consume the IME insets.
        // Snapshot motion tokens once so the non-Composable enterTransition lambdas capture
        // theme-resolved values rather than the hardcoded fallbacks.
        val motion = MaterialTheme.motion
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            // Sibling navigation (bottom-nav tab switch) uses M3 fade-through pattern.
            enterTransition = RootTransitionProviders.Kpt.Enter.fadeThrough(motion),
            exitTransition = RootTransitionProviders.Kpt.Exit.fadeThrough(motion),
            popEnterTransition = RootTransitionProviders.Kpt.Enter.fadeThrough(motion),
            popExitTransition = RootTransitionProviders.Kpt.Exit.fadeThrough(motion),
        ) {
            // TOP LEVEL DESTINATIONS
            homeGraph(
                onSettingsClick = navigateToSettingsScreen,
                homeBody = homeBody,
            )

            // Profile inner content from the fork-owned ForkBackboneRegistry.profileBody seam (default: demo
            // body). The template shell forwards this opaque body — a fork edits ForkBackboneRegistry, not this
            // file. (WS01 base-feature seam, epic AC7 — mirrors the homeBody wiring.)
            profileDestination(profileBody = { ForkBackboneRegistry.profileBody(navController) })

            // Fork INLINE extra-tab top screens (ForkTabRegistry.extraInlineTabDestinations). Registered in
            // THIS inner NavHost so tapping the tab swaps content inside the scaffold (bottom bar stays)
            // — the fork wires each screen's drill-downs on outerNavController (full-screen). Template
            // default = no-op. Keeps the merge-owned shell feature-import-free (the seam lives in the fork).
            ForkTabRegistry.extraInlineTabDestinations.invoke(this, navController, outerNavController)
        }
    }
}

/**
 * Switch to an INLINE extra tab (its top screen registered in the inner NavHost via
 * [ForkTabRegistry.extraInlineTabDestinations]). Mirrors [navigateToTabOrRoot]'s bottom-nav semantics for a
 * plain route string: no-op if already on the tab; otherwise pop up to the graph start saving state,
 * single-top, and restore the tab's saved back stack — so each tab keeps its own state across switches.
 */
private fun NavHostController.navigateToInlineTab(route: String) {
    if (currentDestination?.hierarchy?.any { it.route == route } == true) return
    navigate(
        route,
        navOptions {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        },
    )
}

private fun NavController.navigateToTabOrRoot(
    tabToNavigateTo: AuthenticatedNavBarTabItem,
    navigate: (NavOptions) -> Unit,
) {
    if (tabToNavigateTo.startDestinationRoute == currentDestination?.route) {
        return
    } else if (currentDestination?.parent?.route == tabToNavigateTo.graphRoute) {
        popBackStack(route = tabToNavigateTo.startDestinationRoute, inclusive = false)
    } else {
        navigate(
            navOptions {
                popUpTo(graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            },
        )
    }
}

private fun NavBackStackEntry?.isCurrentRoute(route: String): Boolean = this
    ?.destination
    ?.hierarchy
    ?.any { it.route == route } == true
