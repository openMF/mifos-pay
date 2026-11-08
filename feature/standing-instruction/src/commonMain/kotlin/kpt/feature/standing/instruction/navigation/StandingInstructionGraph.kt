/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.FeatureDestination
import kpt.feature.standing.instruction.createOrUpdate.addEditSIScreen
import kpt.feature.standing.instruction.details.siDetailsScreen

/** The standing-instruction feature's destinations: details and the add/edit form. */
@FeatureDestination
fun NavGraphBuilder.standingInstructionGraph(navController: NavController) {
    addEditSIScreen(navigateBack = navController::navigateUp)
    siDetailsScreen(navigateBack = navController::navigateUp)
}
