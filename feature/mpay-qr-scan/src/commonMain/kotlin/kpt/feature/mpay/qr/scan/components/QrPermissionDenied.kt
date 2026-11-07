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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_camera_permission_description
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_camera_permission_title
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_open_settings
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_upload_qr
import org.jetbrains.compose.resources.stringResource
/** Content shown when camera permission is denied, offering to open settings or upload a QR image instead. */
@Composable
fun QrPermissionDenied(
    onOpenSettings: () -> Unit,
    onUploadQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = KptTheme.colorScheme.primary
    val titleText = stringResource(Res.string.feature_qr_camera_permission_title)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
            .semantics { contentDescription = titleText },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = MifosIcons.Camera,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(80.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = titleText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.feature_qr_camera_permission_description),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Icon(
                imageVector = MifosIcons.SettingsOutlined,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(Res.string.feature_qr_open_settings),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onUploadQr,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Icon(
                imageVector = MifosIcons.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(Res.string.feature_qr_upload_qr),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        QrScanFooter()
    }
}
