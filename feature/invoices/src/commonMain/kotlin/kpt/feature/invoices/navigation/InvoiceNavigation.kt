/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.invoices.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.ui.composableWithPushTransitions
import kpt.feature.invoices.details.InvoiceDetailScreen
const val INVOICE_ROUTE = "invoice_route"
const val INVOICE_DATA_ARG = "invoiceId"

fun NavController.navigateToInvoiceDetail(invoiceId: Long) {
    this.navigate("$INVOICE_ROUTE/$invoiceId")
}

fun NavGraphBuilder.invoiceDetailScreen(
    onNavigateBack: () -> Unit,
) {
    composableWithPushTransitions(
        route = "$INVOICE_ROUTE/{$INVOICE_DATA_ARG}",
        arguments = listOf(
            navArgument(INVOICE_DATA_ARG) { type = NavType.LongType },
        ),
    ) {
        InvoiceDetailScreen(
            navigateBack = onNavigateBack,
        )
    }
}
