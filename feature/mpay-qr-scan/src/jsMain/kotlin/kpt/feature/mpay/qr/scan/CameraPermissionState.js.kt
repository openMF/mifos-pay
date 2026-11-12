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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

/**
 * Web implementation of camera permission state.
 *
 * Uses the browser's Permissions API when available, with fallback
 * to attempting getUserMedia to check permission status.
 */
@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    var permissionStatus by remember { mutableStateOf(CameraPermissionStatus.Unknown) }
    val scope = rememberCoroutineScope()

    // Check initial permission status
    LaunchedEffect(Unit) {
        permissionStatus = checkCameraPermission()
    }

    return remember(permissionStatus) {
        object : CameraPermissionState {
            override val status: CameraPermissionStatus
                get() = permissionStatus

            override fun requestCameraPermission() {
                scope.launch {
                    permissionStatus = requestCameraAccess()
                }
            }

            override fun goToSettings() {
                // On web, we can't programmatically open browser settings
                // The user needs to manually access their browser settings
                println(
                    "Camera settings: Please go to your browser settings to manage camera permissions.",
                )
            }
        }
    }
}

/**
 * Checks the current camera permission status using the Permissions API.
 */
private suspend fun checkCameraPermission(): CameraPermissionStatus {
    return try {
        // Check if Permissions API is available
        val hasPermissionsApi = js(
            "typeof navigator !== 'undefined' && typeof navigator.permissions !== 'undefined'",
        ).unsafeCast<Boolean>()

        if (hasPermissionsApi) {
            val permissionStatus = js("navigator.permissions.query({ name: 'camera' })")
                .unsafeCast<Promise<dynamic>>()
                .await()

            when (permissionStatus.state as String) {
                "granted" -> CameraPermissionStatus.Granted
                "denied" -> CameraPermissionStatus.Denied
                "prompt" -> CameraPermissionStatus.Unknown
                else -> CameraPermissionStatus.Unknown
            }
        } else {
            // Permissions API not available, check if getUserMedia exists
            val hasGetUserMedia = WebPlatformDetection.hasCameraApi()
            if (hasGetUserMedia) {
                CameraPermissionStatus.Unknown // We don't know until we try
            } else {
                CameraPermissionStatus.Denied // No camera API available
            }
        }
    } catch (e: Exception) {
        CameraPermissionStatus.Unknown
    }
}

/**
 * Requests camera access using getUserMedia.
 */
private suspend fun requestCameraAccess(): CameraPermissionStatus {
    return try {
        val hasGetUserMedia = WebPlatformDetection.hasCameraApi()
        if (!hasGetUserMedia) {
            return CameraPermissionStatus.Denied
        }

        // Try to get camera access
        val stream = js("navigator.mediaDevices.getUserMedia({ video: true })")
            .unsafeCast<Promise<dynamic>>()
            .await()

        // Stop all tracks immediately - we just wanted to check permission
        val tracks = stream.getTracks().unsafeCast<Array<dynamic>>()
        tracks.forEach { track ->
            track.stop()
        }

        CameraPermissionStatus.Granted
    } catch (e: Exception) {
        val errorMessage = e.message ?: ""
        when {
            errorMessage.contains("NotAllowedError") ||
                errorMessage.contains("Permission denied") ||
                errorMessage.contains("denied") -> CameraPermissionStatus.Denied
            errorMessage.contains("NotFoundError") -> CameraPermissionStatus.Denied
            else -> CameraPermissionStatus.Unknown
        }
    }
}
