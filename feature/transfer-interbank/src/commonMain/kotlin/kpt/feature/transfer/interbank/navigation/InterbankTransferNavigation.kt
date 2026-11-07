/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.interbank.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.feature.transfer.interbank.InterbankTransferFlowScreen
/**
 * Navigation route of the interbank transfer flow, optionally prefilled from a scanned QR code.
 * @property returnDestination Destination to return to after the flow.
 * @property phoneNumber Prefilled recipient phone number, or null.
 * @property recipientName Prefilled recipient name, or null.
 * @property amount Prefilled amount, or null.
 */
@Serializable
data class InterbankTransferRoute(
    val returnDestination: String = "home",
    val phoneNumber: String? = null,
    val recipientName: String? = null,
    val amount: String? = null,
)

/** Navigates to the interbank transfer flow with optional prefilled recipient details. */
fun NavController.navigateToInterbankTransfer(
    returnDestination: String = "home",
    phoneNumber: String? = null,
    recipientName: String? = null,
    amount: String? = null,
    navOptions: NavOptions? = null,
) {
    this.navigate(
        InterbankTransferRoute(
            returnDestination = returnDestination,
            phoneNumber = phoneNumber,
            recipientName = recipientName,
            amount = amount,
        ),
        navOptions,
    )
}

/** Registers the interbank transfer destination. */
fun NavGraphBuilder.interbankTransferScreen(
    onBackClick: () -> Unit,
    onTransferSuccess: () -> Unit,
) {
    composable<InterbankTransferRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<InterbankTransferRoute>()
        InterbankTransferFlowScreen(
            onBackClick = onBackClick,
            onTransferSuccess = onTransferSuccess,
            initialPhoneNumber = route.phoneNumber,
            initialAmount = route.amount,
        )
    }
}
