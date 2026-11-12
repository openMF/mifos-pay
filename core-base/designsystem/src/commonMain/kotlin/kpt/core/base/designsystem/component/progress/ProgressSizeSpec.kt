/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.component.progress

/**
 * T-shirt sizes for [KptProgress] variants. Maps to (diameter, stroke) dp pairs via
 * [ProgressSizeSpec.dpFor] — keeps every project-wide progress indicator on a
 * single set of rhythm-aligned dimensions.
 */
enum class ProgressSize {
    /** Inline with body text — a spinner beside a label. */
    Xs,

    /** Inside a compact control, such as a button that is loading. */
    Sm,

    /** The default: a card or a section that is loading. */
    Md,

    /** Full-screen or empty-state loading, where the indicator is the only content. */
    Lg,
}

/**
 * Canonical (diameter dp, stroke dp) values for each [ProgressSize]. Internal so
 * call sites stay declarative — they pass a [ProgressSize] and the renderer reads
 * the spec at render time.
 */
internal object ProgressSizeSpec {
    /** Returns (diameter dp, stroke dp) for the given [size]. */
    fun dpFor(size: ProgressSize): Pair<Int, Int> = when (size) {
        ProgressSize.Xs -> 16 to 2
        ProgressSize.Sm -> 24 to 2
        ProgressSize.Md -> 36 to 3
        ProgressSize.Lg -> 48 to 4
    }
}
