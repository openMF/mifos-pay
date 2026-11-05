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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import cmp.navigation.AppEvent
import cmp.navigation.AppViewModel
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
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.util.NonNullEnterTransitionProvider
import kpt.core.base.ui.util.NonNullExitTransitionProvider
import kpt.core.base.ui.util.RootTransitionProviders
import kpt.feature.onboarding.OnboardingRoute
import kpt.feature.onboarding.navigateToOnboarding
import kpt.feature.onboarding.onboardingDestination
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.feature.passcode.biometricSetupScreen
import org.mifos.feature.passcode.navigateToBiometricSetupScreen
import org.mifos.feature.passcode.navigateToReAuthMifosPasscodeScreen
import org.mifos.feature.passcode.reAuthMifosPasscodeScreen
import org.mifos.feature.passcode.rootMifosPasscodeScreen
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Phase 2 nav-chunk (skeleton). Wires:
 *
 *  1. **Login/auth graph** — registered via the [authGraphBuilder] callback
 *     supplied by `SharedApp` (which composes `loginNavGraph(...)` — the fork's
 *     graph in `:cmp-shared/navigation/LoginNavGraph.kt` — with
 *     `onShowInstanceSelector = appViewModel::showInstanceSelectorSheet`).
 *     Injected as a NavGraphBuilder-receiver lambda so `:cmp-navigation` avoids
 *     a reverse-direction Gradle dependency on `:cmp-shared` (same pattern
 *     established by `instanceSelectorOverlay` in `ComposeApp`).
 *
 *  2. **Passcode destinations** — `rootMifosPasscodeScreen()`,
 *     `biometricSetupScreen()`, `reAuthMifosPasscodeScreen()` from
 *     `:feature:passcode` (already a `cmp-navigation` dependency). Mirrors the
 *     fork's `RootNavGraph.kt` registrations. `rootMifosPasscodeScreen` is
 *     targeted after login (via `navigateToRootMifosPasscodeScreen`, called
 *     from `loginScreen`'s `navigateToMifosPasscodeScreen` callback inside
 *     `loginNavGraph`); on `Verified` it hops to the authenticated graph.
 *
 *  3. **ReAuthRequired last-mile** — [EventsEffect] observes
 *     [AppViewModel.eventFlow] and pushes the re-auth passcode screen on
 *     [AppEvent.ReAuthRequired]. `AppEvent.LogoutRequested` is still handled by
 *     `ComposeApp`'s own EventsEffect (it fans out to
 *     `onSessionLogOut = mifosPayViewModel::logOut`) — we don't duplicate that
 *     here; RootNavViewModel independently routes to `RootNavState.Auth` when
 *     the 401 flag flips.
 *
 *  4. **RootNavState branches** — `Auth` now navigates to [authGraphRoute]
 *     (the fork's `MifosNavGraph.LOGIN_GRAPH` string, supplied via callback),
 *     `UserUnlocked` continues to hop to `AuthenticatedGraphRoute`. `UserLocked`
 *     / `ShowOnboarding` remain no-ops — the fork's `RootNavViewModel` never
 *     emits them (see its KDoc: only `Splash | Auth | UserUnlocked` are live).
 *     The `when` remains exhaustive so a future emission surfaces a compile
 *     error, not silent nothing.
 *
 * @param authGraphBuilder NavGraphBuilder-receiver lambda injected by
 *        `SharedApp` — registers the fork's `loginNavGraph(...)` inside this
 *        NavHost. Default no-op keeps template-only variants compiling.
 * @param authGraphRoute Route string the [RootNavState.Auth] transition
 *        targets. Default `""` means "no login graph wired" (skip the
 *        navigation) so the template variant doesn't crash on missing route.
 *        `SharedApp` supplies `org.mifospay.shared.navigation.MifosNavGraph.LOGIN_GRAPH`.
 * @param authenticatedContent Composable slot rendered inside the
 *        `AuthenticatedNavbarRoute` bridge — `SharedApp` fills with the fork's
 *        `MifosApp(...)`, which owns its own scaffold + 4-tab bottom nav +
 *        `MifosNavHost` (all ~60 fork feature destinations). See
 *        `authenticatednavbar/AuthenticatedNavbarNavigation.kt` KDoc for the
 *        wholesale-bridge rationale + per-feature typed-route follow-up.
 * @param onSessionLogOut Fork-supplied session-clear (default no-op).
 *        `RootNavScreen` doesn't invoke this itself — `ComposeApp` already
 *        wires it against `AppEvent.LogoutRequested`. Passed through for
 *        symmetry / future use.
 */
@OptIn(ExperimentalAtomicApi::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun RootNavScreen(
    modifier: Modifier = Modifier,
    viewModel: RootNavViewModel = koinViewModel(),
    appViewModel: AppViewModel = koinViewModel(),
    navController: NavHostController = rememberKptNavController(name = "RootNavScreen"),
    onSplashScreenRemoved: () -> Unit = {},
    authGraphBuilder: NavGraphBuilder.(NavController) -> Unit = {},
    authGraphRoute: String = "",
    authenticatedContent: @Composable () -> Unit = {},
    onSessionLogOut: () -> Unit = {},
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val previousStateReference = remember { AtomicReference(state) }

    val isNotSplashScreen = state != RootNavState.Splash
    LaunchedEffect(isNotSplashScreen) {
        if (isNotSplashScreen) onSplashScreenRemoved()
    }

    // (Task item 4) ReAuthRequired last-mile: push the re-auth passcode screen
    // on background-resume qualifying event from AppViewModel. AppEvent.LogoutRequested
    // is handled ONLY in ComposeApp (avoid double-consumption — EventsEffect uses
    // the eventFlow which is a single SharedFlow, but the filter guards on lifecycle
    // — both collectors receive each emission; keeping the logout handler in one
    // place prevents the fork's session-clear from firing twice).
    EventsEffect(eventFlow = appViewModel.eventFlow) { event ->
        when (event) {
            is AppEvent.ReAuthRequired -> navController.navigateToReAuthMifosPasscodeScreen()
            else -> Unit
        }
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
                // First-run onboarding (feature/onboarding, template) — installed once here; only reached when
                // app-profile security.first_run_onboarding is true (the fork RootNavViewModel never emits it).
                onboardingDestination()

                // (Task item 3) Login/auth graph — injected by SharedApp as
                // `loginNavGraph(navController, onShowInstanceSelector = appViewModel::showInstanceSelectorSheet)`.
                // The `LoginScreen` inside `feature/auth/.../login/LoginScreen.kt`
                // already accepts `onShowInstanceSelector: () -> Unit` and hooks
                // it to a `detectMultiTapGesture` — no LoginScreen-side change is
                // required, just the caller hookup happens inside `SharedApp`
                // (which passes `appViewModel::showInstanceSelectorSheet` — the
                // SAME `AppViewModel` singleton that `ComposeApp` and this
                // screen resolve via `koinViewModel()`). Invoked via `.invoke`
                // with explicit receiver so Kotlin resolves the extension
                // function-type variable unambiguously inside the NavGraphBuilder
                // scope (avoids the "extension function type value called as
                // regular function" resolution edge case).
                authGraphBuilder.invoke(this, navController)

                // (Task item 3 cont.) Passcode/auth-perimeter destinations —
                // registered here rather than in the loginNavGraph so cmp-navigation
                // owns the perimeter (`:feature:passcode` is already a direct
                // `cmp-navigation` dep). Mirrors the fork's `RootNavGraph.kt`
                // registrations at cmp-shared. `LoginScreen` calls
                // `navigateToRootMifosPasscodeScreen` on successful auth, which
                // targets `rootMifosPasscodeScreen` below.
                rootMifosPasscodeScreen(
                    navigateToLogin = onSessionLogOut,
                    onAuthenticationSuccess = {
                        navController.popBackStack()
                        navController.navigateToAuthenticatedGraph()
                    },
                    onPasscodeCreation = {
                        navController.popBackStack()
                        // Biometrics-available branching lives inside
                        // `BiometricSetupScreen` (it detects hardware itself and
                        // exposes a "skip" path); avoid duplicating the
                        // `platformAuthenticationProvider.current.authenticatorStatus`
                        // observation here — that read was in the fork's
                        // `RootNavGraph.kt` but forces a Composable context.
                        // Route unconditionally to the setup screen; its own
                        // skip path lands on the authenticated graph.
                        navController.navigateToBiometricSetupScreen()
                    },
                )

                biometricSetupScreen(
                    onBiometricsRegistrationSuccess = {
                        navController.popBackStack()
                        navController.navigateToAuthenticatedGraph()
                    },
                    onSkipBiometricSetup = {
                        navController.popBackStack()
                        navController.navigateToAuthenticatedGraph()
                    },
                )

                reAuthMifosPasscodeScreen(
                    navigateToLogin = {
                        navController.popBackStack()
                        onSessionLogOut()
                    },
                    onAuthenticationSuccess = { navController.popBackStack() },
                )

                authenticatedGraph(
                    navController = navController,
                    authenticatedContent = authenticatedContent,
                )
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

    // (Task item 4) RootNavState → target route.
    // `authGraphRoute` is a plain string (fork's `MifosNavGraph.LOGIN_GRAPH`);
    // `SplashRoute` / `AuthenticatedGraphRoute` are typed serializable data
    // objects converted via `toObjectNavigationRoute()`. Use their string form
    // uniformly so the short-circuit `currentRoute == targetRoute` comparison
    // (process-death restore path) works for all branches.
    val targetRoute: String = when (state) {
        // Template first-run onboarding; the fork RootNavViewModel never emits ShowOnboarding.
        RootNavState.ShowOnboarding -> OnboardingRoute.toObjectNavigationRoute()
        RootNavState.Auth -> authGraphRoute
        RootNavState.Splash -> SplashRoute.toObjectNavigationRoute()
        // UserUnlockRoute.Standard — fork never emits UserLocked (see
        // `RootNavViewModel` KDoc). If it ever does, route to the root
        // passcode gate here (uncomment when the fork wires that path).
        RootNavState.UserLocked -> ""
        is RootNavState.UserUnlocked -> AuthenticatedGraphRoute.toObjectNavigationRoute()
    }
    val currentRoute = navController.currentDestination?.rootLevelRoute()

    // Don't navigate if we are already at the correct root. This notably happens during process
    // death. In this case, the NavHost already restores state, so we don't have to navigate.
    // However, if the route is correct but the underlying state is different, we should still
    // proceed in order to get a fresh version of that route.
    if (currentRoute == targetRoute && previousStateReference.load() == state) {
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
            // (Task item 3 last-mile) Fork's login graph is registered above
            // via `authGraphBuilder(navController)`; target it by string route.
            // Guard on `isNotEmpty()` so the template-only variant (which
            // supplies `authGraphRoute = ""`) doesn't crash on missing route.
            RootNavState.Auth -> if (authGraphRoute.isNotEmpty()) {
                navController.navigate(authGraphRoute, rootNavOptions)
            }
            // navController.navigateToSetLanguage(rootNavOptions)
            RootNavState.ShowOnboarding -> navController.navigateToOnboarding(rootNavOptions)
            // navController.navigateToUserUnlock(rootNavOptions)
            // TODO(phase-2-nav): when the fork wires an app-locked branch, route
            // here to `ROOT_MIFOS_PASSCODE_ROUTE` (already registered above).
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

@Composable
expect fun ClearFocus()
