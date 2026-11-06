/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberContactPermissionState(): ContactPermissionState {
    val accPermissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS)

    val context = LocalContext.current
    val wrapper = remember(accPermissionState) {
        AccompanistContactPermissionWrapper(accPermissionState, context)
    }

    return wrapper
}

@OptIn(ExperimentalPermissionsApi::class)
class AccompanistContactPermissionWrapper(
    private val permissionState: PermissionState,
    private val context: Context,
) : ContactPermissionState {
    override val status: ContactPermissionStatus
        get() = permissionState.status.toContactPermissionStatus()

    override fun requestContactPermission() {
        permissionState.launchPermissionRequest()
    }

    override fun goToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun PermissionStatus.toContactPermissionStatus(): ContactPermissionStatus {
    return when (this) {
        is PermissionStatus.Granted -> ContactPermissionStatus.Granted
        is PermissionStatus.Denied -> ContactPermissionStatus.Denied
    }
}
