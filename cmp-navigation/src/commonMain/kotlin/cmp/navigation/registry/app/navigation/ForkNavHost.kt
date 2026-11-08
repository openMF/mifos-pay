/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import cmp.navigation.registry.ForkFeatureRegistry
import cmp.navigation.registry.ForkFeatureRegistry.shellDestinations
import cmp.navigation.registry.app.ui.ForkAppState
import kpt.core.ui.fork.utils.RootTransitionProviders
import kpt.feature.home.navigation.HOME_ROUTE
import kpt.feature.send.money.navigation.navigateToSendMoneyOptionsScreen
import kpt.feature.transfer.interbank.navigation.navigateToInterbankTransfer
import kpt.feature.transfer.intrabank.navigation.navigateToIntraBankHub

/**
 * `SavedStateHandle` key used by callers of the internal passcode screen for
 * the round-trip "did the user verify their passcode?" boolean. Currently
 * consumed by the intra-bank transfer auth gate
 * (`TransferConfirmScreen` → `TransferConfirmViewModel`); the settings
 * disable-biometrics flow uses its own
 * `DISABLE_BIOMETRICS_VERIFICATION_KEY` from `:feature:settings`.
 */
const val AUTHENTICATION_VERIFICATION_KEY = "org.mifospay.mifos.authentication_verification_success"

/**
 * The signed-in app's navigation host, hung off `ForkNavGraph.MAIN_GRAPH` from [RootNavGraph].
 *
 * It owns no feature wiring. Every destination comes from [ForkFeatureRegistry]: the
 * `@FeatureDestination` graphs and the wiring between features through
 * [ForkFeatureRegistry.featureDestinations], and the passcode gate, settings and change password
 * through [ForkFeatureRegistry.shellDestinations], which need the root's logout and locale actions.
 * Only the transfer-options sheet, which belongs to this shell, is registered here.
 */
@Composable
internal fun ForkNavHost(
    appState: ForkAppState,
    onClickLogout: () -> Unit,
    handleAppLocale: (locale: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController

    NavHost(
        route = ForkNavGraph.MAIN_GRAPH,
        startDestination = HOME_ROUTE,
        navController = navController,
        modifier = modifier,
        // Explicit lightweight fade for every destination that does NOT declare its own
        // transition (the four bottom-nav tabs + ~10 other plain composables). Without this the
        // JetBrains nav lib falls back to a 700ms fadeIn/fadeOut crossfade that keeps two heavy
        // screens alpha-composited for 0.7s — the primary source of the transition lag. Fade (not
        // slide) is the correct motion for sibling bottom-nav tabs; per-destination slide/push
        // transitions (composableWithSlideTransitions/…PushTransitions) still override this default.
        enterTransition = { RootTransitionProviders.Enter.fadeIn(this) },
        exitTransition = { RootTransitionProviders.Exit.fadeOut(this) },
        popEnterTransition = { RootTransitionProviders.Enter.fadeIn(this) },
        popExitTransition = { RootTransitionProviders.Exit.fadeOut(this) },
    ) {
        shellDestinations(navController, onLogout = onClickLogout, handleAppLocale = handleAppLocale)
        ForkFeatureRegistry.featureDestinations(this, navController)

        transferOptionsDialog(
            onIntraBankTransferClick = navController::navigateToIntraBankHub,
            onInterBankTransferClick = navController::navigateToInterbankTransfer,
            onUpiSendMoney = navController::navigateToSendMoneyOptionsScreen,
            onDismiss = {
                navController.popBackStack()
            },
        )
    }
}
