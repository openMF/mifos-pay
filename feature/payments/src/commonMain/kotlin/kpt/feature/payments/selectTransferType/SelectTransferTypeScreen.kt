/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.payments.selectTransferType

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.ui.AvatarBox
import mifos_pay.feature.payments.generated.resources.Res
import mifos_pay.feature.payments.generated.resources.feature_payments_inter_bank_transfer_description
import mifos_pay.feature.payments.generated.resources.feature_payments_inter_bank_transfer_title
import mifos_pay.feature.payments.generated.resources.feature_payments_intra_bank_transfer_description
import mifos_pay.feature.payments.generated.resources.feature_payments_intra_bank_transfer_title
import mifos_pay.feature.payments.generated.resources.feature_payments_select_transfer_type_header
import mifos_pay.feature.payments.generated.resources.feature_payments_select_transfer_type_subtitle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun SelectTransferTypeScreen(
    onIntraBankTransferClick: () -> Unit,
    onInterBankTransferClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KptTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(KptTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = KptTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.feature_payments_select_transfer_type_header),
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal,
                color = KptTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.feature_payments_select_transfer_type_subtitle),
                style = KptTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = KptTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }

        // Intra-Bank Transfer Card
        TransferTypeCard(
            title = stringResource(Res.string.feature_payments_intra_bank_transfer_title),
            description = stringResource(Res.string.feature_payments_intra_bank_transfer_description),
            icon = MifosIcons.Transfer,
            onClick = onIntraBankTransferClick,
        )

        // Inter-Bank Transfer Card
        TransferTypeCard(
            title = stringResource(Res.string.feature_payments_inter_bank_transfer_title),
            description = stringResource(Res.string.feature_payments_inter_bank_transfer_description),
            icon = MifosIcons.Bank,
            onClick = onInterBankTransferClick,
        )

        // Spacer
        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
    }
}

@Composable
private fun TransferTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Card(
                modifier = Modifier.size(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = KptTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AvatarBox(
                        modifier = Modifier.size(50.dp),
                        icon = icon,
                        size = 50,
                        backgroundColor = KptTheme.colorScheme.primaryContainer,
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = KptTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = title,
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.onBackground,
                )
                Text(
                    text = description,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Arrow Icon
            Icon(
                imageVector = MifosIcons.ArrowRight,
                contentDescription = "Navigate to $title",
                modifier = Modifier
                    .padding(end = KptTheme.spacing.sm)
                    .size(24.dp),
                tint = KptTheme.colorScheme.primary,
            )
        }
    }
}

@Preview
@Composable
fun SelectTransferTypeScreenPreview() {
    MifosTheme {
        SelectTransferTypeScreen(
            onIntraBankTransferClick = {},
            onInterBankTransferClick = {},
        )
    }
}
