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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.KptShimmerListItem
import kpt.core.base.designsystem.component.KptShimmerLoadingBox

/**
 * Single dispatch composable for every "something is in progress" UI in the toolkit.
 * Pick a [KptProgress] variant; this renderer wires it to the right primitive at the
 * right size.
 *
 * - [KptProgress.Linear]   → M3 [LinearProgressIndicator]
 * - [KptProgress.Circular] → M3 [CircularProgressIndicator]
 * - [KptProgress.Shimmer]  → [KptShimmerLoadingBox]
 * - [KptProgress.Skeleton] → N [KptShimmerListItem]
 *
 * Determinate (progress != null) and indeterminate flavours selected per-variant.
 *
 * @param variant The [KptProgress] variant + size to render.
 * @param modifier Modifier applied to the root container of the chosen variant.
 */
@Composable
fun KptProgress(
    variant: KptProgress,
    modifier: Modifier = Modifier,
) {
    val (diameter, stroke) = ProgressSizeSpec.dpFor(variant.size)
    when (variant) {
        is KptProgress.Linear -> {
            val height = stroke.dp
            val barModifier = modifier
                .fillMaxWidth()
                .height(height)
            if (variant.progress == null) {
                LinearProgressIndicator(modifier = barModifier)
            } else {
                LinearProgressIndicator(
                    progress = { variant.progress.coerceIn(0f, 1f) },
                    modifier = barModifier,
                )
            }
        }

        is KptProgress.Circular -> {
            val circModifier = modifier.size(diameter.dp)
            if (variant.progress == null) {
                CircularProgressIndicator(
                    modifier = circModifier,
                    strokeWidth = stroke.dp,
                )
            } else {
                CircularProgressIndicator(
                    progress = { variant.progress.coerceIn(0f, 1f) },
                    modifier = circModifier,
                    strokeWidth = stroke.dp,
                )
            }
        }

        is KptProgress.Shimmer -> {
            KptShimmerLoadingBox(
                modifier = modifier
                    .fillMaxWidth(variant.widthFraction.coerceIn(0f, 1f))
                    .height(diameter.dp),
            )
        }

        is KptProgress.Skeleton -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(variant.rowCount) {
                    KptShimmerListItem(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
