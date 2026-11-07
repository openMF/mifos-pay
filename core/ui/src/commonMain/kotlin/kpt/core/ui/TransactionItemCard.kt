/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_money_in
import kpt.core.ui.generated.resources.core_ui_money_out
import org.jetbrains.compose.resources.painterResource
/** Card wrapping a single [Transaction] row, clickable to open its details. */
@Composable
fun TransactionItemCard(
    transaction: Transaction,
    onClick: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = {
            onClick(transaction.accountId, transaction.transactionId)
        },
        color = Color.Transparent,
        contentColor = KptTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        ) {
            Column(
                modifier = Modifier,
            ) {
                Text(
                    text = transaction.transactionType.toString(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500),
                        color = KptTheme.colorScheme.onSurface,
                    ),
                )
                Text(
                    text = transaction.date,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight(400),
                        color = KptTheme.colorScheme.onSurface,
                    ),
                )
            }
            val formattedAmount = CurrencyFormatter.format(
                balance = transaction.amount,
                currencyCode = transaction.currency.code,
                maximumFractionDigits = 2,
            )
            val amount = when (transaction.transactionType) {
                TransactionType.DEBIT -> "- $formattedAmount"
                TransactionType.CREDIT -> "+ $formattedAmount"
                else -> formattedAmount
            }
            Text(
                modifier = Modifier,
                text = amount,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = when (transaction.transactionType) {
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

                        else -> KptTheme.colorScheme.scrim
                    },
                    textAlign = TextAlign.End,
                ),
            )
        }
    }
}

/** Row showing a [Transaction] amount, type and date with an optional leading icon. */
@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    showLeadingIcon: Boolean = true,
    onClick: (Long, Long) -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = {
            onClick(transaction.accountId, transaction.transactionId)
        },
        color = Color.Transparent,
        contentColor = KptTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                AnimatedVisibility(showLeadingIcon) {
                    Image(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = KptTheme.spacing.xs),
                        painter = painterResource(
                            resource = when (transaction.transactionType) {
                                TransactionType.DEBIT -> Res.drawable.core_ui_money_out
                                TransactionType.CREDIT -> Res.drawable.core_ui_money_in
                                else -> Res.drawable.core_ui_money_in
                            },
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(KptTheme.colorScheme.onSurface),
                    )
                }

                Column {
                    Text(
                        text = transaction.transactionType.name.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500),
                        style = KptTheme.typography.bodySmall,
                    )
                    Text(
                        text = transaction.date,
                        fontWeight = FontWeight(300),
                        style = KptTheme.typography.bodySmall,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val formattedAmount =
                    "${transaction.currency.displaySymbol}${
                        CurrencyFormatter.format(
                            balance = transaction.amount,
                            maximumFractionDigits = 2,
                        )
                    }"

                val amount = when (transaction.transactionType) {
                    TransactionType.DEBIT -> "- $formattedAmount"
                    TransactionType.CREDIT -> "+ $formattedAmount"
                    else -> formattedAmount
                }

                Text(
                    modifier = Modifier,
                    text = amount,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (transaction.reversed) {
                            Color.Blue
                        } else {
                            when (transaction.transactionType) {
                                TransactionType.CREDIT -> KptTheme.colorScheme.onTertiaryContainer.copy(
                                    red = 0f,
                                    green = 0.51f,
                                    blue = 0.21f,
                                )

                                TransactionType.DEBIT -> KptTheme.colorScheme.error.copy(
                                    red = 0.8f,
                                    green = 0f,
                                    blue = 0f,
                                )

                                else -> KptTheme.colorScheme.onSurface
                            }
                        },
                        textAlign = TextAlign.End,
                        textDecoration = if (transaction.reversed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                    ),
                )
            }
        }
    }
}
