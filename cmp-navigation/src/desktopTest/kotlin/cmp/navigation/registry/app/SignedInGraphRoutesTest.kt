/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cmp.navigation.registry.ForkFeatureRegistry
import cmp.navigation.registry.ForkFeatureRegistry.shellDestinations
import kpt.feature.autopay.AutoPayNavigation
import kpt.feature.editpassword.navigation.EDIT_PASSWORD_ROUTE
import kpt.feature.faq.navigation.FAQ_ROUTE
import kpt.feature.finance.navigation.FINANCE_ROUTE
import kpt.feature.history.navigation.HISTORY_ROUTE
import kpt.feature.home.navigation.HOME_ROUTE
import kpt.feature.invoices.navigation.INVOICE_ROUTE
import kpt.feature.merchants.navigation.MERCHANT_TRANSFER_ROUTE
import kpt.feature.mpay.qr.navigation.MPAY_QR_ROUTE
import kpt.feature.mpay.qr.scan.navigation.SCAN_QR_ROUTE
import kpt.feature.notification.NOTIFICATION_ROUTE
import kpt.feature.payments.PAYMENTS_ROUTE
import kpt.feature.receipt.navigation.RECEIPT_ROUTE
import kpt.feature.settings.navigation.SETTINGS_ROUTE
import kpt.feature.upi.setup.navigation.SETUP_UPI_PIN_ROUTE
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The signed-in graph contains every feature's routes.
 *
 * Destinations reach the graph two ways: a self-contained feature's `@FeatureDestination` graph, collected
 * into the generated aggregate, and the cross-feature wiring in [ForkFeatureRegistry]. Both fail silently —
 * an annotated graph outside a `navigation/` package compiles and is never collected, a wiring call left
 * out compiles too — and either way tapping into that screen crashes with "destination not found".
 */
@OptIn(ExperimentalTestApi::class)
class SignedInGraphRoutesTest {

    @Test
    fun everyFeatureRouteIsRegistered() = runComposeUiTest {
        lateinit var navController: NavHostController
        setContent {
            navController = rememberNavController()
            // A blank start screen: the graph is built in full, but only this destination composes.
            NavHost(navController, startDestination = PROBE, route = "signed_in") {
                composable(PROBE) {}
                shellDestinations(navController, onLogout = {}, handleAppLocale = {})
                ForkFeatureRegistry.featureDestinations(this, navController)
            }
        }
        waitForIdle()

        val routes = navController.graph.allRoutes()
        val expected = listOf(
            // @FeatureDestination graphs
            FAQ_ROUTE, NOTIFICATION_ROUTE, INVOICE_ROUTE, RECEIPT_ROUTE, MERCHANT_TRANSFER_ROUTE,
            SETUP_UPI_PIN_ROUTE, MPAY_QR_ROUTE, AutoPayNavigation.AUTO_PAY_ROUTE,
            // Saved cards and standing instructions keep their route names private to the feature.
            "card_detail", "card_add_edit_item", "sid_details_route", "si_add_edit_item",
            // Cross-feature wiring and the session destinations
            HOME_ROUTE, PAYMENTS_ROUTE, FINANCE_ROUTE, HISTORY_ROUTE, SCAN_QR_ROUTE,
            SETTINGS_ROUTE, EDIT_PASSWORD_ROUTE, "InternalPasscodeRoute",
        )
        val missing = expected.filter { want -> routes.none { it.contains(want) } }
        assertTrue(missing.isEmpty(), "not registered in the signed-in graph: $missing")
    }

    private fun NavDestination.allRoutes(): List<String> {
        val children = (this as? NavGraph)?.nodes ?: return listOfNotNull(route)
        return listOfNotNull(route) + (0 until children.size()).flatMap { children.valueAt(it).allRoutes() }
    }

    private companion object {
        const val PROBE = "probe"
    }
}
