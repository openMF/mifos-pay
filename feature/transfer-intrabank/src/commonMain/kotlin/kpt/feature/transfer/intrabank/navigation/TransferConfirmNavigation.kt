/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions
import kpt.feature.transfer.intrabank.confirm.TransferConfirmScreen
import kpt.feature.transfer.intrabank.confirm.TransferResult
/**
 * Type-safe route for the intra-bank transfer confirmation screen. Carries
 * the source/destination account snapshot and the post-success destination
 * the caller wants to return to (`home` or `payments`).
 *
 * @property amount Amount to transfer.
 * @property accountId Id of the source account.
 * @property toOfficeId Office id of the destination account, or null.
 * @property toClientId Client id of the destination account, or null.
 * @property toAccountTypeId Account type id of the destination account, or null.
 * @property toAccountName Destination account holder name.
 * @property toAccountNo Destination account number.
 * @property returnDestination Destination to return to after the transfer.
 */
@Serializable
data class TransferConfirmRoute(
    val amount: Int,
    val accountId: Long,
    val toOfficeId: Int? = null,
    val toClientId: Long? = null,
    val toAccountTypeId: Int? = null,
    val toAccountName: String = "",
    val toAccountNo: String = "",
    val returnDestination: String = "home",
)

/**
 * Push the transfer-confirm screen with a fully-populated
 * [TransferConfirmRoute].
 */
fun NavController.navigateToTransferConfirm(
    toOfficeId: Int?,
    toClientId: Long?,
    toAccountTypeId: Int?,
    toAccountId: Int,
    amount: Int,
    toAccountName: String,
    toAccountNo: String,
    returnDestination: String = "home",
    navOptions: NavOptions? = null,
) {
    this.navigate(
        TransferConfirmRoute(
            toOfficeId = toOfficeId,
            toClientId = toClientId,
            toAccountTypeId = toAccountTypeId,
            accountId = toAccountId.toLong(),
            amount = amount,
            toAccountName = toAccountName,
            toAccountNo = toAccountNo,
            returnDestination = returnDestination,
        ),
        navOptions,
    )
}

/**
 * Registers the transfer-confirm destination. Hoists the destination's own
 * `SavedStateHandle` into [TransferConfirmScreen] so the
 * `INTRA_BANK_TRANSFER_VERIFICATION_KEY` round-trip channel works.
 *
 * @param navigateForPasscodeVerification `(verificationKey) -> Unit` —
 *        bind to `navController::navigateToInternalMifosPasscodeScreen`.
 *        The screen forwards `INTRA_BANK_TRANSFER_VERIFICATION_KEY` so the
 *        passcode-screen success/failure callbacks write the boolean back to
 *        this destination's saved-state-handle.
 */
fun NavGraphBuilder.transferConfirmScreen(
    navigateBack: () -> Unit,
    onTransferSuccess: (TransferResult, String) -> Unit,
    navigateForPasscodeVerification: (String) -> Unit,
) {
    composableWithSlideTransitions<TransferConfirmRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<TransferConfirmRoute>()
        TransferConfirmScreen(
            navigateBack = navigateBack,
            onTransferSuccess = { transferResult ->
                onTransferSuccess(transferResult, route.returnDestination)
            },
            navigateForPasscodeVerification = navigateForPasscodeVerification,
            entryStateHandle = backStackEntry.savedStateHandle,
        )
    }
}
