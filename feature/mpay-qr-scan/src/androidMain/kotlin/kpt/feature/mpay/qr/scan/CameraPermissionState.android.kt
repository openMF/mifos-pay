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

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Original source: https://github.com/kalinjul/EasyQRScan
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val accPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val context = LocalContext.current
    val wrapper = remember(accPermissionState) {
        AccompanistPermissionWrapper(accPermissionState, context)
    }

    return wrapper
}

/**
 * Original source: https://github.com/kalinjul/EasyQRScan
 */
@OptIn(ExperimentalPermissionsApi::class)
class AccompanistPermissionWrapper(
    private val permissionState: PermissionState,
    private val context: Context,
) : CameraPermissionState {
    override val status: CameraPermissionStatus
        get() = permissionState.status.toCameraPermissionStatus()

    override fun requestCameraPermission() {
        permissionState.launchPermissionRequest()
    }

    override fun goToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

/**
 * Original source: https://github.com/kalinjul/EasyQRScan
 *
 * Maps Accompanist's PermissionStatus to our CameraPermissionStatus.
 * When shouldShowRationale is false and status is Denied, it could mean:
 * 1. Permission was never requested (first time) - treat as Unknown
 * 2. Permission was denied with "Don't ask again" - treat as Denied
 * We treat both cases the same way and let the permission request flow handle it.
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun PermissionStatus.toCameraPermissionStatus(): CameraPermissionStatus {
    return when (this) {
        is PermissionStatus.Denied -> CameraPermissionStatus.Denied
        PermissionStatus.Granted -> CameraPermissionStatus.Granted
    }
}
