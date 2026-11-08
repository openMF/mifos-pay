/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.FeatureDestination
import kpt.feature.savedcards.createOrUpdate.addEditCardScreen
import kpt.feature.savedcards.details.cardDetailRoute

/** The saved-cards feature's destinations: card details and the add/edit form. */
@FeatureDestination
fun NavGraphBuilder.savedCardsGraph(navController: NavController) {
    cardDetailRoute(navigateBack = navController::navigateUp)
    addEditCardScreen(navigateBack = navController::navigateUp)
}
