/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.fork.utils

/** Password strength from weakest (level 0) to strongest (level 5). */
enum class PasswordStrength {
    /** Strength level 0. */
    LEVEL_0,

    /** Strength level 1. */
    LEVEL_1,

    /** Strength level 2. */
    LEVEL_2,

    /** Strength level 3. */
    LEVEL_3,

    /** Strength level 4. */
    LEVEL_4,

    /** Strength level 5. */
    LEVEL_5,
}
