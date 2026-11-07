/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

/** Returns the [Platform] the app is running on. */
expect fun getPlatform(): Platform

/** Platforms the settings feature distinguishes between. */
enum class Platform {
    /** Android. */
    Android,

    /** Desktop (JVM). */
    Desktop,

    /** iOS. */
    IOS,

    /** Kotlin/JS web. */
    JS,

    /** Kotlin/Wasm web. */
    Wasm,
}

/** Whether the current platform supports dynamic (Material You) theming. */
expect fun supportsDynamicTheming(): Boolean
