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
import kpt.feature.pocket.screens.ManagePocketScreen
const val MANAGE_POCKET_ROUTE = "manage_pocket_route"

fun NavController.navigateToManagePocket() {
    navigate(MANAGE_POCKET_ROUTE)
}

fun NavGraphBuilder.managePocketDestination(
    navigateBack: () -> Unit,
) {
    composable(route = MANAGE_POCKET_ROUTE) {
        ManagePocketScreen(
            navigateBack = navigateBack,
        )
    }
}
