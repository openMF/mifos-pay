/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.selectLoanProvider

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions

/**
 * Type-safe navigation route for the loan provider selection screen — the new entry point of the
 * loan application wizard, shown before [org.mifos.lib.loan.ui.selectLoanType.SelectLoanTypeRoute].
 *
 * @param clientId The id of the client applying for a loan.
 */
@Serializable
data class SelectLoanProviderRoute(val clientId: Long)

/**
 * Navigates to the loan provider selection screen for the given [clientId].
 */
fun NavController.navigateToSelectLoanProvider(clientId: Long, navOptions: NavOptions? = null) {
    navigate(SelectLoanProviderRoute(clientId), navOptions)
}

/**
 * Registers the loan provider selection screen in the navigation graph.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToSelectLoanType Callback to proceed into the existing loan-type selection step
 * once a provider has been chosen.
 */
fun NavGraphBuilder.selectLoanProviderScreen(
    navigateBack: () -> Unit,
    navigateToLoanProviderWebView: (
        clientId: Long,
        providerId: String,
        url: String,
    ) -> Unit,
) {
    composableWithSlideTransitions<SelectLoanProviderRoute> {
        SelectLoanProviderScreen(
            navigateBack = navigateBack,
            navigateToLoanProviderWebView = navigateToLoanProviderWebView,
        )
    }
}
