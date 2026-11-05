/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.providerWebView

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions

/**
 * Type-safe navigation route for the loan provider website screen, shown right after a provider
 * is picked on `org.mifos.lib.loan.ui.selectLoanProvider.SelectLoanProviderRoute`.
 *
 * @param clientId The id of the client applying for a loan.
 * @param providerId The id of the loan provider chosen on the Select Loan Provider screen.
 * @param url The provider's website to render (see [urlForLoanProvider]).
 */
@Serializable
data class LoanProviderWebViewRoute(val clientId: Long, val providerId: String, val url: String)

/**
 * Navigates to the loan provider website screen for the given [clientId]/[providerId]/[url].
 */
fun NavController.navigateToLoanProviderWebView(
    clientId: Long,
    providerId: String,
    url: String,
    navOptions: NavOptions? = null,
) {
    navigate(LoanProviderWebViewRoute(clientId, providerId, url), navOptions)
}

/**
 * Registers the loan provider website screen in the navigation graph.
 *
 * @param navigateBack Callback to return to the previous screen (Select Loan Provider).
 */
fun NavGraphBuilder.loanProviderWebViewScreen(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions<LoanProviderWebViewRoute> {
        LoanProviderWebViewScreen(
            navigateBack = navigateBack,
        )
    }
}
