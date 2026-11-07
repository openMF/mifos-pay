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
import kpt.core.ui.fork.utils.PaymentSuccessScreenshotUtils
import kpt.core.ui.fork.utils.ScreenshotContextDetails
import kpt.core.ui.fork.utils.ShareUtils
/**
 * Android-specific implementation of PaymentSuccessScreen with screenshot functionality.
 *
 * This composable sets up the necessary providers for screenshot and sharing functionality
 * and delegates the UI rendering to the common implementation.
 */
@Composable
actual fun PaymentSuccessScreen(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    onNavigateToSendMoneyOptions: () -> Unit,
    modifier: Modifier,
    viewModel: PaymentSuccessViewModel,
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(activity) {
        if (activity != null) {
            PaymentSuccessScreenshotUtils.setActivityProvider { activity }
            ShareUtils.setActivityProvider { activity }
        }
        onDispose { }
    }

    val handleShareScreenshot = remember {
        {
            coroutineScope.launch {
                try {
                    Logger.d("Taking payment success screenshot and sharing...")

                    val state = viewModel.stateFlow.value
                    val contextDetails = ScreenshotContextDetails(
                        screenType = "Payment Success",
                        amount = state.formattedAmount,
                        recipientName = state.payeeName,
                        timestamp = state.timestamp,
                        customSuffix = "UPI",
                    )

                    PaymentSuccessScreenshotUtils.takePaymentSuccessScreenshotAndShare(
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

    PaymentSuccessScreenDefault(
        onShareScreenshot = handleShareScreenshot,
        onDone = onDone,
        onNavigateToSendMoneyOptions = onNavigateToSendMoneyOptions,
        modifier = modifier,
        viewModel = viewModel,
    )
}
