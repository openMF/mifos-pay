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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.model.savingsaccount.Transaction
@Composable
fun TransactionHistoryCard(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    showLeadingIcon: Boolean = false,
    showViewAll: Boolean = true,
    onViewTransaction: (Long, Long) -> Unit,
    onClickViewAll: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
    ) {
        Column(
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
                Text(
                    text = "Transaction History",
                    fontWeight = FontWeight(500),
                )

                AnimatedVisibility(showViewAll) {
                    Box(
                        modifier = Modifier.clickable(
                            onClick = onClickViewAll,
                        ),
                    ) {
                        Text(
                            text = "See All",
                            style = KptTheme.typography.bodySmall,
                            fontWeight = FontWeight(300),
                        )
                    }
                }
            }

            transactions.forEachIndexed { i, transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = onViewTransaction,
                    showLeadingIcon = showLeadingIcon,
                )

                if (i != transactions.size - 1) {
                    MifosDivider(
                        modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
                    )
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions found",
                    modifier = Modifier.padding(KptTheme.spacing.md),
                    style = KptTheme.typography.bodySmall,
                    fontWeight = FontWeight(300),
                )
            }
        }
    }
}
