/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.ui

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import cmp.navigation.registry.app.utils.TopLevelDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kpt.core.data.util.NetworkMonitor
import kpt.core.data.util.TimeZoneMonitor
import kpt.feature.finance.navigation.FINANCE_ROUTE
import kpt.feature.finance.navigation.navigateToFinance
import kpt.feature.history.navigation.HISTORY_ROUTE
import kpt.feature.history.navigation.navigateToHistory
import kpt.feature.home.navigation.HOME_ROUTE
import kpt.feature.home.navigation.navigateToHome
import kpt.feature.payments.PAYMENTS_ROUTE
import kpt.feature.payments.navigateToPayments

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun rememberMifosAppState(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    windowSizeClass: WindowSizeClass = calculateWindowSizeClass(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): MifosAppState {
    return remember(
        navController,
        coroutineScope,
        windowSizeClass,
        networkMonitor,
        timeZoneMonitor,
    ) {
        MifosAppState(
            navController = navController,
            coroutineScope = coroutineScope,
            windowSizeClass = windowSizeClass,
            networkMonitor = networkMonitor,
            timeZoneMonitor = timeZoneMonitor,
        )
    }
}

@Stable
internal class MifosAppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            HOME_ROUTE -> TopLevelDestination.HOME
            PAYMENTS_ROUTE -> TopLevelDestination.PAYMENTS
            FINANCE_ROUTE -> TopLevelDestination.FINANCE
            HISTORY_ROUTE -> TopLevelDestination.HISTORY
            else -> null
        }

    /** True when the window is compact, so the bottom navigation bar is shown. */
    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    /** True when the navigation rail is shown instead of the bottom bar. */
    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    /** Emits true while the device has no network connection. */
    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /** Top-level destinations shown in the bottom bar or navigation rail. */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /** Current system time zone, kept in sync by the time-zone monitor. */
    val currentTimeZone = timeZoneMonitor.currentTimeZone
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            TimeZone.currentSystemDefault(),
        )

    /** Navigates to a top-level destination, restoring its saved state and avoiding duplicate entries. */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }

            when (topLevelDestination) {
                TopLevelDestination.HOME -> navController.navigateToHome(topLevelNavOptions)
                TopLevelDestination.PAYMENTS -> navController.navigateToPayments(topLevelNavOptions)
                TopLevelDestination.FINANCE -> navController.navigateToFinance(topLevelNavOptions)
                TopLevelDestination.HISTORY -> navController.navigateToHistory(topLevelNavOptions)
            }
        }
    }
}
