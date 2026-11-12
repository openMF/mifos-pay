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

import com.russhwolf.settings.Settings

/**
 * Platform-specific factory that creates an encrypted [Settings] instance.
 *
 * Returns the standard [Settings] interface — zero API change for consumers.
 * - Android: EncryptedSharedPreferences -> SharedPreferencesSettings
 * - iOS: KeychainSettings (serviceName = "kpt.secure")
 * - Desktop: AES-encrypted PropertiesSettings
 * - Web: AES-GCM via WebCrypto, key non-extractable in IndexedDB. The web actual additionally
 *   requires `SecureSettingsFactory.warmUp()` to complete before `create()`, because
 *   WebCrypto is async-only while this factory is not.
 */
expect class SecureSettingsFactory {
    /** Creates the encrypted settings store backed by this platform's secure storage. */
    fun create(): Settings
}
