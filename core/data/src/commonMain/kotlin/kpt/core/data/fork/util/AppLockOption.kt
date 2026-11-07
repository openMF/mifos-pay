/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.util

/** How the app is locked on launch. */
enum class AppLockOption {
    /** Lock using the app passcode. */
    Passcode,

    /** Lock using the device's own authentication (biometrics or screen lock). */
    DeviceLock,

    /** No app lock. */
    None,
}
