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

/** Platform-agnostic holder for the contacts permission state and the actions to change it. */
interface ContactPermissionState {
    /** Current contacts permission status. */
    val status: ContactPermissionStatus

    /** Launches the system prompt requesting contacts permission. */
    fun requestContactPermission()

    /** Opens the system settings page where the user can change the contacts permission. */
    fun goToSettings()
}

/** Granted or denied status of the contacts permission. */
enum class ContactPermissionStatus {
    /** The user has not granted access to contacts. */
    Denied,

    /** The user has granted access to contacts. */
    Granted,
}

/** Remembers a platform-specific [ContactPermissionState] for the current composition. */
@Composable
expect fun rememberContactPermissionState(): ContactPermissionState
