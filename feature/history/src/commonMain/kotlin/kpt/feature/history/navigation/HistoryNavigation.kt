/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kpt.feature.history.HistoryScreen
/** Navigation route of the history screen. */
const val HISTORY_ROUTE = "history_route"

/** Registers the history screen in the nav graph; [viewTransactionDetail] opens a transfer (two ids). */
fun NavGraphBuilder.historyNavigation(
    viewTransactionDetail: (Long, Long) -> Unit,
) {
    composable(HISTORY_ROUTE) {
        HistoryScreen(
            viewTransferDetail = viewTransactionDetail,
        )
    }
}

/** Navigates to the history screen, optionally with [navOptions]. */
fun NavController.navigateToHistory(navOptions: NavOptions? = null) {
    navigate(HISTORY_ROUTE, navOptions)
}
