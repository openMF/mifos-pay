/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.savingsaccount.details

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
private const val ROUTE = "saving_account_detail"
private const val ACCOUNT_ID = "accountId"

private const val BASE_ROUTE = "$ROUTE?$ACCOUNT_ID={$ACCOUNT_ID}"

/** Registers the savings account detail destination. */
fun NavGraphBuilder.savingAccountDetailRoute(
    navigateBack: () -> Unit,
    onViewTransaction: (Long, Long) -> Unit,
) {
    composableWithSlideTransitions(
        route = BASE_ROUTE,
        arguments = listOf(
            navArgument(ACCOUNT_ID) { type = NavType.LongType },
        ),
    ) {
        SavingAccountDetailScreen(
            navigateBack = navigateBack,
            onViewTransaction = onViewTransaction,
        )
    }
}

/** Navigates to the details of the savings account with [accountId]. */
fun NavController.navigateToSavingAccountDetails(accountId: Long) {
    navigate("$ROUTE?$ACCOUNT_ID=$accountId")
}
