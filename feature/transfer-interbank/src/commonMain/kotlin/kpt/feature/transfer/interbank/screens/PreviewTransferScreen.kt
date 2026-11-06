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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.ui.AvatarBox
import kpt.core.ui.MifosProgressIndicatorOverlay
import mifos_pay.feature.transfer_interbank.generated.resources.Res
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_acknowledgement_section
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_amount
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_available_balance
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_confirm_pay
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_date
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_description
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_edit
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_from_account
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_i_acknowledged
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_preview_transfer
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_terms_acknowledged
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_terms_acknowledged_description
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_to_account
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transfer_amount
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_verified_recipient
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun PreviewTransferScreen(
    amount: String,
    transferDate: String,
    transferDescription: String,
    fromAccountName: String,
    fromAccountNo: String,
    fromAccountBalance: Double = 0.0,
    fromAccountType: String = "",
    recipientInfo: InterBankPartyInfoResponse?,
    isProcessing: Boolean,
    onEditClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onBackClick: () -> Unit,
    currencyCode: String = "MXN",
    modifier: Modifier = Modifier,
) {
    var disclaimerAccepted by remember { mutableStateOf(false) }
    MifosScaffold(
        modifier = modifier,
        topBar = {
            MifosTopBar(
                topBarTitle = stringResource(Res.string.feature_send_interbank_preview_transfer),
                backPress = onBackClick,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                MifosButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing && disclaimerAccepted,
                ) {
                    Text(stringResource(Res.string.feature_send_interbank_confirm_pay))
                }

                MifosButton(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing,
                ) {
                    Text(stringResource(Res.string.feature_send_interbank_edit))
                }
            }
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
        ) {
            // From Account Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_from_account),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    TransferPreviewCard(
                        name = fromAccountName,
                        accountNo = fromAccountNo,
                        accountType = fromAccountType,
                        balance = fromAccountBalance,
                        currencyCode = currencyCode,
                        icon = MifosIcons.Bank,
                    )
                }
            }

            // To Account Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_to_account),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    TransferPreviewCard(
                        name = "${recipientInfo?.firstName ?: ""} ${recipientInfo?.lastName ?: ""}".trim(),
                        accountNo = recipientInfo?.partyId ?: "N/A",
                        accountType = recipientInfo?.destinationFspId ?: "",
                        icon = MifosIcons.Person,
                        isVerified = recipientInfo?.executionStatus == true,
                    )
                }
            }

            // Amount
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_amount),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = KptTheme.colorScheme.primaryContainer,
                        ),
                        shape = KptTheme.shapes.medium,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(KptTheme.spacing.lg),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = currencyCode,
                                    style = KptTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KptTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text = amount,
                                    style = KptTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = KptTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Text(
                                text = stringResource(Res.string.feature_send_interbank_transfer_amount),
                                style = KptTheme.typography.labelSmall,
                                color = KptTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // Date
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_date),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    MifosCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = KptTheme.colorScheme.surface,
                        ),
                        shape = KptTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(KptTheme.spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = MifosIcons.CalenderMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = KptTheme.colorScheme.primary,
                                )
                                Text(
                                    text = transferDate.ifEmpty { "N/A" },
                                    style = KptTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = "Today",
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Description
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_send_interbank_description),
                            style = KptTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    MifosCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = KptTheme.colorScheme.surface,
                        ),
                        shape = KptTheme.shapes.medium,
                    ) {
                        Text(
                            text = transferDescription.ifEmpty { "N/A" },
                            style = KptTheme.typography.bodyMedium,
                            modifier = Modifier.padding(KptTheme.spacing.md),
                        )
                    }
                }
            }

            // Acknowledgement Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_acknowledgement_section),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    DisclaimerCheckboxCard(
                        isChecked = disclaimerAccepted,
                        onCheckedChange = { disclaimerAccepted = it },
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(vertical = KptTheme.spacing.lg))
            }
        }

        if (isProcessing) {
            MifosProgressIndicatorOverlay()
        }
    }
}

@Composable
private fun TransferPreviewCard(
    name: String,
    accountNo: String,
    icon: ImageVector,
    accountType: String = "",
    balance: Double = 0.0,
    currencyCode: String = "",
    isVerified: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = KptTheme.colorScheme.primary,
                shape = KptTheme.shapes.medium,
            ),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
        shape = KptTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(KptTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                AvatarBox(
                    icon = icon,
                    backgroundColor = KptTheme.colorScheme.primary,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = name,
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurface,
                    )
                    if (accountType.isNotEmpty()) {
                        Text(
                            text = accountType,
                            style = KptTheme.typography.bodySmall,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = accountNo,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isVerified) {
                        Text(
                            text = stringResource(Res.string.feature_send_interbank_verified_recipient),
                            style = KptTheme.typography.labelSmall,
                            color = KptTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (balance > 0.0 && currencyCode.isNotEmpty()) {
                Text(
                    text = stringResource(
                        Res.string.feature_send_interbank_available_balance,
                        CurrencyFormatter.format(balance, currencyCode, null),
                    ),
                    style = KptTheme.typography.labelMedium,
                    color = KptTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DisclaimerCheckboxCard(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isChecked) {
        KptTheme.colorScheme.primaryContainer
    } else {
        KptTheme.colorScheme.errorContainer
    }

    val textColor = if (isChecked) {
        KptTheme.colorScheme.onPrimaryContainer
    } else {
        KptTheme.colorScheme.onErrorContainer
    }

    val checkboxColor = if (isChecked) {
        KptTheme.colorScheme.primary
    } else {
        KptTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isChecked) }
                .padding(KptTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = checkboxColor,
                    uncheckedColor = checkboxColor,
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = if (isChecked) {
                        stringResource(Res.string.feature_send_interbank_terms_acknowledged)
                    } else {
                        stringResource(Res.string.feature_send_interbank_i_acknowledged)
                    },
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                )
                Text(
                    text = stringResource(Res.string.feature_send_interbank_terms_acknowledged_description),
                    style = KptTheme.typography.bodySmall,
                    color = textColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewTransferScreenPreview() {
    MifosTheme {
        val mockRecipient = InterBankPartyInfoResponse(
            sourceFspId = "mifos-bank-1",
            destinationFspId = "blackbank-test",
            requestId = "req-001",
            partyId = "9880000020",
            currencyCode = "MXN",
            firstName = "Pedro",
            lastName = "Barreto",
            systemMessage = "Success",
            executionStatus = true,
            partyIdType = "MSISDN",
        )

        PreviewTransferScreen(
            amount = "100.00",
            transferDate = "11/09/25",
            transferDescription = "Dinner share",
            fromAccountName = "ALEJANDRO ESCUTIA",
            fromAccountNo = "00000002",
            fromAccountBalance = 90760.0,
            fromAccountType = "Savings Account | Mexican Peso",
            recipientInfo = mockRecipient,
            isProcessing = false,
            onEditClick = {},
            onConfirmClick = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewTransferScreenProcessingPreview() {
    MifosTheme {
        val mockRecipient = InterBankPartyInfoResponse(
            sourceFspId = "mifos-bank-1",
            destinationFspId = "blackbank-test",
            requestId = "req-001",
            partyId = "9880000020",
            currencyCode = "MXN",
            firstName = "Pedro",
            lastName = "Barreto",
            systemMessage = "Success",
            executionStatus = true,
            partyIdType = "MSISDN",
        )

        PreviewTransferScreen(
            amount = "100.00",
            transferDate = "11/09/25",
            transferDescription = "Dinner share",
            fromAccountName = "ALEJANDRO ESCUTIA",
            fromAccountNo = "00000002",
            fromAccountBalance = 90760.0,
            fromAccountType = "Savings Account | Mexican Peso",
            recipientInfo = mockRecipient,
            isProcessing = true,
            onEditClick = {},
            onConfirmClick = {},
            onBackClick = {},
        )
    }
}
