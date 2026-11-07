/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

/** Stored value for [AppLockOption.MifosPasscode]. */
const val MIFOS_PASSCODE_VALUE = "mifos_passcode"

/** Stored value for [AppLockOption.DeviceLock]. */
const val DEVICE_AUTHENTICATION_METHOD_VALUE = "device_authentication_method"

/** Helpers for persisting [AppLockOption] values as strings. */
object Helpers {

    /** Converts an [AppLockOption] to the string stored in preferences. */
    fun authOptionToStringMapperFunction(option: AppLockOption): String {
        return when (option) {
            AppLockOption.MifosPasscode -> MIFOS_PASSCODE_VALUE
            AppLockOption.DeviceLock -> DEVICE_AUTHENTICATION_METHOD_VALUE
            AppLockOption.None -> ""
        }
    }

    /** Converts a stored string value back to an [AppLockOption], defaulting to [AppLockOption.None]. */
    fun stringToAuthOptionMapperFunction(option: String): AppLockOption {
        return when (option) {
            MIFOS_PASSCODE_VALUE -> AppLockOption.MifosPasscode
            DEVICE_AUTHENTICATION_METHOD_VALUE -> AppLockOption.DeviceLock
            else -> AppLockOption.None
        }
    }
}
