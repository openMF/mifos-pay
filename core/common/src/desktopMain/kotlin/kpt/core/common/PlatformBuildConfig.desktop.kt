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

/** Desktop: debug when JVM assertions are enabled (`-ea`, the development run configuration). */
actual object PlatformBuildConfig {
    /** True when this is a debug build; used to gate debug-only UI. */
    actual val isDebug: Boolean = assertionsEnabled()

    private fun assertionsEnabled(): Boolean {
        var enabled = false
        @Suppress("KotlinConstantConditions")
        assert(true.also { enabled = true })
        return enabled
    }
}
