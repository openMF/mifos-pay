/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.merchants.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosProgressIndicator
import kpt.feature.merchants.MerchantTransferUiState
import kpt.feature.merchants.MerchantTransferViewModel
import mifos_pay.feature.merchants.generated.resources.Res
import mifos_pay.feature.merchants.generated.resources.feature_merchants_amount
import mifos_pay.feature.merchants.generated.resources.feature_merchants_credits
import mifos_pay.feature.merchants.generated.resources.feature_merchants_debits
import mifos_pay.feature.merchants.generated.resources.feature_merchants_error_oops
import mifos_pay.feature.merchants.generated.resources.feature_merchants_merchant_transaction
import mifos_pay.feature.merchants.generated.resources.feature_merchants_no_transactions_found
import mifos_pay.feature.merchants.generated.resources.feature_merchants_other
import mifos_pay.feature.merchants.generated.resources.feature_merchants_submit
import mifos_pay.feature.merchants.generated.resources.feature_merchants_transaction_date
import mifos_pay.feature.merchants.generated.resources.feature_merchants_transaction_id
import mifos_pay.feature.merchants.generated.resources.feature_merchants_transfer_money_to_this_merchant
import mifos_pay.feature.merchants.generated.resources.feature_merchants_unexpected_error_subtitle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun MerchantTransferScreenRoute(
    onBackPressed: () -> Unit,
    proceedWithMakeTransferFlow: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MerchantTransferViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MerchantTransferScreen(
        uiState = uiState,
        merchantName = "New User",
        merchantVPA = "Sample VPA",
        onBackPressed = onBackPressed,
        checkBalanceAvailability = { vpa, transferAmount -> },
        modifier = modifier,
    )
}

@Composable
@VisibleForTesting
internal fun MerchantTransferScreen(
    uiState: MerchantTransferUiState,
    merchantName: String,
    merchantVPA: String,
    onBackPressed: () -> Unit,
    checkBalanceAvailability: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBottomSheet by remember { mutableStateOf(true) }
    var amount by rememberSaveable { mutableStateOf("") }

    MifosScaffold(
        modifier = modifier,
        topBarTitle = stringResource(Res.string.feature_merchants_merchant_transaction),
        backPress = onBackPressed,
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                when (uiState) {
                    is MerchantTransferUiState.Loading -> MifosProgressIndicator()

                    is MerchantTransferUiState.Error -> {
                        EmptyContentScreen(
                            modifier = Modifier,
                            title = stringResource(Res.string.feature_merchants_error_oops),
                            subTitle = stringResource(Res.string.feature_merchants_unexpected_error_subtitle),
                            iconTint = KptTheme.colorScheme.error,
                        )
                    }

                    is MerchantTransferUiState.Empty -> {
                        EmptyContentScreen(
                            title = stringResource(Res.string.feature_merchants_error_oops),
                            subTitle = stringResource(Res.string.feature_merchants_no_transactions_found),
                            modifier = Modifier,
                        )
                    }

                    is MerchantTransferUiState.Success -> {
                        TransactionList(uiState.transactionsList)
                    }

                    is MerchantTransferUiState.InsufficientBalance -> {}
                }

                if (showBottomSheet) {
                    MerchantBottomSheet(
                        merchantName = merchantName,
                        merchantVPA = merchantVPA,
                        amount = amount,
                        onAmountChange = { amount = it },
                        checkBalanceAvailability = checkBalanceAvailability,
                        onDismiss = { showBottomSheet = false },
                    )
                }
            }
        },
    )
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        items(
            items = transactions,
            key = { it.transactionId },
        ) { transaction ->
            SpecificTransactionItem(transaction)
            HorizontalDivider()
        }
    }
}

@Composable
private fun MerchantBottomSheet(
    merchantName: String,
    merchantVPA: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    checkBalanceAvailability: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosBottomSheet(
        content = {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.feature_merchants_transfer_money_to_this_merchant),
                    color = KptTheme.colorScheme.tertiaryContainer.copy(
                        red = 0.38f,
                        green = 0f,
                        blue = 0.93f,
                    ),
                    style = KptTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))

                MerchantInfo(
                    merchantName,
                    merchantVPA,
                )

                Spacer(modifier = Modifier.height(24.dp))
                MifosOutlinedTextField(
                    value = amount,
                    label = stringResource(Res.string.feature_merchants_amount),
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(modifier = Modifier.height(24.dp))
                MifosButton(
                    onClick = {
                        checkBalanceAvailability(
                            merchantName,
                            merchantVPA,
                        )
                    },
                    colors = ButtonDefaults.buttonColors(KptTheme.colorScheme.primary),
                    modifier = Modifier.width(155.dp),
                ) {
                    Text(
                        stringResource(Res.string.feature_merchants_submit),
                        color = KptTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
private fun MerchantInfo(
    merchantName: String,
    merchantVPA: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        modifier = modifier,
    ) {
        MerchantInitialAvatar(merchantName)

        Text(
            text = merchantName,
            style = KptTheme.typography.labelMedium,
        )

        Text(
            text = merchantVPA,
            style = KptTheme.typography.labelMedium,
            color = KptTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun MerchantInitialAvatar(
    merchantName: String,
    modifier: Modifier = Modifier,
) {
    val initial = merchantName.take(1).uppercase()

    Box(
        modifier =
        modifier
            .size(86.dp)
            .background(
                color = KptTheme.colorScheme.primary,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = KptTheme.colorScheme.onPrimary,
            fontSize = 44.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SpecificTransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = KptTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpecificTransactionAccountInfo(
                amount = transaction.amount.toString(),
                accountNo = transaction.accountNo,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = MifosIcons.SendRightTilted, contentDescription = null)
            SpecificTransactionAccountInfo(
                amount = transaction.amount.toString(),
                accountNo = transaction.accountNo,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.feature_merchants_transaction_id) + transaction.transactionId,
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(Res.string.feature_merchants_transaction_date) + transaction.date,
                    style = KptTheme.typography.bodyLarge,
                )
                Text(
                    text =
                    when (transaction.transactionType) {
                        TransactionType.DEBIT -> stringResource(Res.string.feature_merchants_debits)
                        TransactionType.CREDIT -> stringResource(Res.string.feature_merchants_credits)
                        TransactionType.OTHER -> stringResource(Res.string.feature_merchants_other)
                    },
                    style = KptTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${transaction.currency.code}${transaction.amount}",
                style = KptTheme.typography.displaySmall,
                color =
                when (transaction.transactionType) {
                    TransactionType.DEBIT -> KptTheme.colorScheme.error.copy(
                        red = 0.8f,
                        green = 0f,
                        blue = 0f,
                    )

                    TransactionType.CREDIT -> KptTheme.colorScheme.onTertiaryContainer.copy(
                        red = 0f,
                        green = 0.51f,
                        blue = 0.21f,
                    )

                    TransactionType.OTHER -> KptTheme.colorScheme.primaryContainer.copy(
                        red = 1f,
                        green = 1f,
                        blue = 0f,
                    )
                },
            )
        }
    }
}

@Composable
private fun SpecificTransactionAccountInfo(
    amount: String,
    accountNo: String,
    modifier: Modifier = Modifier,
    accountClicked: (String) -> Unit = {},
) {
    Column(
        modifier =
        modifier.clickable {
            accountClicked(accountNo)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = MifosIcons.AccountCircle, contentDescription = null)
        Text(
            text = accountNo,
            style = KptTheme.typography.titleSmall,
        )
        Text(
            text = amount,
            style = KptTheme.typography.bodyMedium,
        )
    }
}

internal class MerchantTransferUiStateProvider : PreviewParameterProvider<MerchantTransferUiState> {
    override val values: Sequence<MerchantTransferUiState>
        get() =
            sequenceOf(
                MerchantTransferUiState.Success(arrayListOf()),
                MerchantTransferUiState.Error,
                MerchantTransferUiState.Loading,
                MerchantTransferUiState.Empty,
                MerchantTransferUiState.InsufficientBalance,
            )
}

@Preview
@Composable
private fun Preview(
    @PreviewParameter(MerchantTransferUiStateProvider::class)
    uiState: MerchantTransferUiState,
) {
    MifosTheme {
        MerchantTransferScreen(
            uiState = uiState,
            merchantName = "New User",
            merchantVPA = "naman.dwivedi2@mifos",
            onBackPressed = {},
            checkBalanceAvailability = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun TransactionItemPreview() {
    MifosTheme {
        SpecificTransactionItem(
            transaction = Transaction(
                accountId = 2447,
                amount = 28.29,
                date = "pellentesque",
                currency = Currency(
                    code = "persecuti",
                    name = "Bradford Davidson",
                    decimalPlaces = 9112,
                    inMultiplesOf = 1440,
                    displaySymbol = "audire",
                    nameCode = "Alison Bowers",
                    displayLabel = "prodesset",
                ),
                transactionType = TransactionType.OTHER,
                transactionId = 6078,
                accountNo = "nascetur",
                transferId = null,
                originalTransactionId = 8388,
                paymentDetailId = null,
                reversed = true,
            ),
        )
    }
}

@Preview
@Composable
private fun MerchantBottomSheetPreview() {
    MifosTheme {
        MerchantBottomSheet(
            merchantName = "Naman Dwivedi 2",
            merchantVPA = "naman.dwivedi2@mifos",
            amount = "",
            onAmountChange = {},
            checkBalanceAvailability = { _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun MerchantInitialAvatarPreview() {
    MifosTheme {
        MerchantInitialAvatar("Naman")
    }
}
