/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.hub.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkOutlinedButton
import kpt.core.designsystem.fork.component.ForkTextUserImage
import kpt.core.model.beneficiary.Beneficiary
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_pay_button
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Card showing a saved beneficiary with a pay button. */
@Composable
fun BeneficiaryCard(
    beneficiary: Beneficiary,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ForkTextUserImage(
                text = getInitials(beneficiary.name),
                size = 44.dp,
            )

            Spacer(Modifier.width(KptTheme.spacing.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = beneficiary.name,
                    style = KptTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = KptTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = formatAccountDisplay(beneficiary.accountNumber),
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.width(KptTheme.spacing.sm))

            ForkOutlinedButton(
                onClick = onPayClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = KptTheme.colorScheme.primary,
                ),
            ) {
                Text(stringResource(Res.string.feature_send_money_pay_button))
            }
        }
    }
}

/**
 * Gets initials from name (first letter of first two words)
 */
private fun getInitials(name: String): String {
    val words = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(1).uppercase()
        else -> "${words[0].first().uppercase()}${words[1].first().uppercase()}"
    }
}

/**
 * Formats account number to display as "●●●● ●●●● 1234"
 */
private fun formatAccountDisplay(accountNo: String): String {
    val lastFourDigits = if (accountNo.length >= 4) {
        accountNo.takeLast(4)
    } else {
        accountNo
    }
    return "●●●● ●●●● $lastFourDigits"
}

@Preview
@Composable
private fun PreviewBeneficiaryCard() {
    KptMaterialTheme {
        BeneficiaryCard(
            beneficiary = Beneficiary(
                id = 1,
                name = "John Doe",
                officeName = "Head Office",
                clientName = "John Doe",
                accountType = Beneficiary.AccountType(
                    id = 2,
                    code = "SAVINGS",
                    value = "Savings",
                ),
                accountNumber = "ACC1234567543",
                transferLimit = 100000,
            ),
            onPayClick = {},
        )
    }
}
