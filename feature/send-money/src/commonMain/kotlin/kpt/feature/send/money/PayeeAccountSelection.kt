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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.send.money.generated.resources.Res
import kpt.feature.send.money.generated.resources.feature_send_money_add_bank_account
import kpt.feature.send.money.generated.resources.feature_send_money_add_bank_account_desc
import kpt.feature.send.money.generated.resources.feature_send_money_balance
import kpt.feature.send.money.generated.resources.feature_send_money_bank_icon
import kpt.feature.send.money.generated.resources.feature_send_money_change_account
import kpt.feature.send.money.generated.resources.feature_send_money_check_now
import kpt.feature.send.money.generated.resources.feature_send_money_choose_account
import kpt.feature.send.money.generated.resources.feature_send_money_pay_amount
import kpt.feature.send.money.generated.resources.feature_send_money_selected
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun AccountSelectionBottomSheet(
    state: PayeeDetailsState,
    onAccountSelected: (BankAccount) -> Unit,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dummyAccounts = listOf(
        BankAccount(
            id = "1",
            bankName = "State Bank of India",
            accountNumber = "****1234",
            isDefault = true,
            accountType = "",
        ),
        BankAccount(
            id = "2",
            bankName = "HDFC Bank",
            accountNumber = "****5678",
            isDefault = false,
            accountType = "",
        ),
    )

    MifosBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(KptTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.feature_send_money_choose_account),
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = KptTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    dummyAccounts.forEach { account ->
                        AccountItem(
                            account = account,
                            isSelected = state.selectedAccount?.id == account.id,
                            onAccountClick = { onAccountSelected(account) },
                        )
                    }

                    AddBankAccountItem()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
            ) {
                val focusManager = LocalFocusManager.current
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onConfirmPayment()
                    },
                    enabled = state.selectedAccount != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedAccount != null) {
                            KptTheme.colorScheme.primary
                        } else {
                            KptTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (state.selectedAccount != null) {
                            KptTheme.colorScheme.onPrimary
                        } else {
                            KptTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                    shape = RoundedCornerShape(KptTheme.spacing.sm),
                    contentPadding = PaddingValues(horizontal = KptTheme.spacing.lg),
                ) {
                    Text(
                        text = stringResource(
                            Res.string.feature_send_money_pay_amount,
                            AmountUtils.formatPaiseForUI(state.amount),
                        ),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AccountItem(
    account: BankAccount,
    isSelected: Boolean,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAccountClick() },
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Bank,
                contentDescription = stringResource(Res.string.feature_send_money_bank_icon),
                modifier = Modifier.size(32.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = account.bankName,
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = account.accountNumber,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                // TODO implement check now for balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_money_balance),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(Res.string.feature_send_money_check_now),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.primary,
                        modifier = Modifier.clickable { },
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = MifosIcons.CheckCircle,
                    contentDescription = stringResource(Res.string.feature_send_money_selected),
                    modifier = Modifier.size(24.dp),
                    tint = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
internal fun SelectedAccountSection(
    account: BankAccount,
    onChangeAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg)
                .clickable { onChangeAccount() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Bank,
                contentDescription = stringResource(Res.string.feature_send_money_bank_icon),
                modifier = Modifier.size(32.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = account.bankName,
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = account.accountNumber,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_money_balance),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(Res.string.feature_send_money_check_now),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.primary,
                        modifier = Modifier.clickable { },
                    )
                }
            }

            Icon(
                imageVector = MifosIcons.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.feature_send_money_change_account),
                modifier = Modifier.size(24.dp),
                tint = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun AddBankAccountItem(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Add,
                contentDescription = stringResource(Res.string.feature_send_money_add_bank_account_desc),
                modifier = Modifier.size(24.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            Text(
                text = stringResource(Res.string.feature_send_money_add_bank_account),
                style = KptTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.primary,
            )
        }
    }
}

/** Preview of the account selection bottom sheet. */
@Preview
@Composable
fun AccountSelectionBottomSheetPreview() {
    val state = PayeeDetailsState(
        payeeName = "John Doe",
        upiId = "john.doe@upi",
        phoneNumber = "1234567890",
        amount = "100.00",
        note = "Test payment",
        isAmountEditable = true,
        isUpiCode = true,
        isLoading = false,
        showMaxAmountMessage = false,
        hasNoteFieldBeenFocused = false,
        showAccountSelectionSheet = false,
        selectedAccount = null,
    )
    AccountSelectionBottomSheet(
        state = state,
        onAccountSelected = {},
        onDismiss = {},
        onConfirmPayment = {},
        modifier = Modifier,
    )
}

/** Preview of a single account item. */
@Preview
@Composable
fun AccountItemPreview() {
    val account = BankAccount(
        id = "1",
        bankName = "State Bank of India",
        accountNumber = "****1234",
        isDefault = true,
        accountType = "",

    )
    AccountItem(
        account = account,
        isSelected = true,
        onAccountClick = {},
        modifier = Modifier,
    )
}

/** Preview of the selected account section. */
@Preview
@Composable
fun SelectedAccountSectionPreview() {
    val account = BankAccount(
        id = "1",
        bankName = "State Bank of India",
        accountNumber = "****1234",
        isDefault = true,
        accountType = "",
    )
    SelectedAccountSection(
        account = account,
        onChangeAccount = {},
        modifier = Modifier,
    )
}

/** Preview of the add-bank-account item. */
@Preview
@Composable
fun AddBankAccountItemPreview() {
    AddBankAccountItem(modifier = Modifier)
}
