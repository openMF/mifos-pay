/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kpt.feature.home.fork.HomeScreen
const val HOME_ROUTE = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HOME_ROUTE, navOptions)

fun NavGraphBuilder.homeScreen(
    onNavigateBack: () -> Unit,
    onRequest: (String) -> Unit,
    onPay: () -> Unit,
    onAutoPay: () -> Unit,
    navigateToPocketDashboard: () -> Unit,
    navigateToTransactionDetail: (Long, Long) -> Unit,
    navigateToAccountDetail: (Long) -> Unit,
    navigateToHistory: () -> Unit,
) {
    composable(route = HOME_ROUTE) {
        HomeScreen(
            onRequest = onRequest,
            onPay = onPay,
            onAutoPay = onAutoPay,
            navigateToPocketDashboard = navigateToPocketDashboard,
            onNavigateBack = onNavigateBack,
            navigateToTransactionDetail = navigateToTransactionDetail,
            navigateToAccountDetail = navigateToAccountDetail,
            navigateToHistory = navigateToHistory,
        )
    }
}
