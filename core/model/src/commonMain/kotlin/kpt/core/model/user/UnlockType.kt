/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

/**
 * How the user unlocks the app — passcode or biometric.
 */
enum class UnlockType {
    /** Alphanumeric password — no length or character policy is implied here; that lives in `PasswordChecker`. */
    PASSWORD,

    /** Numeric PIN. Shows a digits-only keypad, which is the only behavioural difference from [PASSWORD]. */
    PIN,
}
