/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.history.detail.TransactionDetailScreen
const val TRANSACTION_DETAIL_ROUTE = "transaction_detail"
const val TRANSFER_ID = "transferId"

private const val BASE_ROUTE = "$TRANSACTION_DETAIL_ROUTE&$TRANSFER_ID={$TRANSFER_ID}"

fun NavGraphBuilder.transactionDetailNavigation(
    navigateBack: () -> Unit,
    navigateToReceipt: (transferId: Long) -> Unit = {},
) {
    composableWithSlideTransitions(
        route = BASE_ROUTE,
        arguments = listOf(
            navArgument(TRANSFER_ID) { type = NavType.LongType },
        ),
    ) {
        TransactionDetailScreen(
            onNavigateBack = navigateBack,
            onShareTransaction = navigateToReceipt,
        )
    }
}

fun NavHostController.navigateToTransactionDetail(transferId: Long) {
    navigate("$TRANSACTION_DETAIL_ROUTE&$TRANSFER_ID=$transferId")
}
