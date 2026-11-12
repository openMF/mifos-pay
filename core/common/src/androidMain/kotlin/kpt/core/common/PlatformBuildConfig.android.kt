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
 * Android: a KMP library module gets no generated `BuildConfig` under AGP 9, so there is nothing to read
 * here without an application context. Reports false — the production-safe answer.
 */
actual object PlatformBuildConfig {
    /** True when this is a debug build; used to gate debug-only UI. */
    actual val isDebug: Boolean = false
}
