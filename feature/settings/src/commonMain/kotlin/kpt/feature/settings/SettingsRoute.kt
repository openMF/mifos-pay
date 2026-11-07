/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions

/** Navigation route of the settings screen. */
@Serializable
data object SettingsRoute

/** Navigation route of the notification screen. */
@Serializable
data object NotificationRoute

/** Navigation route of the sync-and-drafts screen. */
@Serializable
data object SyncAndDraftsRoute

/** Navigates to the settings screen. */
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)

/** Navigates to the notification screen. */
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)

/** Navigates to the sync-and-drafts screen. */
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)

/**
 * The settings backbone destination. [settingsBody] is the fork-owned inner content (default supplied by
 * `cmp-navigation`'s `ForkBackboneRegistry.settingsBody`, which wires the back / sync-and-drafts / dev-menu
 * callbacks into the demo body); this template graph forwards the opaque body. (WS01 base-feature seam,
 * epic AC7 — mirrors the `home` shell/seam split.)
 */
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {}) {
    composableWithPushTransitions<SettingsRoute> {
        settingsBody()
    }
}

/** Registers the notification destination. */
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<NotificationRoute> {
        NotificationScreen(
            onBackClick = onBackClick,
        )
    }
}

/** Registers the sync-and-drafts destination. */
fun NavGraphBuilder.syncAndDraftsDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<SyncAndDraftsRoute> {
        SyncAndDraftsScreen(
            onBackClick = onBackClick,
        )
    }
}
