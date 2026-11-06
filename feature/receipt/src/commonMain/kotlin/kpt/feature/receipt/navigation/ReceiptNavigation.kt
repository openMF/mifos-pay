/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.receipt.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.receipt.ReceiptScreenRoute
const val RECEIPT_ROUTE = "receipt_route"
const val RECEIPT_TRANSFER_ID = "transferId"

private const val BASE_ROUTE = "$RECEIPT_ROUTE&$RECEIPT_TRANSFER_ID={$RECEIPT_TRANSFER_ID}"

/**
 * Reads `transferId` (see [navigateToReceipt]) via `SavedStateHandle` inside
 * `ReceiptViewModel` — same nav-arg contract `TransactionDetailNavigation.kt`
 * already uses (D13 verdict — `sub-plans/RECEIPT_DATA_SOURCE.md`).
 */
fun NavGraphBuilder.receiptScreen(
    onBackClick: () -> Unit,
) {
    composableWithSlideTransitions(
        route = BASE_ROUTE,
        arguments = listOf(
            navArgument(RECEIPT_TRANSFER_ID) { type = NavType.LongType },
        ),
    ) {
        ReceiptScreenRoute(onBackClick = onBackClick)
    }
}

fun NavController.navigateToReceipt(transferId: Long) {
    this.navigate("$RECEIPT_ROUTE&$RECEIPT_TRANSFER_ID=$transferId")
}
