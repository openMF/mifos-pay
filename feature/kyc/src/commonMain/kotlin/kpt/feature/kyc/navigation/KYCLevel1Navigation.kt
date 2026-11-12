/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.ui.composableWithPushTransitions
import kpt.feature.kyc.KYCLevel1Screen
/** Navigation route of the KYC level 1 screen. */
const val KYC_LEVEL_1_ROUTE = "kyc_level_1_route"

/** Navigates to the KYC level 1 screen. */
fun NavController.navigateToKYCLevel1() {
    this.navigate(KYC_LEVEL_1_ROUTE)
}

/** Registers the KYC level 1 destination in the navigation graph. */
fun NavGraphBuilder.kycLevel1Screen(
    navigateBack: () -> Unit,
    navigateToKycLevel2: () -> Unit,
) {
    composableWithPushTransitions(route = KYC_LEVEL_1_ROUTE) {
        KYCLevel1Screen(
            navigateBack = navigateBack,
            navigateToKycLevel2 = navigateToKycLevel2,
        )
    }
}
