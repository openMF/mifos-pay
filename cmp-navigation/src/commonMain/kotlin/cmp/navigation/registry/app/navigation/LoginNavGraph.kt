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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kpt.feature.auth.navigation.LOGIN_ROUTE
import kpt.feature.auth.navigation.loginScreen
import kpt.feature.auth.navigation.mobileVerificationScreen
import kpt.feature.auth.navigation.navigateToLogin
import kpt.feature.auth.navigation.navigateToSignup
import kpt.feature.auth.navigation.signupScreen
import kpt.feature.auth.socialSignup.navigateToSignupMethod
import kpt.feature.auth.socialSignup.signupMethodScreen
import kpt.feature.passcode.navigateToRootPasscodeScreen
internal fun NavGraphBuilder.loginNavGraph(
    navController: NavController,
    onShowInstanceSelector: () -> Unit,
) {
    navigation(
        route = ForkNavGraph.LOGIN_GRAPH,
        startDestination = LOGIN_ROUTE,
    ) {
        loginScreen(
            onNavigateBack = navController::popBackStack,
            navigateToPasscode = navController::navigateToRootPasscodeScreen,
            onNavigateToSignupScreen = navController::navigateToSignupMethod,
            onShowInstanceSelector = onShowInstanceSelector,
        )

        signupMethodScreen(
            onNavigateBack = navController::popBackStack,
            onNavigateToSignUp = {
                navController.navigateToSignup(savingsProductId = it)
            },
        )

        signupScreen(
            onNavigateBack = navController::popBackStack,
            onNavigateToLogin = navController::navigateToLogin,
        )

        mobileVerificationScreen(
            onNavigateBack = navController::popBackStack,
            onOtpVerificationSuccess = { fullNumber ->
                navController.navigateToSignup(mobileNumber = fullNumber)
            },
        )
    }
}
