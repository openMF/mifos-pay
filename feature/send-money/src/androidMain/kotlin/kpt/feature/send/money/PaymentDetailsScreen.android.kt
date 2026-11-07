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

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.ui.fork.utils.ScreenshotContextDetails
import kpt.core.ui.fork.utils.ShareUtils
/**
 * Android-specific implementation of PaymentDetailsScreen with screenshot functionality.
 *
 * This composable sets up the necessary providers for screenshot and sharing functionality
 * and delegates the UI rendering to the common implementation.
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
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(activity) {
        if (activity != null) {
            ShareUtils.setActivityProvider { activity }
            PaymentDetailsScreenshotUtils.setActivityProvider { activity }
        }
        onDispose { }
    }

    val handleShareScreenshot = remember {
        {
            coroutineScope.launch {
                try {
                    Logger.d("Taking payment details screenshot and sharing...")

                    val state = viewModel.stateFlow.value
                    val contextDetails = ScreenshotContextDetails(
                        screenType = "Payment Details",
                        amount = state.formattedAmount,
                        recipientName = state.payeeName,
                        timestamp = state.transactionDate,
                        customSuffix = "UPI",
                    )

                    PaymentDetailsScreenshotUtils.takePaymentDetailsScreenshotAndShare(
                        contextDetails = contextDetails,
                        onError = { errorMessage ->
                            Logger.e("Screenshot failed: $errorMessage")
                        },
                    )
                } catch (e: Exception) {
                    Logger.e(e) { "Failed to take screenshot: ${e.message}" }
                }
            }
            Unit
        }
    }

    PaymentDetailsScreenDefault(
        onBackClick = onBackClick,
        onPayAgainClick = onPayAgainClick,
        onRetryClick = onRetryClick,
        onShareScreenshot = handleShareScreenshot,
        modifier = modifier,
        viewModel = viewModel,
        transactionId = transactionId,
    )
}
