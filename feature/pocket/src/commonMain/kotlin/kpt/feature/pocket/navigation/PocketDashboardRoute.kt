/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pocket.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kpt.feature.pocket.screens.PocketDashboardScreen
/** Navigation route of the pocket dashboard screen. */
const val POCKET_DASHBOARD_ROUTE = "pocket_dashboard_route"

/** Navigates to the pocket dashboard screen. */
fun NavController.navigateToPocketDashboard() {
    navigate(POCKET_DASHBOARD_ROUTE)
}

/** Registers the pocket dashboard screen destination. */
fun NavGraphBuilder.pocketDashboardScreen(
    navigateBack: () -> Unit,
    navigateToManagePocket: () -> Unit,
    navigateToLoanAccountDetail: (Long) -> Unit,
    navigateToShareAccountDetail: (Long) -> Unit,
    navigateToSavingsAccountDetail: (Long) -> Unit,
) {
    composable(route = POCKET_DASHBOARD_ROUTE) {
        PocketDashboardScreen(
            navigateBack = navigateBack,
            navigateToManagePocket = navigateToManagePocket,
            navigateToLoanAccountDetail = navigateToLoanAccountDetail,
            navigateToShareAccountDetail = navigateToShareAccountDetail,
            navigateToSavingsAccountDetail = navigateToSavingsAccountDetail,
        )
    }
}
