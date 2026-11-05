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
 * The selected colour brand. A fork extends this to offer its own palettes.
 *
 * @property brandName persisted label — see [fromString].
 */
enum class ThemeBrand(val brandName: String) {
    /** The template's own palette. */
    DEFAULT("Default"),

    /** The Android-green palette. */
    ANDROID("Android"),
    ;

    /** Parsing. */
    companion object {
        /** Parses a persisted [brandName], case-insensitively, falling back to [DEFAULT] for an unknown value. */
        fun fromString(value: String): ThemeBrand {
            return entries.find { it.brandName.equals(value, ignoreCase = true) } ?: DEFAULT
        }
    }
}
