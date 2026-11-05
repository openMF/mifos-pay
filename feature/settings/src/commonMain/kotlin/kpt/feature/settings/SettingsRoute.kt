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

@Serializable
data object SettingsRoute

@Serializable
data object NotificationRoute

@Serializable
data object SyncAndDraftsRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)

fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)

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

fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<NotificationRoute> {
        NotificationScreen(
            onBackClick = onBackClick,
        )
    }
}

fun NavGraphBuilder.syncAndDraftsDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<SyncAndDraftsRoute> {
        SyncAndDraftsScreen(
            onBackClick = onBackClick,
        )
    }
}
