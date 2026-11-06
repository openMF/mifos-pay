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

interface CameraPermissionState {
    val status: CameraPermissionStatus
    fun requestCameraPermission()
    fun goToSettings()
}

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState

enum class CameraPermissionStatus {
    /** Permission status is not yet known */
    Unknown,

    /** Permission has been denied */
    Denied,

    /** Permission has been granted */
    Granted,
}
