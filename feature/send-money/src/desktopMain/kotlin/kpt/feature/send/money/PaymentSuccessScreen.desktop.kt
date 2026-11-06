/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Desktop stub implementation of PaymentSuccessScreen.
 *
 * Screenshot functionality is not implemented for Desktop yet.
 */
@Composable
actual fun PaymentSuccessScreen(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    onNavigateToSendMoneyOptions: () -> Unit,
    modifier: Modifier,
    viewModel: PaymentSuccessViewModel,
) {
    PaymentSuccessScreenDefault(
        onShareScreenshot = onShareScreenshot,
        onDone = onDone,
        onNavigateToSendMoneyOptions = onNavigateToSendMoneyOptions,
        modifier = modifier,
        viewModel = viewModel,
    )
}
