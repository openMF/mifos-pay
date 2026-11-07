/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.upi.setup.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kpt.core.common.Constants
import kpt.core.model.bank.BankAccountDetails
import kpt.feature.upi.setup.screens.SetupUpiPinScreenRoute
/**
 * Base route name of the set-up UPI PIN screen.
 */
const val SETUP_UPI_PIN_ROUTE = "setup_upi_pin_route"

/**
 * Was a nav-registered-but-blank destination — the composable body called no screen
 * (`SetupUpiPinScreenRoute(...)` was commented out) and [navigateToSetupUpiPin] had zero
 * callers anywhere in the app. This fix makes the destination actually render; the
 * reachability gap (no in-app entry point navigates here yet) is documented in
 * `sub-plans/UPI_OTP_STUB_VERDICT.md` rather than papered over with an invented caller.
 *
 * `bankAccountDetails` is reconstructed from the nav args this route already carried
 * (index/type) plus the account number, which IS threaded through the route args below —
 * unlike the previous version, which built a hardcoded placeholder `BankAccountDetails`
 * regardless of what was passed to [navigateToSetupUpiPin].
 */
fun NavGraphBuilder.setupUpiPinScreen(
    navigateBack: () -> Unit,
) {
    composable(
        route = "$SETUP_UPI_PIN_ROUTE/{${Constants.INDEX}}/{${Constants.TYPE}}/{${Constants.ACCOUNT_NO}}",
        arguments = listOf(
            navArgument(Constants.INDEX) { type = NavType.IntType },
            navArgument(Constants.TYPE) { type = NavType.StringType },
            navArgument(Constants.ACCOUNT_NO) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        // NavBackStackEntry.arguments is a KMP `androidx.core.bundle.Bundle` without typed
        // getters in commonMain — every other nav-arg reader in this codebase goes through
        // `SavedStateHandle` instead (see TransactionDetailViewModel, ReceiptViewModel), which
        // IS multiplatform-safe and auto-populated from these same route args.
        val index = backStackEntry.savedStateHandle.get<Int>(Constants.INDEX) ?: 0
        val type = backStackEntry.savedStateHandle.get<String>(Constants.TYPE) ?: ""
        val accountNo = backStackEntry.savedStateHandle.get<String>(Constants.ACCOUNT_NO) ?: ""

        SetupUpiPinScreenRoute(
            type = type,
            index = index,
            bankAccountDetails = BankAccountDetails(
                accountNo = accountNo,
                bankName = null,
                accountHolderName = null,
                branch = null,
                ifsc = null,
                type = null,
                isUpiEnabled = false,
                upiPin = null,
            ),
            onBackPress = navigateBack,
        )
    }
}

/**
 * Navigates to the UPI PIN screen for [type] (setup, change or forgot) of the given bank account.
 */
fun NavController.navigateToSetupUpiPin(
    bankAccountDetails: BankAccountDetails,
    index: Int,
    type: String,
) {
    this.navigate("$SETUP_UPI_PIN_ROUTE/$index/$type/${bankAccountDetails.accountNo}") {
        this.launchSingleTop = true
        this.restoreState = true
    }
}
