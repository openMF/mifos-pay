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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import mifos_pay.feature.mpay_qr.generated.resources.Res
import mifos_pay.feature.mpay_qr.generated.resources.feature_mpay_qr_primary
import mifos_pay.feature.mpay_qr.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Professional account selector card displaying:
 * - Mifos logo with branded styling
 * - Client display name prominently
 * - Office/bank name
 * - Masked account number
 * - "Primary" badge with checkmark
 * - Dropdown indicator when multiple accounts available
 */
@Composable
internal fun AccountSelectorCard(
    client: Client,
    account: DefaultAccount,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    hasMultipleAccounts: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && hasMultipleAccounts) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.spacing.xs / 2,
        ),
        shape = KptTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mifos Logo Avatar
            Box(
                modifier = Modifier
                    .size(KptTheme.spacing.lg * 2)
                    .clip(CircleShape)
                    .background(KptTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(KptTheme.spacing.lg + KptTheme.spacing.sm)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            // Account Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs / 2),
            ) {
                // Client Name
                Text(
                    text = client.displayName,
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )

                // Office/Bank Name
                if (client.officeName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                    ) {
                        Icon(
                            imageVector = MifosIcons.Bank,
                            contentDescription = null,
                            modifier = Modifier.size(KptTheme.spacing.md - KptTheme.spacing.xs),
                            tint = KptTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = client.officeName,
                            style = KptTheme.typography.labelSmall,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Masked Account Number
                Text(
                    text = formatAccountDisplay(account.accountNo),
                    style = KptTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.primary,
                )
            }

            // Dropdown indicator when multiple accounts available
            if (hasMultipleAccounts) {
                Icon(
                    imageVector = MifosIcons.KeyboardArrowDown,
                    contentDescription = "Select account",
                    modifier = Modifier.size(KptTheme.spacing.lg),
                    tint = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Primary Badge
            if (isPrimary) {
                PrimaryBadge()
            }
        }
    }
}

@Composable
private fun PrimaryBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = KptTheme.shapes.small,
        color = KptTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = KptTheme.spacing.sm,
                vertical = KptTheme.spacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Icon(
                imageVector = MifosIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(KptTheme.spacing.md - KptTheme.spacing.xs),
                tint = KptTheme.colorScheme.onPrimary,
            )
            Text(
                text = stringResource(Res.string.feature_mpay_qr_primary),
                style = KptTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onPrimary,
            )
        }
    }
}

/**
 * Formats account number for display with spacing.
 * Example: "1234567890" -> "●●●● ●●●● 7890"
 */
private fun formatAccountDisplay(accountNo: String): String {
    if (accountNo.length <= 4) return accountNo
    val lastFour = accountNo.takeLast(4)
    return "●●●● ●●●● $lastFour"
}

@Preview
@Composable
private fun AccountSelectorCardPreview() {
    KptMaterialTheme {
        AccountSelectorCard(
            client = Client(
                id = 1,
                accountNo = "000000001",
                externalId = "EXT-123",
                active = true,
                activationDate = emptyList(),
                firstname = "John",
                lastname = "Doe",
                displayName = "John Doe",
                mobileNo = "",
                emailAddress = "",
                dateOfBirth = emptyList(),
                isStaff = false,
                officeId = 1,
                officeName = "Mifos Head Office",
                savingsProductName = "",
            ),
            account = DefaultAccount(
                accountId = 1,
                accountNo = "1234567890",
            ),
            isPrimary = true,
        )
    }
}

@Preview
@Composable
private fun AccountSelectorCardNoPrimaryPreview() {
    KptMaterialTheme {
        AccountSelectorCard(
            client = Client(
                id = 1,
                accountNo = "000000001",
                externalId = "EXT-123",
                active = true,
                activationDate = emptyList(),
                firstname = "Jane",
                lastname = "Smith",
                displayName = "Jane Smith",
                mobileNo = "",
                emailAddress = "",
                dateOfBirth = emptyList(),
                isStaff = false,
                officeId = 1,
                officeName = "Branch Office",
                savingsProductName = "",
            ),
            account = DefaultAccount(
                accountId = 2,
                accountNo = "9876543210",
            ),
            isPrimary = false,
        )
    }
}
