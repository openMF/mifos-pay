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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_powered_by
import org.jetbrains.compose.resources.stringResource

/** Footer caption displayed under the scanner viewfinder. */
@Composable
fun QrScanFooter(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.feature_qr_powered_by),
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    )
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun QrScanFooterPreview() {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.Black),
    ) {
        QrScanFooter()
    }
}
