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

@Composable
actual fun rememberContactPermissionState(): ContactPermissionState {
    return object : ContactPermissionState {
        override val status: ContactPermissionStatus = ContactPermissionStatus.Denied

        override fun requestContactPermission() {
            // TODO
        }

        override fun goToSettings() {
            // TODO
        }
    }
}
