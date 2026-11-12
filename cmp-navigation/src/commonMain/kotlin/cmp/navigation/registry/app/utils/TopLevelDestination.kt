/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.utils

import androidx.compose.ui.graphics.vector.ImageVector
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.finance
import cmp.navigation.generated.resources.history
import cmp.navigation.generated.resources.payments
import cmp.navigation.generated.resources.transaction_history
import cmp.navigation.generated.resources.wallet_app_name
import cmp.navigation.generated.resources.wallet_home
import kpt.core.designsystem.fork.icon.ForkIcons
import org.jetbrains.compose.resources.StringResource
/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
// TODO Profile is not using self api
internal enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: StringResource,
    val titleText: StringResource,
) {
    HOME(
        selectedIcon = ForkIcons.Home,
        unselectedIcon = ForkIcons.HomeBoarder,
        iconText = Res.string.wallet_home,
        titleText = Res.string.wallet_app_name,
    ),
    PAYMENTS(
        selectedIcon = ForkIcons.Payment,
        unselectedIcon = ForkIcons.Payment,
        iconText = Res.string.payments,
        titleText = Res.string.payments,
    ),
    FINANCE(
        selectedIcon = ForkIcons.Finance,
        unselectedIcon = ForkIcons.FinanceBoarder,
        iconText = Res.string.finance,
        titleText = Res.string.finance,
    ),
    HISTORY(
        selectedIcon = ForkIcons.History,
        unselectedIcon = ForkIcons.HistoryBoarder,
        iconText = Res.string.history,
        titleText = Res.string.transaction_history,
    ),
}
