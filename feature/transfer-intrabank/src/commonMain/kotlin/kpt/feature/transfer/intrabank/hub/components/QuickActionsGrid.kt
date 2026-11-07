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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_add_payee
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_history
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_request
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_scan_qr
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Grid of quick actions on the hub: add payee, history, scan QR and request money. */
@Composable
fun QuickActionsGrid(
    onAddPayeeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onRequestMoneyClick: () -> Unit,
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
                .padding(vertical = KptTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            QuickActionItem(
                icon = MifosIcons.Add,
                label = stringResource(Res.string.feature_send_money_add_payee),
                onClick = onAddPayeeClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionItem(
                icon = MifosIcons.History,
                label = stringResource(Res.string.feature_send_money_history),
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionItem(
                icon = MifosIcons.Scan,
                label = stringResource(Res.string.feature_send_money_scan_qr),
                onClick = onScanQrClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionItem(
                icon = MifosIcons.Transfer,
                label = stringResource(Res.string.feature_send_money_request),
                onClick = onRequestMoneyClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(KptTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = KptTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = KptTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = KptTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(KptTheme.spacing.lg),
            )
        }
        Spacer(Modifier.height(KptTheme.spacing.sm))
        Text(
            text = label,
            style = KptTheme.typography.labelMedium,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun PreviewQuickActionsGrid() {
    KptMaterialTheme {
        QuickActionsGrid(
            onAddPayeeClick = {},
            onHistoryClick = {},
            onScanQrClick = {},
            onRequestMoneyClick = {},
        )
    }
}
