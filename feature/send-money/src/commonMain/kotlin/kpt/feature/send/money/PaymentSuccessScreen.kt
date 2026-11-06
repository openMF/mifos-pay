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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.send_money.generated.resources.Res
import mifos_pay.feature.send_money.generated.resources.feature_send_money_banking_name
import mifos_pay.feature.send_money.generated.resources.feature_send_money_done
import mifos_pay.feature.send_money.generated.resources.feature_send_money_paid_to
import mifos_pay.feature.send_money.generated.resources.feature_send_money_payment_success
import mifos_pay.feature.send_money.generated.resources.feature_send_money_payment_success_description
import mifos_pay.feature.send_money.generated.resources.feature_send_money_powered_by_upi
import mifos_pay.feature.send_money.generated.resources.feature_send_money_share_screenshot
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
expect fun PaymentSuccessScreen(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    onNavigateToSendMoneyOptions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentSuccessViewModel = koinViewModel(),
)

@Composable
internal fun PaymentSuccessScreenDefault(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    onNavigateToSendMoneyOptions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentSuccessViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            PaymentSuccessEvent.ShareScreenshot -> onShareScreenshot.invoke()
            PaymentSuccessEvent.NavigateToHome -> onDone.invoke()
            PaymentSuccessEvent.NavigateToSendMoneyOptions -> onNavigateToSendMoneyOptions.invoke()
        }
    }

    MifosScaffold(
        modifier = modifier,
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(KptTheme.spacing.xxl))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                Spacer(modifier = Modifier.height(KptTheme.spacing.xl))

                Box(
                    modifier = Modifier.testTag("payment-success-content"),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                    ) {
                        PaymentSuccessHeader(state)

                        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                        PaymentDetailsCard(state)
                    }
                }

                Spacer(modifier = Modifier.height(KptTheme.spacing.xxl))

                PlaceholderBanner()

                Spacer(modifier = Modifier.height(KptTheme.spacing.xl))

                Upilogo()

                Spacer(modifier = Modifier.height(KptTheme.spacing.xs))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                ActionButtons(
                    onShareScreenshot = {
                        viewModel.trySendAction(PaymentSuccessAction.ShareScreenshot)
                    },
                    onDone = {
                        viewModel.trySendAction(PaymentSuccessAction.Done)
                    },
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
            }
        }
    }
}

@Composable
private fun PaymentSuccessHeader(
    state: PaymentSuccessState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Icon(
            imageVector = MifosIcons.CheckCircle,
            contentDescription = stringResource(Res.string.feature_send_money_payment_success),
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF4CAF50),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = state.formattedAmount,
                style = KptTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KptTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(Res.string.feature_send_money_payment_success_description),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PaymentDetailsCard(
    state: PaymentSuccessState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_send_money_paid_to),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Text(
                text = state.payeeName,
                style = KptTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = KptTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(Res.string.feature_send_money_banking_name, state.upiName),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Text(
                text = state.timestamp,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PlaceholderBanner(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = KptTheme.spacing.lg)
            .background(
                color = KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(KptTheme.spacing.sm),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Advertisement Banner",
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Upilogo(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.feature_send_money_powered_by_upi),
        style = KptTheme.typography.bodySmall,
        color = KptTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun ActionButtons(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        MifosOutlinedButton(
            onClick = onShareScreenshot,
            modifier = Modifier
                .weight(2f),
            text = {
                Text(
                    text = stringResource(Res.string.feature_send_money_share_screenshot),
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = MifosIcons.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        )

        MifosButton(
            onClick = onDone,
            modifier = Modifier
                .weight(1f),
            text = {
                Text(
                    text = stringResource(Res.string.feature_send_money_done),
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            },
        )
    }
}

@Preview
@Composable
fun PaymentSuccessScreenPreview() {
    PaymentSuccessScreen(
        onShareScreenshot = {},
        onDone = {},
        onNavigateToSendMoneyOptions = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun PaymentSuccessHeaderPreview() {
    val state = PaymentSuccessState(
        payeeName = "John Doe",
        amount = "100.00",
        upiName = "JOHN DOE",
        timestamp = "14 August 2025, 11:09 am",
    )
    PaymentSuccessHeader(state = state, modifier = Modifier)
}

@Preview
@Composable
fun PaymentDetailsCardPreview() {
    val state = PaymentSuccessState(
        payeeName = "John Doe",
        amount = "100.00",
        upiName = "JOHN DOE",
        timestamp = "14 August 2025, 11:09 am",
    )
    PaymentDetailsCard(state = state, modifier = Modifier)
}

@Preview
@Composable
fun PlaceholderBannerPreview() {
    PlaceholderBanner(modifier = Modifier)
}

@Preview
@Composable
fun UpilogoPreview() {
    Upilogo(modifier = Modifier)
}

@Preview
@Composable
fun ActionButtonsPreview() {
    ActionButtons(
        onShareScreenshot = {},
        onDone = {},
        modifier = Modifier,
    )
}
