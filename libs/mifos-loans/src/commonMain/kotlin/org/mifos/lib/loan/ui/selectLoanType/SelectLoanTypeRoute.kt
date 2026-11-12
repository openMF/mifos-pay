/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.selectLoanType

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions

/**
 * Type-safe navigation route for the loan type selection screen — the entry point of the
 * loan application wizard.
 *
 * @param clientId The id of the client applying for a loan.
 * @param providerId The id of the loan provider chosen on the Select Loan Provider screen.
 */
@Serializable
data class SelectLoanTypeRoute(val clientId: Long, val providerId: String)

/**
 * Navigates to the loan type selection screen for the given [clientId]/[providerId].
 */
fun NavController.navigateToSelectLoanType(
    clientId: Long,
    providerId: String,
    navOptions: NavOptions? = null,
) {
    navigate(SelectLoanTypeRoute(clientId, providerId), navOptions)
}

/**
 * Registers the loan type selection screen in the navigation graph.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToLoanProductDetails Callback to proceed to the details of a selected product.
 */
fun NavGraphBuilder.selectLoanTypeScreen(
    navigateBack: () -> Unit,
    navigateToLoanProductDetails: (clientId: Long, productId: Long, providerId: String) -> Unit,
) {
    composableWithSlideTransitions<SelectLoanTypeRoute> {
        SelectLoanTypeScreen(
            navigateBack = navigateBack,
            navigateToLoanProductDetails = navigateToLoanProductDetails,
        )
    }
}
