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
 * Sealed family of "something is in progress" UI variants. Callers pick the right
 * variant for the surface — [Linear] for top-of-screen progress bars, [Circular] for
 * in-list / overlay spinners, [Shimmer] for content placeholders, [Skeleton] for
 * multi-row list placeholders.
 *
 * Every variant carries a [ProgressSize] so the rhythm stays consistent across the app.
 *
 * Use via the `KptProgress(variant, modifier)` composable in [KptProgressRenderer].
 */
sealed interface KptProgress {
    /** T-shirt size — drives diameter/stroke for Circular, height for Linear, row sizing for Skeleton. */
    val size: ProgressSize

    /**
     * Material 3 [androidx.compose.material3.LinearProgressIndicator]. Use for top-of-
     * screen progress (refresh banners, multi-step flows).
     *
     * @param progress When null, indeterminate. When non-null in [0f, 1f], determinate.
     */
    data class Linear(
        override val size: ProgressSize = ProgressSize.Md,
        val progress: Float? = null,
    ) : KptProgress

    /**
     * Material 3 [androidx.compose.material3.CircularProgressIndicator]. Use for
     * in-list "load more" footers, button-internal loading states, modal overlays.
     *
     * @param progress When null, indeterminate. When non-null in [0f, 1f], determinate.
     */
    data class Circular(
        override val size: ProgressSize = ProgressSize.Md,
        val progress: Float? = null,
    ) : KptProgress

    /**
     * Single shimmer bar (delegates to [kpt.core.base.designsystem.component.KptShimmerLoadingBox]).
     * Use for inline placeholders for individual amount/title/value rows.
     *
     * @param widthFraction Fraction of the parent width occupied by the shimmer bar.
     */
    data class Shimmer(
        override val size: ProgressSize = ProgressSize.Md,
        val widthFraction: Float = 1f,
    ) : KptProgress

    /**
     * Multi-row shimmer skeleton (N copies of
     * [kpt.core.base.designsystem.component.KptShimmerListItem]). Use for list-screen
     * loading states.
     *
     * @param rowCount Number of skeleton rows to render.
     */
    data class Skeleton(
        override val size: ProgressSize = ProgressSize.Md,
        val rowCount: Int = 3,
    ) : KptProgress
}
