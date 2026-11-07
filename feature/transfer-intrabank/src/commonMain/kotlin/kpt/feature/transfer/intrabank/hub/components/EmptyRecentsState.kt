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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_add_payee
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_no_recents
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_no_recents_subtitle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Placeholder shown when there are no recent payees, with an optional add-payee action. */
@Composable
fun EmptyRecentsState(
    onAddPayeeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = MifosIcons.History,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = KptTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        Text(
            text = stringResource(Res.string.feature_send_money_no_recents),
            style = KptTheme.typography.titleMedium,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(KptTheme.spacing.sm))

        Text(
            text = stringResource(Res.string.feature_send_money_no_recents_subtitle),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onAddPayeeClick != null) {
            Spacer(Modifier.height(KptTheme.spacing.lg))

            MifosOutlinedButton(
                onClick = onAddPayeeClick,
                text = {
                    Text(stringResource(Res.string.feature_send_money_add_payee))
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEmptyRecentsState() {
    KptMaterialTheme {
        EmptyRecentsState(
            onAddPayeeClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEmptyRecentsStateNoButton() {
    KptMaterialTheme {
        EmptyRecentsState()
    }
}
