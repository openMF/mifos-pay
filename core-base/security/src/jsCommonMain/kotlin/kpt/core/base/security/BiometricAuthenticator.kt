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

/** Web (JS/WasmJS) implementation of `BiometricAuthenticator`. */
actual class BiometricAuthenticator actual constructor() {

    /** `isAvailable` on Web (JS/WasmJS). */
    actual fun isAvailable(): Boolean = false

    /** `authenticate` on Web (JS/WasmJS). */
    actual suspend fun authenticate(reason: String): BiometricResult {
        return BiometricResult.Unavailable
    }
}
