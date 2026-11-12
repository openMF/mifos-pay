/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kpt.core.model.utils.QrCodeData
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.mpay.qr.scan.ScanQrCodeScreen
/** Navigation route of the scan-QR screen. */
const val SCAN_QR_ROUTE = "scan_qr_route"

/** Navigates to the scan-QR screen. */
fun NavController.navigateToScanQr(navOptions: NavOptions? = null) =
    navigate(SCAN_QR_ROUTE, navOptions)

/**
 * Registers the scan-QR destination, wiring navigation callbacks for intra-bank, inter-bank and
 * add-beneficiary results.
 */
fun NavGraphBuilder.scanQrScreen(
    navigateBack: () -> Unit,
    navigateToIntraBankTransfer: (QrCodeData) -> Unit,
    navigateToInterbankTransfer: (accountExternalId: String, recipientName: String, amount: String) -> Unit,
    navigateToAddBeneficiaryScreen: (String) -> Unit,
    // from send money pr
//    navigateToSendScreen: (String) -> Unit,
//    navigateToPayeeDetailsScreen: (String) -> Unit,
) {
    composableWithSlideTransitions(route = SCAN_QR_ROUTE) {
        ScanQrCodeScreen(
            navigateBack = navigateBack,
            navigateToIntraBankTransfer = navigateToIntraBankTransfer,
            navigateToInterbankTransfer = navigateToInterbankTransfer,
            navigateToAddBeneficiaryScreen = navigateToAddBeneficiaryScreen,
            // from send money pr
//            navigateToSendScreen = navigateToSendScreen,
//            navigateToPayeeDetailsScreen = navigateToPayeeDetailsScreen,
        )
    }
}
