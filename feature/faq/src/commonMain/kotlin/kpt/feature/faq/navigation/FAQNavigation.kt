/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.faq.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.faq.FaqScreenRoute
const val FAQ_ROUTE = "faq_route"

fun NavController.navigateToFAQ(navOptions: NavOptions? = null) {
    this.navigate(FAQ_ROUTE, navOptions)
}

fun NavGraphBuilder.faqScreen(
    navigateBack: () -> Unit,
) {
    composableWithSlideTransitions(route = FAQ_ROUTE) {
        FaqScreenRoute(
            navigateBack = navigateBack,
        )
    }
}
