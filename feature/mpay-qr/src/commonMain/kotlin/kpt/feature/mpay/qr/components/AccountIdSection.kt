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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_account_ids
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_account_number
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_copy
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_external_id
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Section showing copyable account identifiers:
 * - Section header "Account IDs"
 * - Account number row with copy button
 * - External ID row (if available) with copy button
 * - Card-style rows with label + value
 */
@Composable
internal fun AccountIdSection(
    accountNumber: String,
    externalId: String?,
    onCopyAccountNumber: () -> Unit,
    onCopyExternalId: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.feature_mpay_qr_account_ids),
            style = KptTheme.typography.titleSmall,
            color = KptTheme.colorScheme.onSurface,
        )

        AccountIdRow(
            label = stringResource(Res.string.feature_mpay_qr_account_number),
            value = maskAccountNumber(accountNumber),
            onCopy = onCopyAccountNumber,
        )

        if (!externalId.isNullOrBlank()) {
            AccountIdRow(
                label = stringResource(Res.string.feature_mpay_qr_external_id),
                value = externalId,
                onCopy = onCopyExternalId,
            )
        }
    }
}

@Composable
private fun AccountIdRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceVariant,
        ),
        shape = KptTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = KptTheme.spacing.md,
                    end = KptTheme.spacing.xs,
                    top = KptTheme.spacing.sm,
                    bottom = KptTheme.spacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = label,
                    style = KptTheme.typography.labelSmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = ForkIcons.Copy,
                    contentDescription = stringResource(Res.string.feature_mpay_qr_copy),
                    tint = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Masks the account number to show only the last 4 digits.
 * Example: "1234567890" -> "●●●● 7890"
 */
private fun maskAccountNumber(accountNo: String): String {
    if (accountNo.length <= 4) return accountNo
    val lastFour = accountNo.takeLast(4)
    return "●●●● $lastFour"
}

@Preview
@Composable
private fun AccountIdSectionPreview() {
    KptMaterialTheme {
        AccountIdSection(
            accountNumber = "1234567890",
            externalId = "EXT-12345",
            onCopyAccountNumber = {},
            onCopyExternalId = {},
        )
    }
}

@Preview
@Composable
private fun AccountIdSectionNoExternalIdPreview() {
    KptMaterialTheme {
        AccountIdSection(
            accountNumber = "1234567890",
            externalId = null,
            onCopyAccountNumber = {},
            onCopyExternalId = {},
        )
    }
}
