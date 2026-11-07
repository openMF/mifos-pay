/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kpt.core.ui.composableWithSlideTransitions
/** Navigation route of the notification screen. */
const val NOTIFICATION_ROUTE = "notification_route"

/** Registers the notification screen destination in the navigation graph. */
fun NavGraphBuilder.notificationScreen(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions(NOTIFICATION_ROUTE) {
        NotificationScreen(
            navigateBack = navigateBack,
        )
    }
}

/** Navigates to the notification screen. */
fun NavController.navigateToNotification(navOptions: NavOptions? = null) {
    navigate(NOTIFICATION_ROUTE, navOptions)
}
