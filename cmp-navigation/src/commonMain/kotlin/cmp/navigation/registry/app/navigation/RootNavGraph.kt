/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cmp.navigation.registry.app.instance.InstanceSelectorScreen
import cmp.navigation.registry.app.ui.MifosApp
import kpt.core.data.util.NetworkMonitor
import kpt.core.data.util.TimeZoneMonitor
import kpt.feature.passcode.biometricSetupScreen
import kpt.feature.passcode.navigateToBiometricSetupScreen
import kpt.feature.passcode.reAuthMifosPasscodeScreen
import kpt.feature.passcode.rootMifosPasscodeScreen
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus

/**
 * Root nav graph composing the three login-perimeter destinations and the
 * authenticated `:main` graph below them.
 *
 * Destinations registered here:
 *  - `loginNavGraph` — username/password entry; on success calls into
 *    `navigateToRootMifosPasscodeScreen`.
 *  - `rootMifosPasscodeScreen` — first-time passcode creation **and** post-
 *    login passcode unlock (same screen, behaviour driven by whether the
 *    library finds a stored passcode). On `Created`, routes to either the
 *    biometric setup screen (if hardware is available) or directly to
 *    `:main`. On `Verified`, routes to `:main`. On `Forgotten`, calls
 *    [onClickLogout].
 *  - `biometricSetupScreen` — first-time biometric enrolment; both the
 *    "enrol" and "skip" branches land on `:main`.
 *  - `reAuthMifosPasscodeScreen` — pushed on top by `MifosPayApp` when the
 *    user backgrounds the app for >15 s; pops back to the prior destination
 *    on `Verified`.
 *  - `MifosNavGraph.MAIN_GRAPH` — the authenticated content host
 *    ([cmp.navigation.registry.app.ui.MifosApp]).
 *
 * Biometric availability — [PlatformAuthenticatorStatus.BIOMETRICS_SET] in
 * the provider's `authenticatorStatus` flow — is read once and used to gate
 * the biometric-setup detour. If hardware enrolment changes mid-session the
 * routing decision stays as-of-composition.
 *
 * @param networkMonitor Forwarded down to `:main` for offline banner.
 * @param timeZoneMonitor Forwarded down to `:main`.
 * @param navHostController Hoisted by the caller so background re-auth
 *        navigation in `MifosPayApp` can target the same controller.
 * @param startDestination Resolved by the caller from session state — see
 *        `MifosPayApp.navDestination`.
 * @param onClickLogout Invoked from the passcode "Forgot" path and the global
 *        logout button in `:main`. Should clear session state and return the
 *        user to [LOGIN_GRAPH].
 */
@Composable
internal fun RootNavGraph(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    handleAppLocale: (locale: String?) -> Unit,
    navHostController: NavHostController,
    startDestination: String,
    onClickLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showInstanceSelector by remember { mutableStateOf(false) }

    val systemAuthProvider = platformAuthenticationProvider.current
    val authenticatorStatus by systemAuthProvider.authenticatorStatus.collectAsStateWithLifecycle()

    val isBiometricsAvailable =
        authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET)

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        route = MifosNavGraph.ROOT_GRAPH,
        modifier = modifier,
    ) {
        loginNavGraph(
            navController = navHostController,
            onShowInstanceSelector = { showInstanceSelector = true },
        )

        rootMifosPasscodeScreen(
            navigateToLogin = {
                onClickLogout()
            },
            onAuthenticationSuccess = {
                navHostController.popBackStack()
                navHostController.navigateToMainGraph()
            },
            onPasscodeCreation = {
                navHostController.popBackStack()
                if (isBiometricsAvailable) {
                    navHostController.navigateToBiometricSetupScreen()
                } else {
                    navHostController.navigateToMainGraph()
                }
            },
        )

        biometricSetupScreen(
            onBiometricsRegistrationSuccess = {
                navHostController.popBackStack()
                navHostController.navigateToMainGraph()
            },
            onSkipBiometricSetup = {
                navHostController.popBackStack()
                navHostController.navigateToMainGraph()
            },
        )

        reAuthMifosPasscodeScreen(
            navigateToLogin = {
                navHostController.popBackStack()
                onClickLogout()
            },
            onAuthenticationSuccess = { navHostController.popBackStack() },
        )

        composable(MifosNavGraph.MAIN_GRAPH) {
            MifosApp(
                networkMonitor = networkMonitor,
                timeZoneMonitor = timeZoneMonitor,
                handleAppLocale = handleAppLocale,
                onClickLogout = onClickLogout,
            )
        }
    }

    if (showInstanceSelector) {
        InstanceSelectorScreen(
            onDismiss = { showInstanceSelector = false },
        )
    }
}
