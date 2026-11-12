/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.feature.onboarding.ui.OnboardingScreen

/** First-run onboarding — three intro pages and the privacy consent page. */
@Serializable
data object OnboardingRoute

/** Opens onboarding. */
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) = navigate(OnboardingRoute, navOptions)

/**
 * Onboarding is NOT annotated `@FeatureDestination`: that annotation registers onto the AUTHENTICATED graph, and
 * onboarding runs before it. `cmp-navigation` `RootNavScreen` installs it on the root graph and routes
 * `RootNavState.ShowOnboarding` to it.
 *
 * There is no "finished" callback: finishing marks first run done, RootNavViewModel resolves the next root state
 * (sign-in for an app with a login, the app itself otherwise) and the shell navigates there on its own.
 *
 * @param onOpenServerConfig opens the app's server-setup screen from the privacy page; `null` hides that link (the
 *   template has no server setup — a fork with one passes it).
 */
fun NavGraphBuilder.onboardingDestination(onOpenServerConfig: (() -> Unit)? = null) {
    composableWithStayTransitions<OnboardingRoute> {
        OnboardingScreen(onOpenServerConfig = onOpenServerConfig)
    }
}
