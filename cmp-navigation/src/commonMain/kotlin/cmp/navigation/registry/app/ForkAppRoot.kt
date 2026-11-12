/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import cmp.navigation.registry.app.UserState.Authenticated
import cmp.navigation.registry.app.navigation.ForkNavGraph.LOGIN_GRAPH
import cmp.navigation.registry.app.navigation.RootNavGraph
import co.touchlab.kermit.Logger
import kpt.core.common.GlobalAuthManager
import kpt.core.data.fork.util.ForkNetworkMonitorQualifier
import kpt.core.data.fork.util.ForkTimeZoneMonitorQualifier
import kpt.core.data.fork.util.NetworkMonitor
import kpt.core.data.fork.util.TimeZoneMonitor
import kpt.core.designsystem.fork.component.ForkDialogBox
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.feature.passcode.ROOT_PASSCODE_ROUTE
import kpt.feature.passcode.navigateToReAuthPasscodeScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.authenticator.biometrics.BiometricStorageAdapter
import org.mifos.authenticator.biometrics.PlatformAuthenticatorCompositionProvider
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * The wallet's app root, rendered by [cmp.navigation.registry.ForkAppShell.root] inside the
 * template's `ComposeApp` (theme, layout direction) — so every platform entry point reaches it
 * through the template's `SharedApp`.
 *
 * Wraps the entire UI tree in [PlatformAuthenticatorCompositionProvider] so the
 * `platformAuthenticationProvider` and `platformAvailableAuthenticationOption`
 * composition locals from the biometrics library resolve everywhere downstream.
 * Without this wrapper, any descendant that reads those locals (passcode
 * screen, settings biometrics toggle, intra-bank transfer auth gate) crashes at
 * compose time.
 *
 * @param modifier Modifier forwarded to the root [RootNavGraph].
 * @param handleAppLocale Applies a locale on the platform; the template provides it.
 * @param onSplashScreenRemoved Called once the first frame is up — Android holds its splash until then.
 * @param networkMonitor Connectivity observer; defaults to the Koin singleton.
 * @param timeZoneMonitor Time-zone change observer; defaults to the Koin
 *        singleton.
 */
@Composable
fun ForkAppRoot(
    handleAppLocale: (locale: String?) -> Unit,
    onSplashScreenRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    networkMonitor: NetworkMonitor = koinInject(ForkNetworkMonitorQualifier),
    timeZoneMonitor: TimeZoneMonitor = koinInject(ForkTimeZoneMonitorQualifier),
) {
    LaunchedEffect(Unit) { onSplashScreenRemoved() }
    val biometricStorageAdapter: BiometricStorageAdapter = koinInject()
    PlatformAuthenticatorCompositionProvider(
        biometricStorageAdapter = biometricStorageAdapter,
    ) {
        ForkAppContent(
            networkMonitor = networkMonitor,
            timeZoneMonitor = timeZoneMonitor,
            handleAppLocale = handleAppLocale,
            modifier = modifier,
        )
    }
}

/**
 * Internal app-shell composable hosting the [RootNavGraph].
 *
 * Owns three cross-cutting concerns that need a stable composition root:
 *  - **Background re-auth gate** — observes `Lifecycle.Event.ON_STOP` /
 *    `ON_RESUME`; on resume, pushes the re-auth passcode screen iff **all
 *    four** conditions hold: backgrounded for > [lockTimeOut] (15 s), the
 *    lock flag is non-null and `false` (= an established session that was
 *    actively unlocked, not a fresh install), and
 *    `passcodeManager.state.value.passcodeStep == Enter` (the passcode
 *    surface is in unlock mode, not a creation/change flow). The `null`
 *    branch of [AppLockRepository.isAppLocked] is treated as "no session
 *    yet, don't gate."
 *  - **Unauthorized 401 dialog** — observes [GlobalAuthManager.isUnauthorized]
 *    (set by `KtorInterceptor` on any HTTP 401); on `true`, shows the
 *    "Unauthorized User" dialog whose `onConfirm` logs out, navigates to
 *    [LOGIN_GRAPH], and resets [GlobalAuthManager].
 *  - **Start-destination resolution** — derives `navDestination` from
 *    [ForkAppViewModel.userState]: [UserState.UnAuthenticated] →
 *    [LOGIN_GRAPH]; [UserState.Authenticated] with `userData.authenticated`
 *    true → [ROOT_PASSCODE_ROUTE], else [LOGIN_GRAPH]. This only
 *    seeds the NavHost on first composition; subsequent state changes don't
 *    re-target the start destination.
 *
 * @param passcodeManager Library singleton injected from Koin. Read directly
 *        (rather than going through the VM) so the lifecycle observer can
 *        snapshot `state.value.passcodeStep` synchronously inside the
 *        `ON_RESUME` callback without a Flow subscription.
 */
@OptIn(ExperimentalTime::class)
@Composable
private fun ForkAppContent(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    handleAppLocale: (locale: String?) -> Unit,
    modifier: Modifier = Modifier,
    passcodeManager: PasscodeManager = koinInject(),
    viewModel: ForkAppViewModel = koinViewModel(),
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val showErrorDialog = remember { mutableStateOf<Boolean>(false) }
    val isUnauthorized by GlobalAuthManager.isUnauthorized.collectAsStateWithLifecycle()

    LaunchedEffect(isUnauthorized) {
        if (isUnauthorized && userState !is Authenticated) {
            showErrorDialog.value = true
        }
    }

    LaunchedEffect(Unit) {
        val state = userState
        if (
            state is Authenticated &&
            state.userData.authenticated &&
            viewModel.isPasscodeNotCreated()
        ) {
            viewModel.logOut()
        }
    }

    if (showErrorDialog.value) {
        ForkDialogBox(
            title = "Unauthorized User",
            showDialogState = showErrorDialog.value,
            confirmButtonText = "Ok",
            onConfirm = {
                showErrorDialog.value = false
                viewModel.logOut()
                navController.navigate(LOGIN_GRAPH) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
                GlobalAuthManager.reset()
            },
            onDismiss = {},
            message = "Please login again to continue",
        )
    }

    val navDestination = startDestination(userState, viewModel.isPasscodeNotCreated())

    ReAuthOnResume(
        isAppLocked = viewModel::isAppLocked,
        passcodeManager = passcodeManager,
        onReAuthRequired = { navController.navigateToReAuthPasscodeScreen() },
    )

    ForkTheme {
        RootNavGraph(
            networkMonitor = networkMonitor,
            timeZoneMonitor = timeZoneMonitor,
            navHostController = navController,
            startDestination = navDestination,
            modifier = modifier,
            handleAppLocale = handleAppLocale,
            onClickLogout = {
                viewModel.logOut()
                navController.navigate(LOGIN_GRAPH) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            },
        )
    }
}

/**
 * Where the nav graph starts: the passcode screen for a signed-in user who has set a passcode, the
 * login graph otherwise. Only seeds the NavHost; later state changes do not re-target it.
 */
internal fun startDestination(userState: UserState, isPasscodeNotCreated: Boolean): String =
    if (userState is Authenticated && userState.userData.authenticated && !isPasscodeNotCreated) {
        ROOT_PASSCODE_ROUTE
    } else {
        LOGIN_GRAPH
    }

/**
 * Asks for the passcode again when the app returns after more than [lockTimeOut] in the background —
 * only for an established, unlocked session whose passcode surface is in unlock mode.
 */
@OptIn(ExperimentalTime::class)
@Composable
private fun ReAuthOnResume(
    isAppLocked: () -> Boolean?,
    passcodeManager: PasscodeManager,
    onReAuthRequired: () -> Unit,
    lockTimeOut: Duration = 15.seconds,
) {
    // Monotonic mark of the most recent ON_STOP. `null` means "no stop recorded yet" or "already
    // consumed on a previous ON_RESUME" — either way, ON_RESUME skips the gate. A monotonic clock
    // defeats clock-rewind: a user could otherwise background the app, roll the device clock back,
    // and resume far past the window without re-auth.
    val onStopMark = remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val inactiveTime = onStopMark.value?.elapsedNow()
                    onStopMark.value = null
                    if (inactiveTime != null) {
                        Logger.a { "inactiveTime: ${inactiveTime.inWholeSeconds}s" }
                        val unlocked = isAppLocked() == false
                        val unlockMode = passcodeManager.state.value.passcodeStep == PasscodeStep.Enter
                        if (inactiveTime > lockTimeOut && unlocked && unlockMode) onReAuthRequired()
                    }
                }
                Lifecycle.Event.ON_STOP -> onStopMark.value = TimeSource.Monotonic.markNow()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}
