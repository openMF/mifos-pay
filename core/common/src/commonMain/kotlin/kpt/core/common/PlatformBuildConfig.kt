/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

/**
 * Whether this binary is a debug build, for UI that only developers should see. Named to avoid a clash
 * with Android's generated `BuildConfig`.
 */
expect object PlatformBuildConfig {
    /** True in a debug build; always false where the platform cannot tell (Android, web). */
    val isDebug: Boolean
}
