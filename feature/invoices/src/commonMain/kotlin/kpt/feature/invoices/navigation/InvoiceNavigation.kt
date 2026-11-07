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
/** Base navigation route of the invoice detail screen. */
const val INVOICE_ROUTE = "invoice_route"

/** Name of the navigation argument carrying the invoice id. */
const val INVOICE_DATA_ARG = "invoiceId"

/** Navigates to the detail screen of the invoice with the given [invoiceId]. */
fun NavController.navigateToInvoiceDetail(invoiceId: Long) {
    this.navigate("$INVOICE_ROUTE/$invoiceId")
}

/** Registers the invoice detail destination, which takes the invoice id as a long argument. */
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
