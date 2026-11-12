/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package cmp.navigation.splash

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** Route for the splash screen. */
@Serializable
data object SplashRoute

/**
 * Registers the splash destination. Takes no callbacks: the splash never decides where to go — `RootNavViewModel`
 * does, and this screen only holds the frame until it has.
 */
fun NavGraphBuilder.splashDestination() {
    composable<SplashRoute> { SplashScreen() }
}

/**
 * Navigates to the splash screen.
 *
 * @param navOptions optional nav options, e.g. to clear the back stack.
 */
fun NavController.navigateToSplash(navOptions: NavOptions? = null) {
    navigate(SplashRoute, navOptions)
}
