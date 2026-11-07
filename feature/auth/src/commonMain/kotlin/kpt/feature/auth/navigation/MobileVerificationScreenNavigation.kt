/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MaxLineLength")

package kpt.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.auth.mobileVerify.MobileVerificationScreen
/** Navigation route of the mobile verification screen. */
const val MOBILE_VERIFICATION_ROUTE = "mobile_verification_route"

/** Registers the mobile verification destination in the nav graph. */
fun NavGraphBuilder.mobileVerificationScreen(
    onNavigateBack: () -> Unit,
    onOtpVerificationSuccess: (String) -> Unit,
) {
    composableWithSlideTransitions(
        route = MOBILE_VERIFICATION_ROUTE,
    ) {
        MobileVerificationScreen(
            onNavigateBack = onNavigateBack,
            onOtpVerificationSuccess = onOtpVerificationSuccess,
        )
    }
}

/** Navigates to the mobile verification screen. */
fun NavController.navigateToMobileVerification() {
    this.navigate(MOBILE_VERIFICATION_ROUTE)
}
