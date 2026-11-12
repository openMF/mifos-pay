/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.datastore

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

/** Apple/native implementation of `SecureSettingsFactory`. */
actual class SecureSettingsFactory {
    /** `create` on Apple/native. */
    @OptIn(ExperimentalSettingsImplementation::class)
    actual fun create(): Settings {
        return KeychainSettings(service = "kpt.secure")
    }
}
