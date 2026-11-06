/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.rootnav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import cmp.navigation.authenticated.AuthenticatedGraphRoute
import cmp.navigation.authenticated.authenticatedGraph
import cmp.navigation.authenticated.navigateToAuthenticatedGraph
import cmp.navigation.registry.ForkDeepLinkRegistry
import cmp.navigation.splash.SplashRoute
import cmp.navigation.splash.navigateToSplash
import cmp.navigation.splash.splashDestination
import cmp.navigation.ui.rememberKptNavController
import cmp.navigation.utils.toObjectNavigationRoute
import com.mobilebytelabs.kmptoolkit.deeplink.DeepLinkHandler
import kpt.core.base.designsystem.theme.motion
import kpt.core.base.ui.KptConnectivityBanner
import kpt.core.base.ui.util.NonNullEnterTransitionProvider
import kpt.core.base.ui.util.NonNullExitTransitionProvider
import kpt.core.base.ui.util.RootTransitionProviders
import kpt.feature.onboarding.OnboardingRoute
import kpt.feature.onboarding.navigateToOnboarding
import kpt.feature.onboarding.onboardingDestination
import org.koin.compose.viewmodel.koinViewModel
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/** Renders whichever destination `RootNavViewModel` resolved: splash, onboarding, auth, lock or the signed-in shell. */
@OptIn(ExperimentalAtomicApi::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun RootNavScreen(
    modifier: Modifier = Modifier,
    viewModel: RootNavViewModel = koinViewModel(),
    navController: NavHostController = rememberKptNavController(name = "RootNavScreen"),
    onSplashScreenRemoved: () -> Unit = {},
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val previousStateReference = remember { AtomicReference(state) }

    val isNotSplashScreen = state != RootNavState.Splash
    LaunchedEffect(isNotSplashScreen) {
        if (isNotSplashScreen) onSplashScreenRemoved()
    }

    // Snapshot theme tokens once so the non-Composable transition lambdas capture
    // theme-resolved providers. Splash → main handoff suppresses motion; other transitions
    // use the M3 fade-through pattern, both honoring MaterialTheme.motion.
    val motion = MaterialTheme.motion
    val fadeThroughEnter = RootTransitionProviders.Kpt.Enter.fadeThrough(motion)
    val fadeThroughExit = RootTransitionProviders.Kpt.Exit.fadeThrough(motion)
    val noEnter = RootTransitionProviders.Kpt.Enter.none
    val noExit = RootTransitionProviders.Kpt.Exit.none

    // Column layout: connectivity stripe always sits above the NavHost.
    // The stripe's outer Box unconditionally claims statusBarsPadding() space so the
    // NavHost below it never sees the status-bar inset — inner TopAppBars start flush
    // against the stripe without double-padding. This covers ALL authenticated routes
    // (including Settings, Loans, etc.) without per-screen wiring.
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        KptConnectivityBanner()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .consumeWindowInsets(WindowInsets.statusBars),
        ) {
            NavHost(
                navController = navController,
                startDestination = SplashRoute,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { pickEnter(fadeThroughEnter, noEnter)(this) },
                exitTransition = { pickExit(fadeThroughExit, noExit)(this) },
                popEnterTransition = { pickEnter(fadeThroughEnter, noEnter)(this) },
                popExitTransition = { pickExit(fadeThroughExit, noExit)(this) },
            ) {
                splashDestination()
                // First-run onboarding (feature/onboarding). Only reached when app-profile/app.yaml
                // sets security.first_run_onboarding: true; a fork with a server-setup screen passes it here.
                onboardingDestination()
//            authNavGraph(navController)
                authenticatedGraph(navController)
//            userUnlockDestination()
            }
        }
    }

    // Deep-link routing (fork-provided via ForkDeepLinkRegistry — the template ships no links). Inbound URIs are
    // captured by cmp-deep-link (KmpToolkit): Android auto-init reads the launch / onNewIntent Intent.data
    // with NO Activity wiring, parsed onto DeepLinkHandler. We read the retained lastReceived (a StateFlow
    // that survives the Splash → graph handoff, so a cold-start widget tap isn't dropped) and dispatch it
    // through the fork's ForkDeepLinkRegistry ONLY once the user is authenticated AND the authenticated graph is
    // on-screen — otherwise the root-state navigation below (which pops to the graph start) would discard the
    // destination. Held (not cleared) until then, so a first-run link resolves after Splash / onboarding.
    val pendingDeepLink by DeepLinkHandler.lastReceived.collectAsStateWithLifecycle()
    val deepLinkBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(pendingDeepLink, deepLinkBackStackEntry, state) {
        val link = pendingDeepLink ?: return@LaunchedEffect
        val atAuthenticatedRoot = deepLinkBackStackEntry?.destination?.rootLevelRoute() ==
            AuthenticatedGraphRoute.toObjectNavigationRoute()
        if (state is RootNavState.UserUnlocked && atAuthenticatedRoot &&
            ForkDeepLinkRegistry.route(link, navController)
        ) {
            DeepLinkHandler.clear()
        }
    }

    val targetRoute = when (state) {
        RootNavState.ShowOnboarding -> OnboardingRoute
        // AuthGraphRoute
        RootNavState.Auth -> ""
        RootNavState.Splash -> SplashRoute
        // UserUnlockRoute.Standard
        RootNavState.UserLocked -> ""
        is RootNavState.UserUnlocked -> AuthenticatedGraphRoute
    }
    val currentRoute = navController.currentDestination?.rootLevelRoute()

    // Don't navigate if we are already at the correct root. This notably happens during process
    // death. In this case, the NavHost already restores state, so we don't have to navigate.
    // However, if the route is correct but the underlying state is different, we should still
    // proceed in order to get a fresh version of that route.
    if (currentRoute == targetRoute.toObjectNavigationRoute() &&
        previousStateReference.load() == state
    ) {
        previousStateReference.store(state)
        return
    }
    previousStateReference.store(state)

    // In some scenarios on an emulator the Activity can leak when recreated
    // if we don't first clear focus anytime we change the root destination.
    ClearFocus()

    // When state changes, navigate to different root navigation state
    val rootNavOptions = navOptions {
        // When changing root navigation state, pop everything else off the back stack:
        popUpTo(navController.graph.id) {
            inclusive = false
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
    }

    // Use a LaunchedEffect to ensure we don't navigate too soon when the app first opens. This
    // avoids a bug that first appeared in Compose Material3 1.2.0-rc01 that causes the initial
    // transition to appear corrupted.
    LaunchedEffect(state) {
        when (state) {
            RootNavState.Splash -> navController.navigateToSplash(rootNavOptions)
            // navController.navigateToAuthGraph(rootNavOptions)
            RootNavState.Auth -> {}
            RootNavState.ShowOnboarding -> navController.navigateToOnboarding(rootNavOptions)
            // navController.navigateToUserUnlock(rootNavOptions)
            RootNavState.UserLocked -> {}
            is RootNavState.UserUnlocked -> navController.navigateToAuthenticatedGraph(
                navOptions = rootNavOptions,
            )
        }
    }
}

private fun NavDestination?.rootLevelRoute(): String? = when {
    this == null -> null
    parent?.route == null -> route
    else -> parent.rootLevelRoute()
}

/**
 * Pick which pre-resolved enter provider applies, based on the target route. Splash → main
 * handoff suppresses animation (the splash has its own exit choreography); everything else
 * gets the M3 fade-through pattern.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.pickEnter(
    fadeThrough: NonNullEnterTransitionProvider,
    none: NonNullEnterTransitionProvider,
): NonNullEnterTransitionProvider = when (targetState.destination.rootLevelRoute()) {
    SplashRoute.toObjectNavigationRoute() -> none
    else -> fadeThrough
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.pickExit(
    fadeThrough: NonNullExitTransitionProvider,
    none: NonNullExitTransitionProvider,
): NonNullExitTransitionProvider = when (initialState.destination.rootLevelRoute()) {
    SplashRoute.toObjectNavigationRoute() -> none
    else -> fadeThrough
}

/**
 * Clear focus when the root destination changes — prevents an Activity leak on Android emulators
 * where a still-focused view holds a reference across recreation. The multiplatform
 * [LocalFocusManager] covers every target in one commonMain impl (Compose clears the underlying
 * platform focus on Android; a no-op where nothing is focused elsewhere), so no per-platform actual
 * is needed.
 */
@Composable
fun ClearFocus() {
    LocalFocusManager.current.clearFocus()
}
