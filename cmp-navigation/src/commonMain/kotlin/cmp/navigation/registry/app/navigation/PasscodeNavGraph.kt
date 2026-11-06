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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navOptions

// internal fun NavGraphBuilder.passcodeNavGraph(navController: NavController) {
//    navigation(
//        route = MifosNavGraph.PASSCODE_GRAPH,
//        startDestination = PASSCODE_SCREEN,
//    ) {
//        passcodeRoute(
//            onForgotButton = {
//                navController.popBackStack()
//                navController.navigateToMainGraph()
//            },
//            onSkipButton = {
//                navController.popBackStack()
//                navController.navigateToMainGraph()
//            },
//            onPasscodeConfirm = {
//                navController.popBackStack()
//                navController.navigateToMainGraph()
//            },
//            onPasscodeRejected = {
//                navController.popBackStack()
//                navController.navigateToMainGraph()
//            },
//        )
//    }
// }

fun NavController.navigateToMainGraph() {
    val options = navOptions {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = false
    }

    navigate(MifosNavGraph.MAIN_GRAPH, options)
}
