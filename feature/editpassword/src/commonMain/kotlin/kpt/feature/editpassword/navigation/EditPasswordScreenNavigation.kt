/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.editpassword.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.editpassword.EditPasswordScreen
/** Navigation route of the edit-password screen. */
const val EDIT_PASSWORD_ROUTE = "edit_password_route"

/**
 * Registers the edit-password screen; [navigateBack] pops it, [onLogOut] runs after a password change.
 */
fun NavGraphBuilder.editPasswordScreen(
    navigateBack: () -> Unit,
    onLogOut: () -> Unit,
) {
    composableWithSlideTransitions(route = EDIT_PASSWORD_ROUTE) {
        EditPasswordScreen(
            navigateBack = navigateBack,
            onLogout = onLogOut,
        )
    }
}

/** Navigates to the edit-password screen. */
fun NavController.navigateToEditPassword() {
    this.navigate(EDIT_PASSWORD_ROUTE)
}
