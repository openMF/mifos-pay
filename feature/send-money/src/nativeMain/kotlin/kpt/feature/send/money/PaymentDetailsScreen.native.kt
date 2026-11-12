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
 * iOS-specific implementation of PaymentDetailsScreen.
 *
 * This composable delegates the UI rendering to the common implementation.
 * Screenshot functionality is not implemented for iOS.
 */
@Composable
actual fun PaymentDetailsScreen(
    onBackClick: () -> Unit,
    onPayAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShareScreenshot: () -> Unit,
    modifier: Modifier,
    viewModel: PaymentDetailsViewModel,
    transactionId: String,
) {
    PaymentDetailsScreenDefault(
        onBackClick = onBackClick,
        onPayAgainClick = onPayAgainClick,
        onRetryClick = onRetryClick,
        onShareScreenshot = onShareScreenshot,
        modifier = modifier,
        viewModel = viewModel,
        transactionId = transactionId,
    )
}
