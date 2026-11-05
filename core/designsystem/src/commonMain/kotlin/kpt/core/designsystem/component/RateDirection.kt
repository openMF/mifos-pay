/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

/** Direction of a rate / price / metric change relative to the prior period. */
enum class RateDirection {
    /** Rate rose. */
    Up,

    /** Rate fell. */
    Down,

    /** Rate is unchanged. Distinct from an unknown change — this states that it did not move. */
    Flat,
}
