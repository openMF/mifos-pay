/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.security

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memset

/** Apple/native implementation of `SecureWiper`. */
@OptIn(ExperimentalForeignApi::class)
actual class SecureWiper actual constructor() {

    /** `wipeSecureStorage` on Apple/native. */
    actual fun wipeSecureStorage() {
        Logger.w("SecureWiper") { "Secure storage wipe triggered" }
        // Consumer apps should delete Keychain items for service
        // "kpt.secure" and clear UserDefaults.
    }

    /** `scrubMemory` on Apple/native. */
    actual fun scrubMemory(data: ByteArray) {
        data.usePinned { pinned ->
            memset(pinned.addressOf(0), 0, data.size.toULong())
        }
    }
}
