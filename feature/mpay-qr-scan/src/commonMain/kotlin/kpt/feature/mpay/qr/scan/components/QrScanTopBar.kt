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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_close
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_help
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_scan_any_qr
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_supported_transfers
import org.jetbrains.compose.resources.stringResource
/** Top bar of the scanner with close and help buttons. */
@Composable
fun QrScanTopBar(
    onCloseClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val closeDescription = stringResource(Res.string.feature_qr_close)
    val helpDescription = stringResource(Res.string.feature_qr_help)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = ForkIcons.Close,
                contentDescription = closeDescription,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.feature_qr_scan_any_qr),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(Res.string.feature_qr_supported_transfers),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
            )
        }

        IconButton(
            onClick = onHelpClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = ForkIcons.Info,
                contentDescription = helpDescription,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun QrScanTopBarPreview() {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.Black),
    ) {
        QrScanTopBar(
            onCloseClick = {},
            onHelpClick = {},
        )
    }
}
