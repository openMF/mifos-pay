/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions

@Serializable
data object ProfileRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)

/**
 * The profile backbone destination. [profileBody] is the fork-owned inner content (default supplied by
 * `cmp-navigation`'s `ForkBackboneRegistry.profileBody`); this template graph carries zero demo imports and
 * forwards the opaque body into the [ProfileScreen] shell. (WS01 base-feature seam, epic AC7.)
 */
fun NavGraphBuilder.profileDestination(profileBody: @Composable () -> Unit = {}) {
    composableWithStayTransitions<ProfileRoute> {
        ProfileScreen(profileBody = profileBody)
    }
}
