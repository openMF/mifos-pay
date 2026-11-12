/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

/** Apple/native — AES/GCM via CommonCrypto, keyed from the Keychain. */
@OptIn(ExperimentalForeignApi::class)
actual class SecureRandom {
    /** `nextBytes` on this target. Apple/native — AES/GCM via CommonCrypto, keyed from the Keychain. */
    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            val status = SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
            if (status != errSecSuccess) {
                error("SecRandomCopyBytes failed: $status")
            }
        }
        return bytes
    }
}
