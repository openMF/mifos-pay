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
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kpt.core.model.beneficiary.Beneficiary
import kpt.feature.transfer.intrabank.hub.IntraBankHubScreen
@Serializable
data object IntraBankHubRoute

fun NavController.navigateToIntraBankHub(navOptions: NavOptions? = null) {
    this.navigate(IntraBankHubRoute, navOptions)
}

fun NavGraphBuilder.intraBankHubScreen(
    navigateToSelectAccountScreen: () -> Unit,
    navigateToBeneficiary: () -> Unit,
    navigateBack: () -> Unit,
    navigateToTransferConfirm: (
        toOfficeId: Int,
        toClientId: Long,
        toAccountId: Int,
        accountName: String,
        accountNo: String,
    ) -> Unit,
    navigateToHistory: () -> Unit = {},
    navigateToScanQr: () -> Unit = {},
    navigateToRequestMoney: () -> Unit = {},
    navigateToTransferBeneficiary: (Beneficiary) -> Unit = {},
    showTopBar: Boolean = true,
) {
    composable<IntraBankHubRoute> {
        IntraBankHubScreen(
            navigateToSelectAccountScreen = navigateToSelectAccountScreen,
            navigateBack = navigateBack,
            navigateToBeneficiary = navigateToBeneficiary,
            navigateToTransferConfirm = navigateToTransferConfirm,
            navigateToHistory = navigateToHistory,
            navigateToScanQr = navigateToScanQr,
            navigateToRequestMoney = navigateToRequestMoney,
            navigateToTransferBeneficiary = navigateToTransferBeneficiary,
            showTopBar = showTopBar,
        )
    }
}
