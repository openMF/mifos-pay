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

/** Observable camera permission state plus the actions to request the permission or open system settings. */
interface CameraPermissionState {
    /** Current camera permission status. */
    val status: CameraPermissionStatus

    /** Asks the user for camera permission. */
    fun requestCameraPermission()

    /** Opens the system settings screen so the user can grant camera permission manually. */
    fun goToSettings()
}

/** Remembers a platform-specific [CameraPermissionState] for the current composition. */
@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState

/** Camera permission status values reported by [CameraPermissionState]. */
enum class CameraPermissionStatus {
    /** Permission status is not yet known */
    Unknown,

    /** Permission has been denied */
    Denied,

    /** Permission has been granted */
    Granted,
}
