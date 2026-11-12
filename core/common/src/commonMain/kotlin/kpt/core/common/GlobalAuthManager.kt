/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import kotlinx.coroutines.flow.MutableStateFlow

/** Process-wide holder of the 'session unauthorized' signal raised when the server answers 401. */
object GlobalAuthManager {
    /** Emits true once a request was rejected as unauthorized, so the app can force a re-login. */
    val isUnauthorized = MutableStateFlow(false)

    /** Flags the session as unauthorized. */
    fun markUnauthorized() {
        isUnauthorized.value = true
    }

    /** Clears the unauthorized flag, typically after a fresh login. */
    fun reset() {
        isUnauthorized.value = false
    }
}
