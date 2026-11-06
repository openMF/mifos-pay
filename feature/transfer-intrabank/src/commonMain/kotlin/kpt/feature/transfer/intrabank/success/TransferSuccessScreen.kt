/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.success

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.transfer.intrabank.confirm.TransferResult
import mifos_pay.feature.transfer_intrabank.generated.resources.Res
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_make_transfer_payment_success
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_make_transfer_success
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_amount
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_back_to_home
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_copy
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_date
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_description
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_from_account
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_to_account
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_transaction_id
import mifos_pay.feature.transfer_intrabank.generated.resources.feature_transfer_transfer_completed
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TransferSuccessScreen(
    transferResult: TransferResult,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    BackHandler {
        navigateBack()
    }

    MifosScaffold(
        modifier = modifier,
        bottomBar = {
            MifosButton(
                onClick = navigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
            ) {
                Text(text = stringResource(Res.string.feature_transfer_back_to_home))
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
            // Success Icon
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = KptTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = KptTheme.shapes.large,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MifosIcons.Check,
                        contentDescription = stringResource(Res.string.feature_make_transfer_success),
                        modifier = Modifier.size(56.dp),
                        tint = KptTheme.colorScheme.primary,
                    )
                }
            }

            // Title and Subtitle
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_make_transfer_payment_success),
                        style = KptTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(
                            Res.string.feature_transfer_transfer_completed,
                            transferResult.toAccountName,
                        ),
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Transaction Reference Card (only show if transaction ID is available)
            if (transferResult.transactionId.isNotEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_transfer_transaction_id),
                                style = KptTheme.typography.labelMedium,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = KptTheme.spacing.xs),
                                    text = transferResult.transactionId,
                                    style = KptTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KptTheme.colorScheme.primary,
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString(transferResult.transactionId),
                                        )
                                        copied = true
                                    },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        imageVector = MifosIcons.Copy,
                                        contentDescription = stringResource(
                                            Res.string.feature_transfer_copy,
                                        ),
                                        modifier = Modifier.size(20.dp),
                                        tint = KptTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transfer Details Card
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
                        TransferDetailRow(
                            label = stringResource(Res.string.feature_transfer_amount),
                            value = "$${transferResult.amount}",
                            isHighlighted = true,
                        )

                        HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)

                        // To Account
                        TransferDetailRow(
                            label = stringResource(Res.string.feature_transfer_to_account),
                            value = "${transferResult.toAccountName}\n${transferResult.toAccountNo}",
                        )

                        HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)

                        // From Account
                        TransferDetailRow(
                            label = stringResource(Res.string.feature_transfer_from_account),
                            value = "${transferResult.fromAccountName}\n${transferResult.fromAccountNo}",
                        )

                        HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)

                        // Date
                        TransferDetailRow(
                            label = stringResource(Res.string.feature_transfer_date),
                            value = transferResult.transferDate,
                        )

                        // Description (only show if not empty)
                        if (transferResult.description.isNotBlank()) {
                            HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)

                            TransferDetailRow(
                                label = stringResource(Res.string.feature_transfer_description),
                                value = transferResult.description,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferDetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = KptTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (isHighlighted) {
                KptTheme.typography.headlineSmall
            } else {
                KptTheme.typography.bodyMedium
            },
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) {
                KptTheme.colorScheme.primary
            } else {
                KptTheme.colorScheme.onSurface
            },
        )
    }
}

@Preview
@Composable
private fun TransferSuccessScreenPreview() {
    KptTheme {
        TransferSuccessScreen(
            transferResult = TransferResult(
                transactionId = "TXN-20250217-001",
                amount = 250.0,
                fromAccountNo = "987654321",
                fromAccountName = "John Doe",
                toAccountNo = "123456789",
                toAccountName = "Jane Smith",
                transferDate = "17 Feb 2025",
                description = "Payment for groceries",
            ),
            navigateBack = {},
        )
    }
}
