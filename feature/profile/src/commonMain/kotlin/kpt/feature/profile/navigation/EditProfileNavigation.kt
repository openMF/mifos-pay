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
import kpt.core.ui.composableWithPushTransitions
import kpt.feature.profile.edit.EditProfileScreen
/** Navigation route of the edit-profile screen. */
const val EDIT_PROFILE_ROUTE = "edit_profile_route"

/** Navigates to the edit-profile screen. */
fun NavController.navigateToEditProfile(navOptions: NavOptions? = null) =
    navigate(EDIT_PROFILE_ROUTE, navOptions)

internal fun NavGraphBuilder.editProfileScreen(
    onBackPress: () -> Unit,
) {
    composableWithPushTransitions(route = EDIT_PROFILE_ROUTE) {
        EditProfileScreen(
            onBackClick = onBackPress,
        )
    }
}
