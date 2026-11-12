/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.settings.fork.SettingsScreenRoute
/** Route string for the settings destination. */
const val SETTINGS_ROUTE = "settings_route"

/** Pushes [SETTINGS_ROUTE]. */
fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(SETTINGS_ROUTE, navOptions)
}

/**
 * Registers the settings destination. Hoists the destination's own
 * [androidx.lifecycle.SavedStateHandle] into [SettingsScreenRoute] so the
 * disable-biometrics round-trip channel
 * (`DISABLE_BIOMETRICS_VERIFICATION_KEY`) works.
 *
 * @param navigateToPasscodeScreen `(verificationKey?) -> Unit` — bind to
 *        `navController::navigateToInternalPasscodeScreen`. A non-null
 *        key triggers the round-trip flow (disable biometrics); null means
 *        no channel needed (change passcode).
 */
fun NavGraphBuilder.settingsScreen(
    onBackPress: () -> Unit,
    onLogout: () -> Unit,
    handleAppLocale: (String) -> Unit,
    navigateToPasscodeScreen: (verificationKey: String?) -> Unit,
    navigateToEditPasswordScreen: () -> Unit,
    navigateToFaqScreen: () -> Unit,
    navigateToNotificationScreen: () -> Unit,
    navigateToProfile: () -> Unit,
) {
    composableWithSlideTransitions(route = SETTINGS_ROUTE) { entry ->
        SettingsScreenRoute(
            backPress = onBackPress,
            onEditPassword = navigateToEditPasswordScreen,
            onLogout = onLogout,
            handleAppLocale = handleAppLocale,
            navigateToPasscodeScreen = navigateToPasscodeScreen,
            navigateToFaqScreen = navigateToFaqScreen,
            navigateToNotificationScreen = navigateToNotificationScreen,
            navigateToProfile = navigateToProfile,
            entryStateHandle = entry.savedStateHandle,
        )
    }
}
