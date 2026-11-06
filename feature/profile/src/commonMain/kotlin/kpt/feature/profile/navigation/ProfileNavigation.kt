/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.profile.fork.ProfileScreen
private const val PROFILE_NAVIGATION = "profile_navigation"
const val PROFILE_ROUTE = "profile_route"

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_ROUTE, navOptions)
}

fun NavGraphBuilder.profileScreen(
    navigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLinkBankAccount: () -> Unit,
    showQrCode: () -> Unit,
) {
    composableWithSlideTransitions(route = PROFILE_ROUTE) {
        ProfileScreen(
            navigateBack = navigateBack,
            onEditProfile = onEditProfile,
            onLinkBackAccount = onLinkBankAccount,
            showQrCode = showQrCode,
        )
    }
}

fun NavGraphBuilder.profileNavGraph(
    navController: NavController,
    navigateBack: () -> Unit,
    onLinkBankAccount: () -> Unit,
    showQrCode: () -> Unit,
) {
    navigation(
        route = PROFILE_NAVIGATION,
        startDestination = PROFILE_ROUTE,
    ) {
        profileScreen(
            navigateBack = navigateBack,
            onEditProfile = navController::navigateToEditProfile,
            onLinkBankAccount = onLinkBankAccount,
            showQrCode = showQrCode,
        )

        editProfileScreen(
            onBackPress = navController::navigateUp,
        )
    }
}
