/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import org.mifos.lib.loan.ui.confirmDetails.confirmDetailsScreen
import org.mifos.lib.loan.ui.confirmDetails.navigateToConfirmDetails
import org.mifos.lib.loan.ui.loanApply.loanApplyScreen
import org.mifos.lib.loan.ui.loanApply.navigateToLoanApply
import org.mifos.lib.loan.ui.loanProductDetails.loanProductDetailsScreen
import org.mifos.lib.loan.ui.loanProductDetails.navigateToLoanProductDetails
import org.mifos.lib.loan.ui.providerWebView.loanProviderWebViewScreen
import org.mifos.lib.loan.ui.providerWebView.navigateToLoanProviderWebView
import org.mifos.lib.loan.ui.providerWebView.urlForLoanProvider
import org.mifos.lib.loan.ui.selectLoanProvider.navigateToSelectLoanProvider
import org.mifos.lib.loan.ui.selectLoanProvider.selectLoanProviderScreen
import org.mifos.lib.loan.ui.selectLoanType.selectLoanTypeScreen
import org.mifos.lib.loan.ui.uploadDocs.navigateToUploadDocs
import org.mifos.lib.loan.ui.uploadDocs.uploadDocsScreen

/**
 * Entry point into the loan-application wizard — this app's single flat `NavHost` convention
 * (see e.g. `feature/transfer-intrabank` and `feature/send-money`, which have no nested
 * `navigation<T>{ ... }` wrapper) has no notion of a nested sub-graph, so this simply navigates
 * to the first step, [org.mifos.lib.loan.ui.selectLoanProvider.SelectLoanProviderRoute] — the new
 * "pick a loan provider (dummy for now)" step the wizard starts with, ahead of
 * [org.mifos.lib.loan.ui.selectLoanType.SelectLoanTypeRoute].
 *
 * @param clientId The id of the client applying for a loan.
 */
fun NavController.navigateToLoanApplicationGraph(clientId: Long, navOptions: NavOptions? = null) {
    navigateToSelectLoanProvider(clientId, navOptions)
}

/**
 * Registers the loan-application wizard's destinations — Select Loan Provider, Loan Provider
 * Website, Select Loan Type, Loan Product Details, Loan Apply, Upload Docs, and Confirm Details —
 * and wires their forward/back navigation together internally, mirroring how this app registers
 * destinations flatly inside a single shared `NavHost` (no nested `navigation<T>{ ... }`
 * sub-graph is used anywhere in this codebase).
 *
 * Only the wizard's boundary callbacks are exposed to the caller:
 *  - [navigateBack] exits the wizard entirely from its first screen (Select Loan Provider).
 *  - [navigateForPasscodeVerification] and [onSubmitSuccess] are Confirm Details' own boundary
 *    callbacks — see [org.mifos.lib.loan.ui.confirmDetails.confirmDetailsScreen] for their exact
 *    contract. [navigateForPasscodeVerification] is expected to eventually be bound to
 *    `navController.navigateToInternalPasscodeScreen(verificationKey)` by the caller.
 *
 * Picking a provider on Select Loan Provider currently opens Loan Provider Website (rendering
 * that provider's, for now identical, test URL — see
 * [org.mifos.lib.loan.ui.providerWebView.urlForLoanProvider]) instead of continuing into Select
 * Loan Type onwards; the rest of the wizard (Select Loan Type -> Loan Product Details -> Loan
 * Apply -> Upload Docs -> Confirm Details, and each screen's own "back") stays registered and
 * wired exactly as before, via each screen's own `navigateToX` extension, using [navController].
 *
 * @param navController The app's shared nav controller, used to drive the internal forward/back
 * transitions between this wizard's own screens.
 * @param navigateBack Callback to leave the loan-application wizard entirely from its first step.
 * @param navigateForPasscodeVerification `(verificationKey) -> Unit` forwarded, unchanged, to
 * [org.mifos.lib.loan.ui.confirmDetails.confirmDetailsScreen].
 * @param onSubmitSuccess Callback forwarded, unchanged, to
 * [org.mifos.lib.loan.ui.confirmDetails.confirmDetailsScreen], invoked once the loan application has
 * been submitted successfully and the user has acknowledged the success dialog.
 */
fun NavGraphBuilder.loanApplicationGraph(
    navController: NavController,
    navigateBack: () -> Unit,
    navigateForPasscodeVerification: (String) -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    selectLoanProviderScreen(
        navigateBack = navigateBack,
        navigateToLoanProviderWebView = { clientId, providerId, providerUrl ->
            navController.navigateToLoanProviderWebView(
                clientId = clientId,
                providerId = providerId,
                url = providerUrl,
            )
        },
    )

    loanProviderWebViewScreen(
        navigateBack = navController::popBackStack,
    )

    selectLoanTypeScreen(
        navigateBack = navController::popBackStack,
        navigateToLoanProductDetails = { clientId, productId, providerId ->
            navController.navigateToLoanProductDetails(clientId, productId, providerId)
        },
    )

    loanProductDetailsScreen(
        navigateBack = navController::popBackStack,
        navigateToLoanApply = { clientId, productId, providerId ->
            navController.navigateToLoanApply(clientId, productId, providerId)
        },
    )

    loanApplyScreen(
        navigateBack = navController::popBackStack,
        navigateToUploadDocs = {
                clientId,
                productId,
                applicantName,
                loanProductName,
                loanPurpose,
                disbursementDate,
                principalAmount,
                providerId,
            ->
            navController.navigateToUploadDocs(
                clientId = clientId,
                productId = productId,
                applicantName = applicantName,
                loanProductName = loanProductName,
                loanPurpose = loanPurpose,
                disbursementDate = disbursementDate,
                principalAmount = principalAmount,
                providerId = providerId,
            )
        },
    )

    uploadDocsScreen(
        navigateBack = navController::popBackStack,
        navigateToConfirmDetails = {
                clientId,
                productId,
                applicantName,
                loanProductName,
                loanPurpose,
                disbursementDate,
                principalAmount,
                providerId,
            ->
            navController.navigateToConfirmDetails(
                clientId = clientId,
                productId = productId,
                applicantName = applicantName,
                loanProductName = loanProductName,
                loanPurpose = loanPurpose,
                disbursementDate = disbursementDate,
                principalAmount = principalAmount,
                providerId = providerId,
            )
        },
    )

    confirmDetailsScreen(
        navigateBack = navController::popBackStack,
        navigateForPasscodeVerification = navigateForPasscodeVerification,
        onSubmitSuccess = onSubmitSuccess,
    )
}
