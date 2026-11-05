/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.uploadDocs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions

/**
 * Type-safe navigation route for the document upload step of the loan application wizard.
 *
 * Carries the fully-populated application form data forward from [org.mifos.lib.loan.ui.loanApply],
 * so it can be threaded through to the Confirm Details step without re-fetching it.
 *
 * @param clientId The id of the client applying for a loan.
 * @param productId The id of the loan product being applied for.
 * @param applicantName The applicant's display name.
 * @param loanProductName The selected loan product's display name.
 * @param loanPurpose The stated purpose for the loan.
 * @param disbursementDate The scheduled date for funds disbursement.
 * @param principalAmount The requested loan amount.
 * @param providerId The id of the loan provider chosen on the Select Loan Provider screen.
 */
@Serializable
data class UploadDocsRoute(
    val clientId: Long,
    val productId: Long,
    val applicantName: String,
    val loanProductName: String,
    val loanPurpose: String,
    val disbursementDate: String,
    val principalAmount: String,
    val providerId: String,
)

/**
 * Navigates to the document upload step with the fully-populated application form data.
 */
fun NavController.navigateToUploadDocs(
    clientId: Long,
    productId: Long,
    applicantName: String,
    loanProductName: String,
    loanPurpose: String,
    disbursementDate: String,
    principalAmount: String,
    providerId: String,
    navOptions: NavOptions? = null,
) {
    navigate(
        UploadDocsRoute(
            clientId = clientId,
            productId = productId,
            applicantName = applicantName,
            loanProductName = loanProductName,
            loanPurpose = loanPurpose,
            disbursementDate = disbursementDate,
            principalAmount = principalAmount,
            providerId = providerId,
        ),
        navOptions,
    )
}

/**
 * Registers the document upload screen in the navigation graph.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToConfirmDetails Callback to proceed to the Confirm Details step once all
 * required documents have been uploaded. Receives the same application form data this
 * destination was entered with, so it can be threaded through unchanged.
 */
fun NavGraphBuilder.uploadDocsScreen(
    navigateBack: () -> Unit,
    navigateToConfirmDetails: (
        clientId: Long,
        productId: Long,
        applicantName: String,
        loanProductName: String,
        loanPurpose: String,
        disbursementDate: String,
        principalAmount: String,
        providerId: String,
    ) -> Unit,
) {
    composableWithSlideTransitions<UploadDocsRoute> {
        UploadDocsScreen(
            navigateBack = navigateBack,
            navigateToConfirmDetails = navigateToConfirmDetails,
        )
    }
}
