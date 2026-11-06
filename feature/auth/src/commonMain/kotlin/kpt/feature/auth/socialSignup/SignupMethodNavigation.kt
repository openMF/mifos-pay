/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.socialSignup

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SIGNUP_METHOD_ROUTE = "signup_method_route"

fun NavGraphBuilder.signupMethodScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: (savingsProductId: Int) -> Unit,
) {
    composable(
        route = SIGNUP_METHOD_ROUTE,
    ) {
        SignupMethodScreen(
            onDismissSignUp = onNavigateBack,
            navigateToSignupScreen = onNavigateToSignUp,
        )
    }
}

fun NavController.navigateToSignupMethod() {
    this.navigate(SIGNUP_METHOD_ROUTE)
}
