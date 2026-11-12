/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.confirmDetails

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions
import kpt.core.ui.composableWithSlideTransitions
/**
 * Type-safe navigation route for the final "Confirm Details" step of the loan application
 * wizard. Carries the same application form data forwarded, unchanged, all the way from
 * [org.mifos.lib.loan.ui.loanApply] through [org.mifos.lib.loan.ui.uploadDocs].
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
data class ConfirmDetailsRoute(
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
 * Navigates to the Confirm Details step with the specified application data.
 */
fun NavController.navigateToConfirmDetails(
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
        ConfirmDetailsRoute(
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
 * Registers the Confirm Details screen in the navigation graph.
 *
 * Hoists the destination's own `SavedStateHandle` into [ConfirmDetailsScreen] so the
 * [LOAN_APPLICATION_VERIFICATION_KEY] passcode-gate round trip works, mirroring
 * `transferConfirmScreen` in `:feature:transfer-intrabank`.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateForPasscodeVerification `(verificationKey) -> Unit` — bind to
 * `navController::navigateToInternalPasscodeScreen`. The screen forwards
 * [LOAN_APPLICATION_VERIFICATION_KEY] so the passcode-screen success/failure callbacks write the
 * boolean result back to this destination's saved-state-handle.
 * @param onSubmitSuccess Callback invoked once the loan application has been submitted
 * successfully and the user has acknowledged the success dialog. Callers are expected to
 * navigate away from the loan-application flow entirely (e.g. back to the Accounts screen).
 */
fun NavGraphBuilder.confirmDetailsScreen(
    navigateBack: () -> Unit,
    navigateForPasscodeVerification: (String) -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    composableWithSlideTransitions<ConfirmDetailsRoute> { backStackEntry ->
        ConfirmDetailsScreen(
            navigateBack = navigateBack,
            navigateForPasscodeVerification = navigateForPasscodeVerification,
            onSubmitSuccess = onSubmitSuccess,
            entryStateHandle = backStackEntry.savedStateHandle,
        )
    }
}
