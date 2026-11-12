/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

/**
 * Row that wraps onto additional lines when content exceeds the available width.
 */
@Composable
fun KptFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier.testTag("KptFlowRow"),
        content = content,
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        val childConstraints = Constraints(maxWidth = constraints.maxWidth)

        measurables.fastForEach { measurable ->
            val placeable = measurable.measure(childConstraints)

            if (currentSequence.isNotEmpty() &&
                (
                    currentMainAxisSize + placeable.width > constraints.maxWidth ||
                        currentSequence.size >= maxItemsInEachRow
                    )
            ) {
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)
                crossAxisSpace += currentCrossAxisSize

                currentSequence.clear()
                currentMainAxisSize = placeable.width
                currentCrossAxisSize = placeable.height
            } else {
                currentMainAxisSize += placeable.width
                currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
            }

            currentSequence += placeable
        }

        if (currentSequence.isNotEmpty()) {
            sequences += currentSequence
            crossAxisSizes += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)
            crossAxisSpace += currentCrossAxisSize
        }

        val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)
        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        var crossAxisPosition = 0
        // Read crossAxisSIZES to build crossAxisPOSITIONS. Iterating the output list left it empty,
        // so the `crossAxisPositions[sequenceIndex]` read below threw IndexOutOfBounds on the first
        // row — this composable could not render any content at all. `KptFlowColumn`, otherwise an
        // identical twin, has always read the right list here.
        crossAxisSizes.fastForEach { size ->
            crossAxisPositions += crossAxisPosition
            crossAxisPosition += size
        }

        layout(mainAxisLayoutSize, crossAxisLayoutSize) {
            sequences.forEachIndexed { sequenceIndex, placeables ->
                val rowCrossAxisSize = crossAxisSizes[sequenceIndex]
                val rowCrossAxisPosition = crossAxisPositions[sequenceIndex]

                // Honour the declared arrangement. Both parameters were previously accepted and
                // ignored, so `horizontalArrangement = Arrangement.Center` laid out identically to
                // `Arrangement.Start` — an API that silently does nothing is worse than one that
                // does not offer the option.
                val widths = IntArray(placeables.size) { placeables[it].width }
                val positions = IntArray(placeables.size)
                with(horizontalArrangement) {
                    arrange(mainAxisLayoutSize, widths, layoutDirection, positions)
                }

                placeables.fastForEachIndexed { index, placeable ->
                    placeable.place(
                        x = positions[index],
                        y = rowCrossAxisPosition +
                            verticalAlignment.align(placeable.height, rowCrossAxisSize),
                    )
                }
            }
        }
    }
}
