/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MaxLineLength")

package kpt.feature.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.history.transactions.SpecificTransactionsScreen
/** Base route of the specific-transactions screen. */
const val SPECIFIC_TRANSACTIONS_BASE_ROUTE = "specific_transactions_route"

/** Route argument name for the account id. */
const val ACCOUNT_ARG = "accountId"

/** Route argument name for the transaction id. */
const val TRANSACTIONS_ARG = "transactionId"

/** Full route of the specific-transactions screen, including account and transaction arguments. */
const val SPECIFIC_TRANSACTIONS_ROUTE = SPECIFIC_TRANSACTIONS_BASE_ROUTE +
    "&$ACCOUNT_ARG={$ACCOUNT_ARG}" +
    "&$TRANSACTIONS_ARG={$TRANSACTIONS_ARG}"

/** Registers the specific-transactions screen in the nav graph. */
fun NavGraphBuilder.specificTransactionsScreen(
    navigateBack: () -> Unit,
    viewTransactionDetail: (Long) -> Unit,
) {
    composableWithSlideTransitions(
        route = SPECIFIC_TRANSACTIONS_ROUTE,
        arguments = listOf(
            navArgument(ACCOUNT_ARG) { type = NavType.LongType },
            navArgument(TRANSACTIONS_ARG) { type = NavType.LongType },
        ),
    ) {
        SpecificTransactionsScreen(
            navigateBack = navigateBack,
            viewTransaction = viewTransactionDetail,
        )
    }
}

/** Navigates to the specific-transactions screen for the given account and transaction. */
fun NavController.navigateToSpecificTransaction(accountId: Long, transactionId: Long) {
    navigate(
        SPECIFIC_TRANSACTIONS_BASE_ROUTE +
            "&$ACCOUNT_ARG=$accountId&$TRANSACTIONS_ARG=$transactionId",
    )
}
