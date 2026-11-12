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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import cmp.navigation.registry.app.ui.components.TransferOptionsBottomSheet
import kotlinx.serialization.Serializable

/** Navigation route of the transfer-options dialog. */
@Serializable
data object TransferOptionsRoute

/** Navigates to the transfer-options dialog. */
fun NavController.navigateToTransferOptions() {
    this.navigate(TransferOptionsRoute)
}

/** Registers the transfer-options bottom-sheet dialog and wires its click callbacks. */
fun NavGraphBuilder.transferOptionsDialog(
    onIntraBankTransferClick: () -> Unit,
    onInterBankTransferClick: () -> Unit,
    onUpiSendMoney: () -> Unit,
    onDismiss: () -> Unit,
) {
    dialog<TransferOptionsRoute> {
        TransferOptionsBottomSheet(
            onIntraBankTransferClick = {
                onIntraBankTransferClick()
            },
            onInterBankTransferClick = {
                onInterBankTransferClick()
            },
            onUpiSendMoney = {
                onUpiSendMoney()
            },
            onDismiss = onDismiss,
        )
    }
}
