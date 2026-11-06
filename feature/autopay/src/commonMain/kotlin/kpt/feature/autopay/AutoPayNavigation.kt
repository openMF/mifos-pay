/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions

object AutoPayNavigation {
    const val AUTO_PAY_ROUTE = "autopay_route"
    const val AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE = "autopay_schedule_management_route"
    const val AUTO_PAY_PREFERENCES_ROUTE = "autopay_preferences_route"
    const val AUTO_PAY_HISTORY_ROUTE = "autopay_history_route"
    const val AUTO_PAY_SCHEDULE_DETAILS_ROUTE = "autopay_schedule_details_route"
    const val AUTO_PAY_ADD_BILLER_ROUTE = "autopay_add_biller_route"
    const val AUTO_PAY_BILLER_LIST_ROUTE = "autopay_biller_list_route"
    const val AUTO_PAY_EDIT_BILLER_ROUTE = "autopay_edit_biller_route"
    const val AUTO_PAY_ADD_BILL_ROUTE = "autopay_add_bill_route"
    const val AUTO_PAY_BILL_LIST_ROUTE = "autopay_bill_list_route"
    const val AUTO_PAY_EDIT_BILL_ROUTE = "autopay_edit_bill_route"
    const val SCHEDULE_ID_ARG = "scheduleId"
    const val BILLER_ID_ARG = "billerId"
    const val BILL_ID_ARG = "billId"
    const val SOURCE_ARG = "source"
    const val AUTO_PAY_ID_ARG = "autoPayId"
}

/**
 * Custom composable function that uses fade transitions to prevent the state issue
 * where both screens are visible momentarily during navigation.
 */
fun NavGraphBuilder.composableWithFadeTransitions(
    route: String,
    arguments: List<androidx.navigation.NamedNavArgument> = emptyList(),
    deepLinks: List<androidx.navigation.NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        content = content,
    )
}

fun NavController.navigateToAutoPay(navOptions: NavOptions? = null) {
    navigate(AutoPayNavigation.AUTO_PAY_ROUTE, navOptions)
}

fun NavController.navigateToScheduleManagement(navOptions: NavOptions? = null) {
    navigate(AutoPayNavigation.AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE, navOptions)
}

fun NavController.navigateToAutoPayPreferences(navOptions: NavOptions? = null) {
    navigate(AutoPayNavigation.AUTO_PAY_PREFERENCES_ROUTE, navOptions)
}

fun NavController.navigateToAutoPayHistory(autoPayId: Long = 0L, navOptions: NavOptions? = null) {
    val route = "${AutoPayNavigation.AUTO_PAY_HISTORY_ROUTE}?${AutoPayNavigation.AUTO_PAY_ID_ARG}=$autoPayId"
    navigate(route, navOptions)
}

fun NavController.navigateToAutoPayScheduleDetails(scheduleId: String, navOptions: NavOptions? = null) {
    val route = "${AutoPayNavigation.AUTO_PAY_SCHEDULE_DETAILS_ROUTE}?${AutoPayNavigation.SCHEDULE_ID_ARG}=$scheduleId"
    navigate(route, navOptions)
}

fun NavController.navigateToAddBiller(source: String = "direct", navOptions: NavOptions? = null) {
    val route = "${AutoPayNavigation.AUTO_PAY_ADD_BILLER_ROUTE}?${AutoPayNavigation.SOURCE_ARG}=$source"
    navigate(route, navOptions)
}

fun NavController.navigateToBillerList(navOptions: NavOptions? = null) {
    navigate(AutoPayNavigation.AUTO_PAY_BILLER_LIST_ROUTE, navOptions)
}

fun NavController.navigateToEditBiller(billerId: String, navOptions: NavOptions? = null) {
    val route = "${AutoPayNavigation.AUTO_PAY_EDIT_BILLER_ROUTE}?${AutoPayNavigation.BILLER_ID_ARG}=$billerId"
    navigate(route, navOptions)
}

fun NavController.navigateToAddBill(navOptions: NavOptions? = null) {
    navigate(AutoPayNavigation.AUTO_PAY_ADD_BILL_ROUTE, navOptions)
}

fun NavController.navigateToBillList(navOptions: NavOptions? = null) {
    navigate(AutoPayNavigation.AUTO_PAY_BILL_LIST_ROUTE, navOptions)
}

fun NavController.navigateToEditBill(billId: String, navOptions: NavOptions? = null) {
    val route = "${AutoPayNavigation.AUTO_PAY_EDIT_BILL_ROUTE}?${AutoPayNavigation.BILL_ID_ARG}=$billId"
    navigate(route, navOptions)
}

fun NavGraphBuilder.autoPayGraph(
    navController: NavController,
    onNavigateBack: () -> Unit = { navController.navigateUp() },
) {
    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_ROUTE) {
        AutoPayScreen(
            onNavigateToScheduleManagement = {
                navController.navigateToScheduleManagement()
            },
            onNavigateToPreferences = {
                navController.navigateToAutoPayPreferences()
            },
            onNavigateToHistory = {
                navController.navigateToAutoPayHistory(autoPayId = 0L)
            },
            onNavigateToScheduleDetails = { scheduleId ->
                navController.navigateToAutoPayScheduleDetails(scheduleId)
            },
            onNavigateToAddBiller = {
                navController.navigateToAddBiller()
            },
            onNavigateToBillerList = {
                navController.navigateToBillerList()
            },
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
            onNavigateToBillList = {
                navController.navigateToBillList()
            },
            onNavigateBack = onNavigateBack,
            showTopBar = true,
        )
    }

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE) {
        AutoPayScheduleManagementScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
            onNavigateToEditBill = { billId ->
                navController.navigateToEditBill(billId)
            },
            onNavigateToBillList = {
                navController.navigateToBillList()
            },
        )
    }

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_PREFERENCES_ROUTE) {
        AutoPayPreferencesScreen(
            onNavigateBack = onNavigateBack,
        )
    }

    composableWithFadeTransitions(
        route = "${AutoPayNavigation.AUTO_PAY_HISTORY_ROUTE}?${AutoPayNavigation.AUTO_PAY_ID_ARG}={${AutoPayNavigation.AUTO_PAY_ID_ARG}}",
        arguments = listOf(
            navArgument(AutoPayNavigation.AUTO_PAY_ID_ARG) {
                type = NavType.LongType
                nullable = false
                defaultValue = 0L
            },
        ),
    ) { backStackEntry ->
        AutoPayHistoryScreen(
            onNavigateBack = onNavigateBack,
        )
    }

    composableWithFadeTransitions(
        route = "${AutoPayNavigation.AUTO_PAY_SCHEDULE_DETAILS_ROUTE}?${AutoPayNavigation.SCHEDULE_ID_ARG}={${AutoPayNavigation.SCHEDULE_ID_ARG}}",
        arguments = listOf(
            navArgument(AutoPayNavigation.SCHEDULE_ID_ARG) {
                type = NavType.StringType
                nullable = false
            },
        ),
    ) {
        AutoPayScheduleDetailsScreen(
            onNavigateBack = onNavigateBack,
        )
    }

    composableWithFadeTransitions(
        route = "${AutoPayNavigation.AUTO_PAY_ADD_BILLER_ROUTE}?${AutoPayNavigation.SOURCE_ARG}={${AutoPayNavigation.SOURCE_ARG}}",
        arguments = listOf(
            navArgument(AutoPayNavigation.SOURCE_ARG) {
                type = NavType.StringType
                nullable = false
                defaultValue = "direct"
            },
        ),
    ) {
        AddBillerScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBillerList = {
                navController.navigateToBillerList()
            },
        )
    }

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_BILLER_LIST_ROUTE) {
        BillerListScreen(
            onNavigateBack = {
                navController.popBackStack(AutoPayNavigation.AUTO_PAY_ROUTE, false)
            },
            onNavigateToAddBiller = {
                navController.navigateToAddBiller(source = "direct")
            },
            onNavigateToEditBiller = { billerId ->
                navController.navigateToEditBiller(billerId)
            },
        )
    }

    composableWithFadeTransitions(
        route = "${AutoPayNavigation.AUTO_PAY_EDIT_BILLER_ROUTE}?${AutoPayNavigation.BILLER_ID_ARG}={${AutoPayNavigation.BILLER_ID_ARG}}",
        arguments = listOf(
            navArgument(AutoPayNavigation.BILLER_ID_ARG) {
                type = NavType.StringType
                nullable = false
            },
        ),
    ) {
        EditBillerScreen(
            onNavigateBack = onNavigateBack,
        )
    }

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_ADD_BILL_ROUTE) {
        AddBillScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBillList = {
                navController.navigateToBillList(
                    navOptions = navOptions {
                        popUpTo(AutoPayNavigation.AUTO_PAY_ROUTE) {
                            inclusive = false
                        }
                    },
                )
            },
            onNavigateToAddBiller = {
                navController.navigateToAddBiller(source = "bill_creation")
            },
        )
    }

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_BILL_LIST_ROUTE) {
        BillListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
            onNavigateToEditBill = { billId ->
                navController.navigateToEditBill(billId)
            },
            onNavigateToAutopay = {
                navController.popBackStack(AutoPayNavigation.AUTO_PAY_ROUTE, false)
            },
        )
    }

    composableWithFadeTransitions(
        route = "${AutoPayNavigation.AUTO_PAY_EDIT_BILL_ROUTE}?${AutoPayNavigation.BILL_ID_ARG}={${AutoPayNavigation.BILL_ID_ARG}}",
        arguments = listOf(
            navArgument(AutoPayNavigation.BILL_ID_ARG) {
                type = NavType.StringType
                nullable = false
            },
        ),
    ) {
        EditBillScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBiller = {
                navController.navigateToAddBiller(source = "bill_creation")
            },
        )
    }
}
