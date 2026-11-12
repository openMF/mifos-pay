/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.browser.document
import kotlinx.coroutines.await
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_camera_access_denied
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_camera_denied_message
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_camera_error
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_camera_view_active
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_initializing_camera
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_position_qr_code
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_scan_qr_code
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_select_from_gallery
import org.jetbrains.compose.resources.stringResource
import org.w3c.dom.HTMLDivElement

/**
 * State of the web camera scanner.
 */
sealed interface WebCameraScannerState {
    /** Loading - initializing camera */
    data object Loading : WebCameraScannerState

    /** Scanning - camera is active and scanning */
    data object Scanning : WebCameraScannerState

    /** Permission denied - user denied camera access */
    data object PermissionDenied : WebCameraScannerState

    /**
     * Error - failed to initialize camera.
     *
     * @property message Description of the failure.
     */
    data class Error(val message: String) : WebCameraScannerState
}

/**
 * A composable that displays a live camera scanner using html5-qrcode library.
 *
 * @param onScanned Called when a QR code is successfully scanned
 * @param onFallbackToFilePicker Called when user chooses file picker fallback
 * @param modifier Modifier for the scanner
 */
@Composable
fun WebCameraScanner(
    onScanned: (String) -> Boolean,
    onFallbackToFilePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scannerState by remember { mutableStateOf<WebCameraScannerState>(WebCameraScannerState.Loading) }
    var scanner by remember { mutableStateOf<Html5Qrcode?>(null) }
    val scannerId = remember { "qr-scanner-${kotlin.random.Random.nextInt(10000)}" }

    // Create the scanner element
    LaunchedEffect(scannerId) {
        // Create a div element for the scanner
        val existingElement = document.getElementById(scannerId)
        if (existingElement == null) {
            val scannerDiv = document.createElement("div") as HTMLDivElement
            scannerDiv.id = scannerId
            scannerDiv.style.width = "100%"
            scannerDiv.style.maxWidth = "500px"
            scannerDiv.style.margin = "0 auto"
            document.body?.appendChild(scannerDiv)
        }
    }

    // Initialize and start camera
    LaunchedEffect(scannerId) {
        try {
            scannerState = WebCameraScannerState.Loading

            // Wait a bit for the DOM element to be ready
            kotlinx.coroutines.delay(100)

            val html5Qrcode = Html5Qrcode(scannerId)
            scanner = html5Qrcode

            val config = createScanConfig(
                fps = 10,
                qrboxWidth = 250,
                qrboxHeight = 250,
            )

            // Try to start with back camera
            html5Qrcode.start(
                createBackCameraConstraints(),
                config,
                { decodedText, _ ->
                    val handled = onScanned(decodedText)
                    if (handled) {
                        // Stop scanning after successful scan
                        try {
                            html5Qrcode.stop()
                        } catch (_: Exception) {
                            // Ignore stop errors
                        }
                    }
                },
                null,
            ).await()

            scannerState = WebCameraScannerState.Scanning
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"

            // Check if it's a permission error
            scannerState = if (
                errorMessage.contains("permission", ignoreCase = true) ||
                errorMessage.contains("NotAllowedError", ignoreCase = true)
            ) {
                WebCameraScannerState.PermissionDenied
            } else {
                WebCameraScannerState.Error(errorMessage)
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(scannerId) {
        onDispose {
            scanner?.let { html5Qrcode ->
                try {
                    html5Qrcode.stop()
                    html5Qrcode.clear()
                } catch (_: Exception) {
                    // Ignore cleanup errors
                }
            }
            // Remove the scanner element
            document.getElementById(scannerId)?.remove()
        }
    }

    val primaryColor = KptTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KptTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when (scannerState) {
            is WebCameraScannerState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.feature_qr_initializing_camera),
                        fontSize = 16.sp,
                        color = KptTheme.colorScheme.onBackground,
                    )
                }
            }

            is WebCameraScannerState.Scanning -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(Res.string.feature_qr_scan_qr_code),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(Res.string.feature_qr_position_qr_code),
                        fontSize = 14.sp,
                        color = KptTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // The camera view is rendered in the DOM element
                    // We show a placeholder box here for layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_qr_camera_view_active),
                            fontSize = 14.sp,
                            color = KptTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = onFallbackToFilePicker,
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Icon(
                            imageVector = ForkIcons.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(Res.string.feature_qr_select_from_gallery))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            is WebCameraScannerState.PermissionDenied -> {
                CameraErrorContent(
                    icon = ForkIcons.Warning,
                    title = stringResource(Res.string.feature_qr_camera_access_denied),
                    message = stringResource(Res.string.feature_qr_camera_denied_message),
                    primaryAction = stringResource(Res.string.feature_qr_select_from_gallery),
                    onPrimaryAction = onFallbackToFilePicker,
                )
            }

            is WebCameraScannerState.Error -> {
                CameraErrorContent(
                    icon = ForkIcons.Error,
                    title = stringResource(Res.string.feature_qr_camera_error),
                    message = (scannerState as WebCameraScannerState.Error).message,
                    primaryAction = stringResource(Res.string.feature_qr_select_from_gallery),
                    onPrimaryAction = onFallbackToFilePicker,
                )
            }
        }
    }
}

@Composable
private fun CameraErrorContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
) {
    val primaryColor = KptTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KptTheme.colorScheme.error,
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = KptTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = KptTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPrimaryAction,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier.fillMaxWidth(0.7f),
        ) {
            Icon(
                imageVector = ForkIcons.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = primaryAction)
        }
    }
}
