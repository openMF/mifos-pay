/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.loanProductDetails

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions

/**
 * Type-safe navigation route for the loan product details screen.
 *
 * @param clientId The id of the client applying for a loan.
 * @param productId The id of the loan product the client selected.
 * @param providerId The id of the loan provider chosen on the Select Loan Provider screen.
 */
@Serializable
data class LoanProductDetailsRoute(
    val clientId: Long,
    val productId: Long,
    val providerId: String,
)

/**
 * Navigates to the loan product details screen for the given [clientId]/[productId]/[providerId].
 */
fun NavController.navigateToLoanProductDetails(
    clientId: Long,
    productId: Long,
    providerId: String,
    navOptions: NavOptions? = null,
) {
    navigate(LoanProductDetailsRoute(clientId, productId, providerId), navOptions)
}

/**
 * Registers the loan product details screen in the navigation graph.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToLoanApply Callback to proceed to the application form for this product.
 */
fun NavGraphBuilder.loanProductDetailsScreen(
    navigateBack: () -> Unit,
    navigateToLoanApply: (clientId: Long, productId: Long, providerId: String) -> Unit,
) {
    composableWithSlideTransitions<LoanProductDetailsRoute> {
        LoanProductDetailsScreen(
            navigateBack = navigateBack,
            navigateToLoanApply = navigateToLoanApply,
        )
    }
}
