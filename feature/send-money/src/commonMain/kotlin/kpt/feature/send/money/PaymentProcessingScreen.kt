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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkGradientBackground
import kpt.core.designsystem.fork.component.ForkLoadingWheel
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.send.money.generated.resources.Res
import kpt.feature.send.money.generated.resources.feature_send_money_paid_to
import kpt.feature.send.money.generated.resources.feature_send_money_paying_securely
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/** Screen shown while a payment is being processed. */
@Composable
fun PaymentProcessingScreen(
    onPaymentComplete: (String, String, String, String) -> Unit,
    onPaymentFailed: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentProcessingViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is PaymentProcessingEvent.PaymentComplete -> onPaymentComplete.invoke(
                event.payeeName,
                event.amount,
                event.upiName,
                event.transactionTimestamp,
            )
            is PaymentProcessingEvent.PaymentFailed -> onPaymentFailed.invoke(event.errorMessage)
        }
    }

    ForkGradientBackground {
        ForkScaffold(
            modifier = modifier,
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                PaymentProcessingContent(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun PaymentProcessingContent(
    state: PaymentProcessingState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.isProcessing) {
            ForkLoadingWheel(
                contentDesc = "Processing Payment",
                modifier = Modifier.size(120.dp),
            )
        } else {
            Icon(
                imageVector = ForkIcons.CheckCircle,
                contentDescription = "Payment Complete",
                modifier = Modifier.size(120.dp),
                tint = KptTheme.colorScheme.primary,
            )
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(KptTheme.spacing.xl))

        Text(
            text = if (state.isProcessing) {
                stringResource(
                    Res.string.feature_send_money_paying_securely,
                    state.formattedAmount,
                )
            } else {
                stringResource(Res.string.feature_send_money_paid_to)
            },
            style = KptTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(KptTheme.spacing.md))

        Text(
            text = state.payeeName,
            style = KptTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

/** Preview of [PaymentProcessingScreen]. */
@Preview
@Composable
fun PaymentProcessingScreenPreview() {
    PaymentProcessingScreen(
        onPaymentComplete = { _, _, _, _ -> },
        onPaymentFailed = {},
        modifier = Modifier,
    )
}

/** Preview of the payment processing content. */
@Preview
@Composable
fun PaymentProcessingContentPreview() {
    val state = PaymentProcessingState(
        payeeName = "John Doe",
        amount = "100.00",
        isProcessing = true,
    )
    PaymentProcessingContent(
        state = state,
        modifier = Modifier.fillMaxSize(),
    )
}
