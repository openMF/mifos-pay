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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import mifos_pay.feature.mpay_qr_scan.generated.resources.Res
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_drag_drop_hint
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_drop_image_here
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_import_description
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_import_qr_code
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_powered_by
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_preview_image
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_processing
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_select_image
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_select_qr_image
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_supported_formats
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_supported_transfers
import org.jetbrains.compose.resources.stringResource
@Composable
fun QrImportScreen(
    isProcessing: Boolean,
    onSelectImage: () -> Unit,
    modifier: Modifier = Modifier,
    imagePreviewBytes: ByteArray? = null,
    isDragging: Boolean = false,
    showHeader: Boolean = true,
    useDarkTheme: Boolean = false,
) {
    val primaryColor = KptTheme.colorScheme.primary
    val importTitle = stringResource(Res.string.feature_qr_import_qr_code)
    val selectImageDescription = stringResource(Res.string.feature_qr_select_qr_image)
    val platformContext = LocalPlatformContext.current
    val imageLoader = remember(platformContext) { ImageLoader(platformContext) }

    // Colors based on theme mode - use inverse colors for dark theme
    val backgroundColor = if (useDarkTheme) KptTheme.colorScheme.inverseSurface else KptTheme.colorScheme.background
    val textColor = if (useDarkTheme) KptTheme.colorScheme.inverseOnSurface else KptTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = if (useDarkTheme) 0.7f else 0.6f)
    val tertiaryTextColor = textColor.copy(alpha = if (useDarkTheme) 0.5f else 0.4f)
    val borderColor = primaryColor.copy(alpha = if (useDarkTheme) 0.5f else 0.3f)
    val areaBackground = primaryColor.copy(alpha = if (useDarkTheme) 0.1f else 0.05f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .semantics { contentDescription = importTitle },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showHeader) {
                Spacer(modifier = Modifier.height(48.dp))

                // Header
                Icon(
                    imageVector = MifosIcons.QrCode2,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(64.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = importTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.feature_qr_supported_transfers),
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                )

                Spacer(modifier = Modifier.height(48.dp))
            } else {
                // When header is hidden (used inside scanner screen), add top padding for toolbar
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Import area with drag & drop visual feedback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        border = BorderStroke(
                            width = if (isDragging) 3.dp else 2.dp,
                            color = if (isDragging) primaryColor else borderColor,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .background(
                        if (isDragging) {
                            primaryColor.copy(alpha = 0.15f)
                        } else {
                            areaBackground
                        },
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    when {
                        isProcessing -> {
                            // Show image preview while processing
                            if (imagePreviewBytes != null) {
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = imagePreviewBytes,
                                            imageLoader = imageLoader,
                                        ),
                                        contentDescription = stringResource(Res.string.feature_qr_preview_image),
                                        modifier = Modifier
                                            .size(130.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit,
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            CircularProgressIndicator(
                                color = primaryColor,
                                modifier = Modifier.size(48.dp),
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.feature_qr_processing),
                                fontSize = 16.sp,
                                color = textColor,
                            )
                        }

                        isDragging -> {
                            Icon(
                                imageVector = MifosIcons.PhotoLibrary,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(80.dp),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(Res.string.feature_qr_drop_image_here),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                textAlign = TextAlign.Center,
                            )
                        }

                        else -> {
                            Icon(
                                imageVector = MifosIcons.PhotoLibrary,
                                contentDescription = null,
                                tint = primaryColor.copy(alpha = if (useDarkTheme) 1f else 0.6f),
                                modifier = Modifier.size(80.dp),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(Res.string.feature_qr_import_description),
                                fontSize = 16.sp,
                                color = textColor.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = onSelectImage,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(52.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = selectImageDescription
                                    },
                            ) {
                                Icon(
                                    imageVector = MifosIcons.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = stringResource(Res.string.feature_qr_select_image),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.feature_qr_drag_drop_hint),
                                fontSize = 14.sp,
                                color = tertiaryTextColor,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Supported formats
            Text(
                text = stringResource(Res.string.feature_qr_supported_formats),
                fontSize = 12.sp,
                color = tertiaryTextColor,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer branding
            Text(
                text = stringResource(Res.string.feature_qr_powered_by),
                fontSize = 12.sp,
                color = tertiaryTextColor,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
