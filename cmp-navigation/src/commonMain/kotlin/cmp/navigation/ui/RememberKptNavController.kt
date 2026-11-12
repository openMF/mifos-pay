/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper

/**
 * Remembers a `NavController` with the given navigators installed.
 *
 * @param name a label carried into analytics, so a destination change can be attributed to the right nav host.
 * @param navigators extra navigators beyond the defaults.
 */
@Composable
fun rememberKptNavController(name: String, vararg navigators: Navigator<out NavDestination>): NavHostController =
    rememberNavController(navigators = navigators).apply {
        this.addOnDestinationChangedListener { _, destination, _ ->
            val graph = destination.parent?.route?.let { " in $it" }.orEmpty()
            Logger.d("$name destination changed: ${destination.route}$graph")
        }
    }

/**
 * Logs a destination change.
 *
 * @param route the route navigated to.
 */
fun AnalyticsHelper.logDestinationChanged(route: String) {
    logEvent(
        type = "destination_changed",
        params = mapOf("route" to route),
    )
}
