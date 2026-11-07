/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.selectScreen

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/**
 * Navigation route of the select-account screen.
 * @property returnDestination Destination to return to after the transfer.
 */
@Serializable
data class SelectAccountRoute(
    val returnDestination: String = "home",
)

/** Navigates to the select-account screen. */
fun NavController.navigateToSelectAccountScreen(
    returnDestination: String = "home",
    navOptions: NavOptions? = null,
) {
    this.navigate(SelectAccountRoute(returnDestination = returnDestination), navOptions)
}

/** Registers the select-account destination and its navigation callbacks. */
fun NavGraphBuilder.selectAccountScreenDestination(
    navigateBack: () -> Unit,
    navigateToTransferConfirm: (
        toOfficeId: Int?,
        toClientId: Long?,
        toAccountTypeId: Int?,
        toAccountId: Int,
        amount: Int,
        accountName: String,
        accountNo: String,
        returnDestination: String,
    ) -> Unit,
) {
    composable<SelectAccountRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<SelectAccountRoute>()
        SelectPayeeScreen(
            navigateToTransferConfirm = { toOfficeId, toClientId, toAccountTypeId, toAccountId, amount, accountName, accountNo ->
                navigateToTransferConfirm(toOfficeId, toClientId, toAccountTypeId, toAccountId, amount, accountName, accountNo, route.returnDestination)
            },
            navigateBack = navigateBack,
        )
    }
}
