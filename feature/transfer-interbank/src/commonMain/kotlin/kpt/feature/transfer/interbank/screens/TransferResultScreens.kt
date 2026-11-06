/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.interbank.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import mifos_pay.feature.transfer_interbank.generated.resources.Res
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_amount_transferred
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_attempted_amount
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_available_balance_label
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_back_to_home
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_description
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_edit_transfer
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_failed
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_from_account_label
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_success
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_to_account_label
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transaction_date_label
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transaction_failed
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transaction_reference
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transfer_completed
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transfer_failed
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transfer_successful
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun TransferSuccessScreen(
    recipientName: String,
    amount: String,
    transactionReference: String = "TXN-20250911-0001",
    fromAccount: String = "WALLET - #0000000001",
    fromAccountName: String = "TOMAS ASCENCIO ASCENCIO",
    toAccount: String = "Pedro Barreto",
    toAccountNumber: String = "Account: 9388006020",
    transactionDate: String = "11/09/25 at 09:41 AM",
    description: String = "Interbank Transfer",
    currencyCode: String = "MXN",
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    MifosScaffold(
        modifier = modifier,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                MifosButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.feature_send_interbank_back_to_home))
                }
            }
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(KptTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Success Icon with Animation
            item {
                var animationPlayed by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    animationPlayed = true
                }

                val scale by animateFloatAsState(
                    targetValue = if (animationPlayed) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "successIconScale",
                )

                val successGreen = Color(0xFF4CAF50)

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(
                            color = successGreen.copy(alpha = 0.1f),
                            shape = KptTheme.shapes.large,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MifosIcons.Check,
                        contentDescription = stringResource(Res.string.feature_send_interbank_success),
                        modifier = Modifier.size(56.dp),
                        tint = successGreen,
                    )
                }
            }

            // Title and Subtitle
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_transfer_successful),
                        style = KptTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_transfer_completed, recipientName),
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Transaction Reference
            item {
                MifosCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = KptTheme.colorScheme.surface,
                    ),
                    shape = KptTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(KptTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_send_interbank_transaction_reference),
                            style = KptTheme.typography.labelMedium,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f).padding(end = KptTheme.spacing.xs),
                                text = transactionReference,
                                style = KptTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = KptTheme.colorScheme.primary,
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(transactionReference))
                                    copied = true
                                },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = MifosIcons.Copy,
                                    contentDescription = "Copy transaction reference",
                                    modifier = Modifier.size(20.dp),
                                    tint = KptTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = KptTheme.colorScheme.surface,
                    ),
                    shape = KptTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(KptTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                    ) {
                        // Amount
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_amount_transferred),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "$currencyCode $amount",
                                style = KptTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = KptTheme.colorScheme.primary,
                            )
                        }

                        // From Account
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_from_account_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = fromAccount,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = fromAccountName,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // To Account
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_to_account_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = toAccount,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = toAccountNumber,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Date
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_transaction_date_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = transactionDate,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_description),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = description,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferFailedScreen(
    errorMessage: String,
    errorTitle: String = "Insufficient Balance",
    attemptedAmount: String = "MXN 1.00",
    availableBalance: String = "MXN 5,000.00",
    fromAccount: String = "WALLET - #0000000001",
    fromAccountName: String = "TOMAS ASCENCIO ASCENCIO",
    toAccount: String = "Pedro Barreto",
    toAccountNumber: String = "Account: 9388006020",
    transactionDate: String = "11/09/25 at 09:41 AM",
    description: String = "Interbank Transfer",
    onRetry: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                MifosButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.feature_send_interbank_edit_transfer))
                }

                MifosButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.feature_send_interbank_back_to_home))
                }
            }
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(KptTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Error Icon
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = KptTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = KptTheme.shapes.large,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MifosIcons.Error,
                        contentDescription = stringResource(Res.string.feature_send_interbank_failed),
                        modifier = Modifier.size(56.dp),
                        tint = KptTheme.colorScheme.error,
                    )
                }
            }

            // Title and Subtitle
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_transfer_failed),
                        style = KptTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_transaction_failed),
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Error Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = KptTheme.colorScheme.error.copy(alpha = 0.1f),
                    ),
                    shape = KptTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(KptTheme.spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = MifosIcons.Error,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = KptTheme.colorScheme.error,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = errorTitle,
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = KptTheme.colorScheme.error,
                            )
                            Text(
                                text = errorMessage,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = KptTheme.colorScheme.surface,
                    ),
                    shape = KptTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(KptTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                    ) {
                        // Amount
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_attempted_amount),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = attemptedAmount,
                                style = KptTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = KptTheme.colorScheme.primary,
                            )
                        }

                        // Available Balance
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_available_balance_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = availableBalance,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // From Account
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_from_account_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = fromAccount,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = fromAccountName,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // To Account
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_to_account_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = toAccount,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = toAccountNumber,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Date
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_transaction_date_label),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = transactionDate,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_description),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = description,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            // Transaction Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = KptTheme.colorScheme.surface,
                    ),
                    shape = KptTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(KptTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                    ) {
                        // Attempted Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Attempted Amount",
                                    style = KptTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = attemptedAmount,
                                    style = KptTheme.typography.bodySmall,
                                    color = KptTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Transaction Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Transaction Date",
                                    style = KptTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = transactionDate,
                                    style = KptTheme.typography.bodySmall,
                                    color = KptTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TransferSuccessScreenPreview() {
    MifosTheme {
        TransferSuccessScreen(
            recipientName = "Pedro Barreto",
            amount = "1.00",
            transactionReference = "TXN-20250911-0001TXN-20250911-0001TXN-20250911-0001",
            fromAccount = "WALLET - #0000000001",
            fromAccountName = "TOMAS ASCENCIO ASCENCIO",
            toAccount = "Pedro Barreto",
            toAccountNumber = "Account: 9388006020",
            transactionDate = "11/09/25 at 09:41 AM",
            currencyCode = "MXN",
            onBackToHome = {},
        )
    }
}

@Preview
@Composable
fun TransferFailedScreenPreview() {
    MifosTheme {
        TransferFailedScreen(
            errorMessage = "Your available balance is not enough to complete this transfer. Please update the amount and try again.",
            errorTitle = "Insufficient Balance",
            attemptedAmount = "MXN 1.00",
            availableBalance = "MXN 5,000.00",
            transactionDate = "11/09/25 at 09:41 AM",
            onRetry = {},
            onBackToHome = {},
        )
    }
}
