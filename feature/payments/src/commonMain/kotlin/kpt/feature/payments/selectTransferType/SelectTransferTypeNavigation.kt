/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.payments.selectTransferType

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SelectTransferTypeRoute

fun NavController.navigateToSelectTransferType(navOptions: NavOptions? = null) =
    navigate(SelectTransferTypeRoute, navOptions)

fun NavGraphBuilder.selectTransferTypeScreen(
    onIntraBankTransferClick: () -> Unit,
    onInterBankTransferClick: () -> Unit,
) {
    composable<SelectTransferTypeRoute> {
        SelectTransferTypeScreen(
            onIntraBankTransferClick = onIntraBankTransferClick,
            onInterBankTransferClick = onInterBankTransferClick,
        )
    }
}
