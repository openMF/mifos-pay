/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.finance.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kpt.core.ui.utility.TabContent
import kpt.feature.finance.FinanceRoute
const val FINANCE_ROUTE = "finance_route"

fun NavController.navigateToFinance(navOptions: NavOptions) = navigate(FINANCE_ROUTE, navOptions)

fun NavGraphBuilder.financeScreen(
    tabContents: List<TabContent>,
) {
    composable(route = FINANCE_ROUTE) {
        FinanceRoute(tabContents = tabContents)
    }
}
