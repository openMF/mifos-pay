/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_help_description
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_help_title
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_ok
import org.jetbrains.compose.resources.stringResource
/** Dialog explaining how to scan a QR code; closed through [onDismiss]. */
@Composable
fun QrHelpDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = MifosIcons.Warning,
                    contentDescription = null,
                    tint = KptTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(Res.string.feature_qr_help_title),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.feature_qr_help_description),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.feature_qr_ok))
            }
        },
    )
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun QrHelpDialogPreview() {
    kpt.core.base.designsystem.KptMaterialTheme {
        QrHelpDialog(
            onDismiss = {},
        )
    }
}
