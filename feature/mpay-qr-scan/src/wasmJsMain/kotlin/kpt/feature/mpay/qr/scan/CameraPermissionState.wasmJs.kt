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

/**
 * WasmJS implementation of camera permission state.
 *
 * Returns Granted status since we use file picker on WasmJS.
 */
@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    return object : CameraPermissionState {
        override val status: CameraPermissionStatus
            get() = CameraPermissionStatus.Granted

        override fun requestCameraPermission() {
            // Not supported on WasmJS - using file picker instead
        }

        override fun goToSettings() {
            // Not supported on WasmJS
        }
    }
}
