/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.common.DateHelper
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.ui.AvatarBox
import kpt.feature.history.generated.resources.Res
import kpt.feature.history.generated.resources.feature_history_debited_from
import kpt.feature.history.generated.resources.feature_history_paid_to
import kpt.feature.history.generated.resources.feature_history_transaction_date
import kpt.feature.history.generated.resources.feature_history_transaction_id
import org.jetbrains.compose.resources.stringResource
@Composable
internal fun TransactionDetail(
    modifier: Modifier = Modifier,
    detail: TransferDetail,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.feature_history_transaction_id),
                    style = KptTheme.typography.labelLarge,
                )
                Text(text = detail.id.toString())
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.feature_history_transaction_date),
                    style = KptTheme.typography.labelLarge,
                )
                val date = DateHelper.getDateAsString(detail.transferDate)
                Text(text = date)
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(Res.string.feature_history_paid_to),
                style = KptTheme.typography.labelLarge,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KptTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
            ) {
                ListItem(
                    headlineContent = {
                        Text(text = detail.toClient.displayName)
                    },
                    supportingContent = {
                        Text(text = detail.toAccount.accountNo)
                    },
                    leadingContent = {
                        AvatarBox(
                            name = detail.toClient.displayName,
                        )
                    },
                    trailingContent = {
                        val amount = CurrencyFormatter.format(
                            detail.transferAmount,
                            currencyCode = detail.currency.code,
                            maximumFractionDigits = null,
                        )
                        Text(
                            text = amount,
                            style = KptTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = KptTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        KptTheme.spacing.xs,
                        Alignment.CenterHorizontally,
                    ),
                ) {
                    Text(
                        text = detail.toOffice.name,
                        style = KptTheme.typography.labelSmall,
                    )
                    Text(
                        text = "|",
                        style = KptTheme.typography.labelSmall,
                    )
                    Text(
                        text = detail.toAccountType.value,
                        style = KptTheme.typography.labelSmall,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(Res.string.feature_history_debited_from),
                style = KptTheme.typography.labelLarge,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KptTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
            ) {
                ListItem(
                    headlineContent = {
                        Text(text = detail.fromClient.displayName)
                    },
                    supportingContent = {
                        Text(text = detail.fromAccount.accountNo)
                    },
                    leadingContent = {
                        AvatarBox(name = detail.fromClient.displayName)
                    },
                    trailingContent = {
                        val amount = CurrencyFormatter.format(
                            detail.transferAmount,
                            currencyCode = detail.currency.code,
                            maximumFractionDigits = null,
                        )
                        Text(
                            text = amount,
                            style = KptTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = KptTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        KptTheme.spacing.xs,
                        Alignment.CenterHorizontally,
                    ),
                ) {
                    Text(
                        text = detail.fromOffice.name,
                        style = KptTheme.typography.labelSmall,
                    )
                    Text(
                        text = "|",
                        style = KptTheme.typography.labelSmall,
                    )
                    Text(
                        text = detail.fromAccountType.value,
                        style = KptTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
