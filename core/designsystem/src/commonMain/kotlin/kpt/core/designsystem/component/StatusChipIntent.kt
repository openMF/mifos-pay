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

/**
 * Semantic intent of a [StatusChip]. Maps to a (container, content) color pair derived
 * from the active Material color scheme + finance palette.
 */
enum class StatusChipIntent {
    /** Positive / paid / completed. Emerald. */
    Success,

    /** Warning / due-soon / partial. Amber. */
    Warning,

    /** Overdue / failed / urgent. Rose. */
    Danger,

    /** Informational / upcoming / scheduled. Indigo container. */
    Info,

    /** Neutral / archived / inactive. */
    Neutral,
}
