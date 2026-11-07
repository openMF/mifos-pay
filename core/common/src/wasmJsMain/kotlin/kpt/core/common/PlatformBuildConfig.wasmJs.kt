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

/** Web: a deployed site is public, so debug-only UI stays off. */
actual object PlatformBuildConfig {
    /** True when this is a debug build; used to gate debug-only UI. */
    actual val isDebug: Boolean = false
}
