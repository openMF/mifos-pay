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

/** Navigation route of the profile screen. */
const val PROFILE_ROUTE = "profile_route"

/** Navigates to the profile screen. */
fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_ROUTE, navOptions)
}

/** Registers the profile screen destination. */
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

/** Registers the profile nested graph containing the profile and edit-profile screens. */
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
