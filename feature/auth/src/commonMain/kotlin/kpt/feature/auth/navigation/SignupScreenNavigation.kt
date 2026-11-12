/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MaxLineLength")

package kpt.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithPushTransitions
import kpt.feature.auth.signup.SignupScreen
/** Navigation route of the signup screen. */
const val SIGNUP_ROUTE = "signup_route"

/** Registers the signup destination, with optional savings product, mobile number and business name arguments. */
fun NavGraphBuilder.signupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: (String) -> Unit,
) {
    composableWithPushTransitions(
        route = "$SIGNUP_ROUTE?savingsProductId={savingsProductId}" +
            "&mobileNumber={mobileNumber}&businessName={businessName}",
        arguments = listOf(
            navArgument("savingsProductId") {
                type = NavType.IntType
                defaultValue = 0
            },
            navArgument("mobileNumber") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("businessName") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        SignupScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}

/** Navigates to the signup screen, optionally prefilling the savings product, mobile number and business name. */
fun NavController.navigateToSignup(
    savingsProductId: Int = 0,
    mobileNumber: String = "",
    businessName: String = "",
) {
    this.navigate(
        "$SIGNUP_ROUTE?savingsProductId=$savingsProductId" +
            "&mobileNumber=$mobileNumber&businessName=$businessName",
    )
}
