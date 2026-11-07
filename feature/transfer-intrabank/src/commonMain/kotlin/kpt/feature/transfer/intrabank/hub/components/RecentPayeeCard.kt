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
import kpt.core.model.account.RecentPayee
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_pay_button
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Card showing a recent payee with a pay button. */
@Composable
fun RecentPayeeCard(
    payee: RecentPayee,
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
                text = payee.initials,
                size = 44.dp,
            )

            Spacer(Modifier.width(KptTheme.spacing.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = payee.clientName,
                    style = KptTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = KptTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = formatAccountDisplay(payee.accountNo),
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = formatAmount(payee.currency, payee.lastAmount),
                    style = KptTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatDate(payee.lastTransferDate),
                    style = KptTheme.typography.labelSmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
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

/**
 * Formats amount with currency symbol
 */
private fun formatAmount(currency: String, amount: Double): String {
    val wholePart = amount.toLong()
    val decimalPart = ((amount - wholePart) * 100).toInt()
    val formattedWhole = wholePart.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    val formattedAmount = "$formattedWhole.${decimalPart.toString().padStart(2, '0')}"
    return "$currency $formattedAmount"
}

/**
 * Formats date from "yyyy-MM-dd" to "dd MMM 'yy" format
 */
private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val year = parts[0].takeLast(2)
            val month = parts[1].toIntOrNull() ?: return dateString
            val day = parts[2].toIntOrNull() ?: return dateString
            val monthName = getMonthShortName(month)
            val dayFormatted = day.toString().padStart(2, '0')
            "$dayFormatted $monthName '$year"
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}

private fun getMonthShortName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}

@Preview
@Composable
private fun PreviewRecentPayeeCard() {
    KptMaterialTheme {
        RecentPayeeCard(
            payee = RecentPayee(
                clientId = 1,
                clientName = "John Doe",
                accountId = 101,
                accountNo = "ACC1234567543",
                officeId = 1,
                officeName = "HQ",
                lastTransferDate = "2026-02-07",
                lastAmount = 63823.00,
                currency = "₹",
            ),
            onPayClick = {},
        )
    }
}
