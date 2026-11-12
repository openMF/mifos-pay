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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import kpt.core.base.designsystem.theme.KptTheme
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_preview_image
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_processing
import org.jetbrains.compose.resources.stringResource

/** Overlay showing the selected image preview and a progress indicator while a QR image is decoded. */
@Composable
fun QrProcessingOverlay(
    imageBytes: ByteArray?,
    modifier: Modifier = Modifier,
) {
    val primaryColor = KptTheme.colorScheme.primary
    val processingText = stringResource(Res.string.feature_qr_processing)
    val previewDescription = stringResource(Res.string.feature_qr_preview_image)
    val platformContext = LocalPlatformContext.current
    val imageLoader = remember(platformContext) { ImageLoader(platformContext) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .semantics { contentDescription = processingText },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            // Image preview
            if (imageBytes != null) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = imageBytes,
                            imageLoader = imageLoader,
                        ),
                        contentDescription = previewDescription,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            CircularProgressIndicator(
                color = primaryColor,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = processingText,
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }
}
