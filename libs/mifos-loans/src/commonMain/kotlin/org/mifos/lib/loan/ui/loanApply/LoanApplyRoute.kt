/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.loanApply

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions

/**
 * Type-safe navigation route for the loan application form screen.
 *
 * @param clientId The id of the client applying for a loan.
 * @param productId The id of the loan product being applied for.
 * @param providerId The id of the loan provider chosen on the Select Loan Provider screen.
 */
@Serializable
data class LoanApplyRoute(
    val clientId: Long,
    val productId: Long,
    val providerId: String,
)

/**
 * Navigates to the loan application form for the given [clientId]/[productId]/[providerId].
 */
fun NavController.navigateToLoanApply(
    clientId: Long,
    productId: Long,
    providerId: String,
    navOptions: NavOptions? = null,
) {
    navigate(LoanApplyRoute(clientId, productId, providerId), navOptions)
}

/**
 * Registers the loan application form screen in the navigation graph.
 *
 * The next step in the wizard — document upload — is not implemented yet, so
 * [navigateToUploadDocs] is left as a plain callback here. Once `org.mifos.lib.loan.ui.uploadDocs`
 * exists, the caller wiring this destination should bind it to that package's
 * `NavController.navigateToUploadDocsScreen(...)` extension. The expected shape of that
 * destination's route, based on the fields threaded through here, is:
 *
 * ```
 * @Serializable
 * data class UploadDocsRoute(
 *     val clientId: Long,
 *     val productId: Long,
 *     val applicantName: String,
 *     val loanProductName: String,
 *     val loanPurpose: String,
 *     val disbursementDate: String,
 *     val principalAmount: String,
 *     val providerId: String,
 * )
 * ```
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToUploadDocs Callback to proceed to the document upload step with the
 * fully-populated application form data.
 */
fun NavGraphBuilder.loanApplyScreen(
    navigateBack: () -> Unit,
    navigateToUploadDocs: (
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
    composableWithSlideTransitions<LoanApplyRoute> {
        LoanApplyScreen(
            navigateBack = navigateBack,
            navigateToUploadDocs = navigateToUploadDocs,
        )
    }
}
