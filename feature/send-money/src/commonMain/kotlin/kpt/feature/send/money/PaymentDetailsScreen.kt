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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.AvatarBox
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
expect fun PaymentDetailsScreen(
    onBackClick: () -> Unit,
    onPayAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShareScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentDetailsViewModel = koinViewModel(),
    transactionId: String,
)

@Composable
internal fun PaymentDetailsScreenDefault(
    onBackClick: () -> Unit,
    onPayAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShareScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentDetailsViewModel = koinViewModel(),
    transactionId: String,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var isButtonVisible by remember { mutableStateOf(true) }

    LaunchedEffect(transactionId) {
        viewModel.initialize(transactionId)
    }

    LaunchedEffect(scrollState.value) {
        isButtonVisible = scrollState.value <= 100
    }

    MifosScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MifosTopBar(
                topBarTitle = "Payment Details",
                backPress = onBackClick,
            )
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
                    .testTag("payment-details-content"),
            ) {
                ProfileAndRecipientSection(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

                PaymentSummarySection(
                    state = state,
                    onPayAgainClick = onPayAgainClick,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

                StatusSection(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    HorizontalDivider(
                        Modifier.width(300.dp),
                        DividerDefaults.Thickness,
                        KptTheme.colorScheme.outline.copy(alpha = 0.3f),
                    )
                }
                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                TransactionDateTimeSection(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KptTheme.spacing.lg)
                        .background(
                            color = KptTheme.colorScheme.surface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = KptTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        )
                        .padding(KptTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
                ) {
                    TransactionMetadataSection(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    DetailedInfoSection(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(KptTheme.spacing.xxl))

                BrandingSection(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (isButtonVisible) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = KptTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KptTheme.spacing.lg)
                            .padding(vertical = KptTheme.spacing.lg),
                    ) {
                        Button(
                            onClick = onShareScreenshot,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KptTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Share Screenshot",
                                style = KptTheme.typography.labelLarge,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAndRecipientSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        if (state.profileImageUrl != null) {
            AvatarBox(
                name = state.payeeName,
                size = 80,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(KptTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.payeeName.firstOrNull()?.uppercase() ?: "?",
                    style = KptTheme.typography.headlineLarge,
                    color = KptTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = state.payeeName,
                style = KptTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = KptTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = if (state.isBusiness) {
                    state.bankingName
                } else {
                    "${state.phoneNumber} · ${state.upiId}"
                },
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PaymentSummarySection(
    state: PaymentDetailsState,
    onPayAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = state.formattedAmount,
            style = KptTheme.typography.displayMedium,
            fontWeight = FontWeight.Normal,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (state.note.isNotBlank()) {
            Text(
                text = state.note,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (state.isPaymentSuccessful) {
            Button(
                onClick = onPayAgainClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.primary,
                ),
                modifier = Modifier.wrapContentWidth(),
            ) {
                Text(
                    text = "Pay Again",
                    style = KptTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                )
            }
        } else {
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.error,
                ),
                modifier = Modifier.wrapContentWidth(),
            ) {
                Text(
                    text = "Retry",
                    style = KptTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun StatusSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        if (state.isPaymentSuccessful) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Icon(
                    imageVector = MifosIcons.CheckCircle,
                    contentDescription = "Payment Successful",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF4CAF50),
                )
                Text(
                    text = "Completed",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4CAF50),
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Icon(
                    imageVector = MifosIcons.Info,
                    contentDescription = "Payment Failed",
                    modifier = Modifier.size(48.dp),
                    tint = KptTheme.colorScheme.error,
                )
                Text(
                    text = "Payment Failed",
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = KptTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun TransactionDateTimeSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.transactionDate,
            style = KptTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

// TODO improve UI/UX

@Composable
private fun TransactionMetadataSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                Icon(
                    imageVector = MifosIcons.Bank,
                    contentDescription = "Bank",
                    modifier = Modifier.size(24.dp),
                    tint = KptTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = state.payerBankName,
                        style = KptTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal,
                        color = KptTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = state.payerAccountLast4Digits,
                        style = KptTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) MifosIcons.ExpandLess else MifosIcons.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = KptTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider()

        if (isExpanded) {
            PaymentTimelineSection(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun PaymentTimelineSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
    ) {
        TimelineStep(
            step = "Payment Started",
            isCompleted = true,
            isFirst = true,
        )

        if (state.isPaymentSuccessful) {
            TimelineStep(
                step = "Payment received by ${state.payeeName}",
                isCompleted = true,
            )

            TimelineStep(
                step = if (state.isBusiness) "Purchase confirmed" else "Payment Completed",
                isCompleted = true,
                isLast = true,
            )
        } else {
            TimelineStep(
                step = "Payment to ${state.payeeName} failed. Any money debited would be refunded within 3 working days.",
                isCompleted = false,
                isError = true,
            )

            TimelineStep(
                step = if (state.isBusiness) "Waiting for purchase confirmation" else "Waiting for payment completion",
                isCompleted = false,
                isLast = true,
            )
        }
    }
}

@Composable
private fun TimelineStep(
    step: String,
    isCompleted: Boolean,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            if (!isFirst) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isCompleted) KptTheme.colorScheme.primary else KptTheme.colorScheme.outline.copy(alpha = 0.3f),
                                ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isError -> KptTheme.colorScheme.error
                            isCompleted -> Color(0xFF4CAF50)
                            else -> KptTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted && !isError) {
                    Icon(
                        imageVector = MifosIcons.Check,
                        contentDescription = "Completed",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White,
                    )
                } else if (isError) {
                    Icon(
                        imageVector = MifosIcons.Info,
                        contentDescription = "Error",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White,
                    )
                }
            }

            if (!isLast) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isCompleted) KptTheme.colorScheme.primary else KptTheme.colorScheme.outline.copy(alpha = 0.3f),
                                ),
                        )
                    }
                }
            }
        }

        Text(
            text = step,
            style = KptTheme.typography.bodyMedium,
            color = if (isError) KptTheme.colorScheme.error else KptTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DetailedInfoSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
    ) {
        InfoRow(
            label = "UPI Transaction ID",
            value = "123456789012",
        )

        InfoRow(
            label = "To",
            value = "${state.payeeName.uppercase()}\n${state.upiAppName} · ${state.upiId}",
        )

        InfoRow(
            label = "From",
            value = "${state.payerName.uppercase()} (${state.payerBankName})\n${state.payerUpiAppName} · ${state.payerUpiId}",
        )

        InfoRow(
            label = "MifosPay Transaction ID",
            value = "AbC123dEf456",
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
    ) {
        Text(
            text = label,
            style = KptTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = KptTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color = KptTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BrandingSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = "Powered by UPI",
            style = KptTheme.typography.labelLarge,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Mifos Pay",
            style = KptTheme.typography.labelLarge,
            color = KptTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Preview
@Composable
fun PaymentDetailsScreenPreview() {
    PaymentDetailsScreen(
        onBackClick = {},
        onPayAgainClick = {},
        onRetryClick = {},
        onShareScreenshot = {},
        transactionId = "",
    )
}
