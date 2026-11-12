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
import androidx.navigation.NavOptions
import kpt.core.ui.composableWithPushTransitions
import kpt.feature.kyc.KYCLevel2Screen
/** Navigation route of the KYC level 2 screen. */
const val KYC_LEVEL_2_ROUTE = "kyc_level_2_route"

/** Navigates to the KYC level 2 screen. */
fun NavController.navigateToKYCLevel2(navOptions: NavOptions? = null) {
    this.navigate(KYC_LEVEL_2_ROUTE, navOptions)
}

/** Registers the KYC level 2 destination in the navigation graph. */
fun NavGraphBuilder.kycLevel2Screen(
    navigateBack: () -> Unit,
    navigateToLevel3: () -> Unit,
) {
    composableWithPushTransitions(route = KYC_LEVEL_2_ROUTE) {
        KYCLevel2Screen(
            navigateBack = navigateBack,
            navigateToLevel3 = navigateToLevel3,
        )
    }
}
