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
 * The user's dark-mode preference: follow the system, or force light/dark.
 *
 * `osValue` maps to the platform's own night-mode constant, so the choice can be handed straight to
 * the OS rather than re-interpreted per platform.
 *
 * @property configName persisted label — see [fromString].
 * @property osValue the platform's own night-mode constant, so the choice can be handed straight to the OS.
 */
enum class DarkThemeConfig(val configName: String, val osValue: Int) {
    /** Track the OS setting. */
    FOLLOW_SYSTEM("Follow System", -1),

    /** Always light, whatever the system is doing. */
    LIGHT("Light", 1),

    /** Always dark, whatever the system is doing. */
    DARK("Dark", 2),
    ;

    /** Parsing. */
    companion object {
        /**
         * Parses a persisted [configName], case-insensitively. Falls back to [FOLLOW_SYSTEM] for an unknown value so a
         * stale or hand-edited preference cannot leave the app themeless.
         */
        fun fromString(value: String): DarkThemeConfig {
            return entries.find { it.configName.equals(value, ignoreCase = true) } ?: FOLLOW_SYSTEM
        }
    }
}
