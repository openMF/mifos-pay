/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.rootnav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** Route for the root decision point — see `RootNavViewModel`. */
@Serializable
data object RootNavNavigation

/**
 * Add the root navigation screen to the nav graph.
 */
fun NavGraphBuilder.rootNavDestination(onSplashScreenRemoved: () -> Unit) {
    composable<RootNavNavigation> {
        RootNavScreen(onSplashScreenRemoved = onSplashScreenRemoved)
    }
}
