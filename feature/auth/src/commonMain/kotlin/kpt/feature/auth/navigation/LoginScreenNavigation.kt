/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kpt.feature.auth.login.LoginScreen
/** Route string for the login destination. Accepts an optional `username` query arg. */
const val LOGIN_ROUTE = "login_route"

/**
 * Registers the login destination.
 *
 * @param navigateToPasscode Fired by `LoginViewModel` on
 *        successful authentication. Caller should bind this to
 *        `navController::navigateToRootPasscodeScreen` (the post-login
 *        passcode-create-or-unlock screen). The token has just been
 *        persisted by `LoginUseCase` at this point, so the passcode screen
 *        and any subsequent self-service API calls will see it.
 */
fun NavGraphBuilder.loginScreen(
    onNavigateBack: () -> Unit,
    navigateToPasscode: () -> Unit,
    onNavigateToSignupScreen: () -> Unit,
    onShowInstanceSelector: () -> Unit,
) {
    composable(
        route = "$LOGIN_ROUTE?username={username}",
        arguments = listOf(
            navArgument("username") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        LoginScreen(
            onNavigateBack = onNavigateBack,
            navigateToPasscode = navigateToPasscode,
            navigateToSignupScreen = onNavigateToSignupScreen,
            onShowInstanceSelector = onShowInstanceSelector,
        )
    }
}

/**
 * Pushes [LOGIN_ROUTE]. Pre-fills the username field with [username] if
 * non-empty (used by sign-up → login hand-off).
 */
fun NavController.navigateToLogin(username: String = "") {
    this.navigate("$LOGIN_ROUTE?username=$username")
}
