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

import androidx.compose.runtime.Composable
import kpt.core.designsystem.component.PermissionBox
/** Android actual: shows a permission box prompting for READ_CONTACTS access. */
@Composable
actual fun ContactPermissionHandler() {
    PermissionBox(
        title = "Contact Permission Required",
        confirmButtonText = "Grant Permission",
        dismissButtonText = "Open Settings",
        requiredPermissions = listOf("android.permission.READ_CONTACTS"),
        description = "To help you select contacts for payments, we need access to your contacts. This allows you to quickly find and select contacts when making payments.",
        onGranted = { },
    )
}
