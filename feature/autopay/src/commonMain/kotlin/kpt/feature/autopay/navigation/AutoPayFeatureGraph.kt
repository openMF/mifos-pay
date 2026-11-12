/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.FeatureDestination
import kpt.feature.autopay.autoPayGraph

/** The AutoPay feature's destinations. */
@FeatureDestination
fun NavGraphBuilder.autoPayFeatureGraph(navController: NavController) {
    autoPayGraph(navController)
}
