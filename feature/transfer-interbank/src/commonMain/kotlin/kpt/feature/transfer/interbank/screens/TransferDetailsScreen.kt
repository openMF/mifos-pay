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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.account.Account
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Status
import kpt.core.ui.AmountEditText
import mifos_pay.feature.transfer_interbank.generated.resources.Res
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_amount
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_available_balance
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_continue
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_date
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_description
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_edit
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_from_account
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_to_account
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_to_account_interbank
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_transfer_details
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_verified
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDetailsScreen(
    fromAccount: Account?,
    recipient: InterBankPartyInfoResponse?,
    amount: String,
    onAmountChanged: (String) -> Unit,
    date: String,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditFromAccount: () -> Unit = {},
    onEditRecipient: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        topBar = {
            MifosTopBar(
                topBarTitle = stringResource(Res.string.feature_send_interbank_transfer_details),
                backPress = onBackClick,
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                MifosButton(
                    onClick = onContinueClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = amount.isNotEmpty() &&
                        amount.toDoubleOrNull()?.let {
                            it <= (fromAccount?.balance ?: 0.0)
                        } ?: false &&
                        description.isNotEmpty(),
                ) {
                    Text(stringResource(Res.string.feature_send_interbank_continue))
                }
            }
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(KptTheme.spacing.md)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            // From Account Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_from_account),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    AccountDetailCard(
                        account = fromAccount,
                        showVerified = false,
                        onEditClick = onEditFromAccount,
                    )
                }
            }

            // To Account Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_to_account),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    RecipientDetailCard(
                        recipient = recipient,
                        onEditClick = onEditRecipient,
                    )
                }
            }

            // Amount Input
            item {
                var amountLocal by remember { mutableStateOf(amount) }
                var errorMsg by remember { mutableStateOf<String?>(null) }
                var isError by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_amount),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    AmountEditText(
                        value = amountLocal,
                        onValueChange = { doubleAmount ->
                            onAmountChanged.invoke(doubleAmount.toString())
                            amountLocal = doubleAmount.toString()
                        },
                        currencyCode = fromAccount?.currency?.code ?: "MXN",
                        availableBalance = fromAccount?.balance ?: 0.0,
                        maxAmount = fromAccount?.balance ?: 0.0,
                        errorMessage = errorMsg,
                        onAmountValidation = { _, error ->
                            errorMsg = error ?: ""
                            isError = error != null
                        },
                        isError = isError,
                    )
                }
            }

            // Date Input with Picker
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_date),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    MifosCard(
                        colors = CardDefaults.cardColors(KptTheme.colorScheme.background),
                        shape = KptTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(KptTheme.spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                            ) {
                                Text(
                                    text = date,
                                    style = KptTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = stringResource(Res.string.feature_send_interbank_edit),
                                tint = KptTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Description Input
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_description),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    MifosTextField(
                        label = "",
                        value = description,
                        onValueChange = onDescriptionChanged,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(vertical = KptTheme.spacing.md))
            }
        }
    }
}

@Composable
private fun AccountDetailCard(
    account: Account?,
    modifier: Modifier = Modifier,
    showVerified: Boolean = false,
    onEditClick: () -> Unit = {},
) {
    MifosCard(
        colors = CardDefaults.cardColors(KptTheme.colorScheme.background),
        shape = KptTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = account?.clientName ?: "",
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${account?.name ?: ""} - #${account?.number ?: ""}",
                    style = KptTheme.typography.bodySmall,
                )
                Text(
                    text = "${account?.accountType?.value ?: ""} | ${account?.currency?.name ?: ""}",
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        Res.string.feature_send_interbank_available_balance,
                        CurrencyFormatter.format(
                            account?.balance ?: 0.0,
                            account?.currency?.code ?: "",
                            null,
                        ),
                    ),
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.feature_send_interbank_edit),
                    tint = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RecipientDetailCard(
    recipient: InterBankPartyInfoResponse?,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
) {
    MifosCard(
        colors = CardDefaults.cardColors(KptTheme.colorScheme.background),
        shape = KptTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = "${recipient?.firstName ?: ""} ${recipient?.lastName ?: ""}".trim(),
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${recipient?.destinationFspId ?: ""} | ${recipient?.partyIdType ?: ""}",
                    style = KptTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(
                        Res.string.feature_send_interbank_to_account_interbank,
                        recipient?.partyId ?: "",
                    ),
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                if (recipient?.executionStatus == true) {
                    Row(
                        modifier = Modifier.padding(top = KptTheme.spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "✓",
                            style = KptTheme.typography.bodySmall,
                            color = KptTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(Res.string.feature_send_interbank_verified),
                            style = KptTheme.typography.bodySmall,
                            color = KptTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.feature_send_interbank_edit),
                    tint = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview
@Composable
fun TransferDetailsScreenPreview() {
    MifosTheme {
        val mockFromAccount = Account(
            name = "WALLET",
            number = "00000002",
            balance = 5000.0,
            id = 101L,
            productId = 202L,
            clientName = "ALEJANDRO ESCUTIA",
            accountType = kpt.core.model.savingsaccount.AccountType(
                id = 1L,
                code = "savingsAccountType.savings",
                value = "Savings Account",
            ),
            currency = Currency(
                code = "MXN",
                name = "Mexican Peso",
                displaySymbol = "MX$",
                displayLabel = "Mexican Peso (MX$)",
                decimalPlaces = 2,
                inMultiplesOf = 10,
                nameCode = "MXN",
            ),
            status = Status(
                id = 300,
                code = "savingsAccountStatusType.active",
                value = "Active",
                submittedAndPendingApproval = false,
                approved = true,
                rejected = false,
                withdrawnByApplicant = false,
                active = true,
                closed = false,
                prematureClosed = false,
                transferInProgress = false,
                transferOnHold = false,
                matured = false,
            ),
            image = "",
        )

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

        TransferDetailsScreen(
            fromAccount = mockFromAccount,
            recipient = mockRecipient,
            amount = "100.00",
            onAmountChanged = {},
            date = "11/09/25",
            description = "Dinner share",
            onDescriptionChanged = {},
            onContinueClick = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
fun TransferDetailsScreenEmptyPreview() {
    MifosTheme {
        val mockFromAccount = Account(
            name = "WALLET",
            number = "00000002",
            balance = 5000.0,
            id = 101L,
            productId = 202L,
            clientName = "ALEJANDRO ESCUTIA",
            accountType = kpt.core.model.savingsaccount.AccountType(
                id = 1L,
                code = "savingsAccountType.savings",
                value = "Savings Account",
            ),
            currency = Currency(
                code = "MXN",
                name = "Mexican Peso",
                displaySymbol = "MX$",
                displayLabel = "Mexican Peso (MX$)",
                decimalPlaces = 2,
                inMultiplesOf = 10,
                nameCode = "MXN",
            ),
            status = Status(
                id = 300,
                code = "savingsAccountStatusType.active",
                value = "Active",
                submittedAndPendingApproval = false,
                approved = true,
                rejected = false,
                withdrawnByApplicant = false,
                active = true,
                closed = false,
                prematureClosed = false,
                transferInProgress = false,
                transferOnHold = false,
                matured = false,
            ),
            image = "",
        )

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

        TransferDetailsScreen(
            fromAccount = mockFromAccount,
            recipient = mockRecipient,
            amount = "",
            onAmountChanged = {},
            date = "",
            description = "",
            onDescriptionChanged = {},
            onContinueClick = {},
            onBackClick = {},
        )
    }
}
