/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.success

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.feature.transfer.intrabank.confirm.TransferResult
/**
 * Navigation route of the transfer success screen.
 * @property transactionId Id of the completed transaction.
 * @property amount Amount transferred.
 * @property fromAccountNo Source account number.
 * @property fromAccountName Source account holder name.
 * @property toAccountNo Destination account number.
 * @property toAccountName Destination account holder name.
 * @property transferDate Transfer date formatted for display.
 * @property description Description of the transfer.
 * @property returnDestination Destination to return to after the screen.
 */
@Serializable
data class TransferSuccessRoute(
    val transactionId: String = "",
    val amount: Double = 0.0,
    val fromAccountNo: String = "",
    val fromAccountName: String = "",
    val toAccountNo: String = "",
    val toAccountName: String = "",
    val transferDate: String = "",
    val description: String = "",
    val returnDestination: String = "home",
)

/** Registers the transfer success destination. */
fun NavGraphBuilder.transferSuccessScreen(
    navigateBack: (String) -> Unit,
) {
    composable<TransferSuccessRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<TransferSuccessRoute>()
        val transferResult = TransferResult(
            transactionId = route.transactionId,
            amount = route.amount,
            fromAccountNo = route.fromAccountNo,
            fromAccountName = route.fromAccountName,
            toAccountNo = route.toAccountNo,
            toAccountName = route.toAccountName,
            transferDate = route.transferDate,
            description = route.description,
        )
        TransferSuccessScreen(
            transferResult = transferResult,
            navigateBack = { navigateBack(route.returnDestination) },
        )
    }
}

/** Navigates to the transfer success screen for [transferResult]. */
fun NavController.navigateTransferSuccess(
    transferResult: TransferResult,
    returnDestination: String = "home",
    navOptions: NavOptions? = null,
) {
    navigate(
        TransferSuccessRoute(
            transactionId = transferResult.transactionId,
            amount = transferResult.amount,
            fromAccountNo = transferResult.fromAccountNo,
            fromAccountName = transferResult.fromAccountName,
            toAccountNo = transferResult.toAccountNo,
            toAccountName = transferResult.toAccountName,
            transferDate = transferResult.transferDate,
            description = transferResult.description,
            returnDestination = returnDestination,
        ),
        navOptions,
    )
}
