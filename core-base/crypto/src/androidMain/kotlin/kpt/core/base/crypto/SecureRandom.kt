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

/** Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
actual class SecureRandom {
    private val random = java.security.SecureRandom()

    /** `nextBytes` on this target. Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }
}
