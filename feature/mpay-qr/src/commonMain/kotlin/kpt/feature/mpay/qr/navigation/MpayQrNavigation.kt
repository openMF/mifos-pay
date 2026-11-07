/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kpt.core.ui.composableWithPushTransitions
import kpt.feature.mpay.qr.MpayQrScreen
/** Navigation route of the Mpay QR screen. */
const val MPAY_QR_ROUTE = "mpay_qr_route"

/** Registers the Mpay QR screen destination in the navigation graph. */
fun NavGraphBuilder.mpayQrScreen(
    navigateBack: () -> Unit,
    navigateToSendScreen: (String) -> Unit,
    navigateToPayeeDetailsScreen: (String) -> Unit,
) {
    composableWithPushTransitions(
        route = MPAY_QR_ROUTE,
    ) {
        MpayQrScreen(
            navigateBack = navigateBack,
            // from #1906 pr
//            navigateToSendScreen = navigateToSendScreen,
//            navigateToPayeeDetailsScreen = navigateToPayeeDetailsScreen,
        )
    }
}

/** Navigates to the Mpay QR screen. */
fun NavController.navigateToMpayQrScreen(navOptions: NavOptions? = null) {
    navigate(MPAY_QR_ROUTE, navOptions)
}
