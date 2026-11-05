/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.utils

/**
 * Password strength bands used by [PasswordChecker].
 */
enum class PasswordStrength {
    /** Weakest. */
    LEVEL_0,

    /** Very weak. */
    LEVEL_1,

    /** Weak. */
    LEVEL_2,

    /** Moderate. */
    LEVEL_3,

    /** Strong. */
    LEVEL_4,

    /** Strongest. */
    LEVEL_5,
}
