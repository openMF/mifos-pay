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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_inter_bank_unavailable
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Placeholder card shown when Inter-Bank QR code cannot be generated.
 * Displays a humanized message explaining why the QR code is unavailable
 * and what the user can do to enable it.
 */
@Composable
internal fun InterBankPlaceholder(
    reason: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceVariant,
        ),
        shape = KptTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KptTheme.spacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = ForkIcons.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = KptTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.md))

            Text(
                text = stringResource(Res.string.feature_mpay_qr_inter_bank_unavailable),
                style = KptTheme.typography.titleMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

            Text(
                text = reason,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun InterBankPlaceholderPreview() {
    KptMaterialTheme {
        InterBankPlaceholder(
            reason = "Your account doesn't have an external ID configured. Contact your bank to enable inter-bank transfers.",
        )
    }
}
