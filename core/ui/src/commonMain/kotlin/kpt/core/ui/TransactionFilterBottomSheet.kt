/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosButton
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_account_balance
import kpt.core.ui.generated.resources.core_ui_account_number
import kpt.core.ui.generated.resources.core_ui_all
import kpt.core.ui.generated.resources.core_ui_credits
import kpt.core.ui.generated.resources.core_ui_debits
import kpt.core.ui.generated.resources.core_ui_filter_apply
import kpt.core.ui.generated.resources.core_ui_filter_by_account
import kpt.core.ui.generated.resources.core_ui_filter_clear_all
import kpt.core.ui.generated.resources.core_ui_filter_title
import kpt.core.ui.generated.resources.core_ui_filter_transaction_type
import kpt.core.ui.generated.resources.core_ui_select_account
import org.jetbrains.compose.resources.stringResource
/** Bottom sheet for filtering transaction history by account and transaction type. */
@Composable
fun TransactionFilterBottomSheet(
    modifier: Modifier = Modifier,
    selectedAccount: Account?,
    accounts: List<Account>,
    selectedTransactionType: TransactionType,
    onAccountSelected: (Account) -> Unit,
    onTransactionTypeSelected: (TransactionType) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit,
    onDismiss: () -> Unit,
) {
    MifosBottomSheet(
        modifier = modifier,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.core_ui_filter_title),
                    style = KptTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = KptTheme.spacing.md),
                )
                Text(
                    text = stringResource(Res.string.core_ui_filter_clear_all),
                    color = KptTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onClearFilters() },
                )
            }

            Text(
                text = stringResource(Res.string.core_ui_filter_by_account),
                style = KptTheme.typography.titleSmall,
            )
            FilterAccountDropDown(
                selectedAccount = selectedAccount,
                accounts = accounts,
                onAccountSelected = onAccountSelected,
            )

            Text(
                text = stringResource(Res.string.core_ui_filter_transaction_type),
                style = KptTheme.typography.titleSmall,
            )
            TransactionTypeFilter(
                selectedTransactionType = selectedTransactionType,
                onTransactionTypeSelected = onTransactionTypeSelected,
                modifier = modifier.padding(top = KptTheme.spacing.sm),
            )

            Spacer(Modifier.height(KptTheme.spacing.lg))

            MifosButton(
                onClick = onApplyFilters,
                text = { Text(text = stringResource(Res.string.core_ui_filter_apply)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Row of chips for selecting the transaction type filter. */
@Composable
fun TransactionTypeFilter(
    selectedTransactionType: TransactionType,
    onTransactionTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        TransactionType.entries.forEach { transactionType ->
            FilterItem(
                transactionType = transactionType,
                isSelected = transactionType == selectedTransactionType,
                onSelected = onTransactionTypeSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FilterItem(
    transactionType: TransactionType,
    isSelected: Boolean,
    onSelected: (TransactionType) -> Unit,
    selectedColor: Color = KptTheme.colorScheme.primary,
    unSelectedColor: Color = KptTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSelected) selectedColor else unSelectedColor
    val contentColor = if (isSelected) unSelectedColor else selectedColor

    Surface(
        modifier = modifier,
        shape = CircleShape,
        contentColor = contentColor,
        color = containerColor,
        onClick = { onSelected(transactionType) },
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = ButtonDefaults.MinWidth,
                    minHeight = ButtonDefaults.MinHeight,
                )
                .padding(horizontal = KptTheme.spacing.lg, vertical = KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = when (transactionType) {
                    TransactionType.OTHER -> stringResource(Res.string.core_ui_all)
                    TransactionType.DEBIT -> stringResource(Res.string.core_ui_debits)
                    TransactionType.CREDIT -> stringResource(Res.string.core_ui_credits)
                },
                style = KptTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Dropdown for selecting which account to filter transactions by. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterAccountDropDown(
    selectedAccount: Account?,
    accounts: List<Account>,
    modifier: Modifier = Modifier,
    onAccountSelected: (Account) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var parentSize by remember { mutableStateOf(Size.Zero) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { parentSize = it.size.toSize() }
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = KptTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (selectedAccount != null) {
                Column {
                    Text(
                        text = stringResource(Res.string.core_ui_account_number, selectedAccount.number),
                        style = KptTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.core_ui_account_balance, selectedAccount.balance, selectedAccount.currency.code),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.core_ui_select_account),
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { parentSize.width.toDp() })
                .heightIn(max = 300.dp),
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = stringResource(Res.string.core_ui_account_number, account.number),
                                style = KptTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                            Text(
                                text = stringResource(Res.string.core_ui_account_balance, account.balance, account.currency.code),
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
