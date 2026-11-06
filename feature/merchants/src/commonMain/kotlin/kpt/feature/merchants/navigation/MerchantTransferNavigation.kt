/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.merchants.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kpt.feature.merchants.ui.MerchantTransferScreenRoute
const val MERCHANT_TRANSFER_ROUTE = "merchant_transfer_route"

fun NavGraphBuilder.merchantTransferScreen(
    proceedWithMakeTransferFlow: (String, String?) -> Unit,
    onBackPressed: () -> Unit,
) {
    composable(
        route = "$MERCHANT_TRANSFER_ROUTE/{merchantName}/{merchantVPA}/{merchantAccountNumber}",
        arguments = listOf(
            navArgument("merchantName") { type = NavType.StringType },
            navArgument("merchantVPA") { type = NavType.StringType },
            navArgument("merchantAccountNumber") { type = NavType.StringType },
        ),
    ) {
        MerchantTransferScreenRoute(
            onBackPressed = onBackPressed,
            proceedWithMakeTransferFlow = proceedWithMakeTransferFlow,
        )
    }
}

fun NavController.navigateToMerchantTransferScreen(
    merchantName: String,
    merchantVPA: String,
    merchantAccountNumber: String,
) {
    navigate("$MERCHANT_TRANSFER_ROUTE/$merchantName/$merchantVPA/$merchantAccountNumber")
}
