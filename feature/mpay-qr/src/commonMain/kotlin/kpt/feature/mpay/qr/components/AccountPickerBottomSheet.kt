/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Status
import mifos_pay.feature.mpay_qr.generated.resources.Res
import mifos_pay.feature.mpay_qr.generated.resources.feature_mpay_qr_has_external_id
import mifos_pay.feature.mpay_qr.generated.resources.feature_mpay_qr_no_external_id
import mifos_pay.feature.mpay_qr.generated.resources.feature_mpay_qr_select_account
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Bottom sheet for selecting an account for QR code generation.
 * Shows all available accounts with indicators for external ID availability.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountPickerBottomSheet(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = KptTheme.colorScheme.surface,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_mpay_qr_select_account),
                style = KptTheme.typography.titleMedium,
                color = KptTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.md))

            accounts.forEachIndexed { index, account ->
                AccountPickerItem(
                    account = account,
                    isSelected = account.id == selectedAccount?.id,
                    hasExternalId = !account.externalId.isNullOrBlank(),
                    onClick = { onAccountSelected(account) },
                )

                if (index < accounts.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = KptTheme.spacing.xs),
                        color = KptTheme.colorScheme.outlineVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
        }
    }
}

@Composable
private fun AccountPickerItem(
    account: Account,
    isSelected: Boolean,
    hasExternalId: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.productName ?: account.name,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurface,
            )
            Text(
                text = formatAccountDisplay(account.number),
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }

        // External ID indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            if (hasExternalId) {
                Icon(
                    imageVector = MifosIcons.Check,
                    contentDescription = stringResource(Res.string.feature_mpay_qr_has_external_id),
                    modifier = Modifier.size(KptTheme.spacing.md),
                    tint = KptTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = MifosIcons.Info,
                    contentDescription = stringResource(Res.string.feature_mpay_qr_no_external_id),
                    modifier = Modifier.size(KptTheme.spacing.md),
                    tint = KptTheme.colorScheme.error,
                )
            }
        }
    }
}

/**
 * Formats account number for display with masking.
 * Example: "1234567890" -> "●●●● ●●●● 7890"
 */
private fun formatAccountDisplay(accountNo: String): String {
    if (accountNo.length <= 4) return accountNo
    val lastFour = accountNo.takeLast(4)
    return "●●●● ●●●● $lastFour"
}

@Preview
@Composable
private fun AccountPickerItemPreview() {
    KptMaterialTheme {
        Column {
            AccountPickerItem(
                account = Account(
                    id = 1,
                    name = "Savings Account",
                    number = "1234567890",
                    balance = 1000.0,
                    externalId = "EXT-123",
                    productName = "Savings Account",
                    currency = Currency(
                        code = "USD",
                        name = "US Dollar",
                        decimalPlaces = 2,
                        displaySymbol = "$",
                        nameCode = "currency.USD",
                        displayLabel = "US Dollar ($)",
                    ),
                    status = Status(
                        id = 300,
                        code = "active",
                        value = "Active",
                        submittedAndPendingApproval = false,
                        approved = false,
                        rejected = false,
                        withdrawnByApplicant = false,
                        active = true,
                        closed = false,
                        prematureClosed = false,
                        transferInProgress = false,
                        transferOnHold = false,
                        matured = false,
                    ),
                ),
                isSelected = true,
                hasExternalId = true,
                onClick = {},
            )

            HorizontalDivider()

            AccountPickerItem(
                account = Account(
                    id = 2,
                    name = "Current Account",
                    number = "0987654321",
                    balance = 500.0,
                    externalId = null,
                    productName = "Current Account",
                    currency = Currency(
                        code = "USD",
                        name = "US Dollar",
                        decimalPlaces = 2,
                        displaySymbol = "$",
                        nameCode = "currency.USD",
                        displayLabel = "US Dollar ($)",
                    ),
                    status = Status(
                        id = 300,
                        code = "active",
                        value = "Active",
                        submittedAndPendingApproval = false,
                        approved = false,
                        rejected = false,
                        withdrawnByApplicant = false,
                        active = true,
                        closed = false,
                        prematureClosed = false,
                        transferInProgress = false,
                        transferOnHold = false,
                        matured = false,
                    ),
                ),
                isSelected = false,
                hasExternalId = false,
                onClick = {},
            )
        }
    }
}
