/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.merchants.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.theme.styleMedium16sp
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import mifos_pay.feature.merchants.generated.resources.Res
import mifos_pay.feature.merchants.generated.resources.feature_merchants_ic_bank
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MerchantsItem(
    savingsWithAssociations: SavingsWithAssociationsEntity,
    onMerchantClicked: () -> Unit,
    onMerchantLongPressed: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosCard(
        modifier = modifier.combinedClickable(
            onClick = onMerchantClicked,
            onLongClick = {
                onMerchantLongPressed(savingsWithAssociations.id.toString())
            },
        ),
        colors = CardDefaults.cardColors(KptTheme.colorScheme.surface),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KptTheme.spacing.md),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.feature_merchants_ic_bank),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = KptTheme.spacing.md, end = KptTheme.spacing.md)
                        .size(39.dp),
                )

                Column {
                    Text(
                        text = savingsWithAssociations.clientName,
                        color = KptTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = savingsWithAssociations.accountNo,
                        modifier = Modifier.padding(top = KptTheme.spacing.xs),
                        style = styleMedium16sp,
                        color = KptTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.padding(KptTheme.spacing.sm),
            color = KptTheme.colorScheme.outlineVariant,
        )
    }
}

@Preview
@Composable
private fun AccountsItemPreview() {
    MerchantsItem(
        savingsWithAssociations = sampleMerchantList.first(),
        onMerchantClicked = {},
        onMerchantLongPressed = {},
    )
}
