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
 * Column that wraps into additional columns when content exceeds the available height.
 */
@Composable
fun KptFlowColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    maxItemsInEachColumn: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier.testTag("KptFlowColumn"),
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

        val childConstraints = Constraints(maxHeight = constraints.maxHeight)

        measurables.fastForEach { measurable ->
            val placeable = measurable.measure(childConstraints)

            if (currentSequence.isNotEmpty() &&
                (
                    currentMainAxisSize + placeable.height > constraints.maxHeight ||
                        currentSequence.size >= maxItemsInEachColumn
                    )
            ) {
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)
                crossAxisSpace += currentCrossAxisSize

                currentSequence.clear()
                currentMainAxisSize = placeable.height
                currentCrossAxisSize = placeable.width
            } else {
                currentMainAxisSize += placeable.height
                currentCrossAxisSize = max(currentCrossAxisSize, placeable.width)
            }

            currentSequence += placeable
        }

        if (currentSequence.isNotEmpty()) {
            sequences += currentSequence
            crossAxisSizes += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)
            crossAxisSpace += currentCrossAxisSize
        }

        val mainAxisLayoutSize = max(mainAxisSpace, constraints.minHeight)
        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minWidth)

        var crossAxisPosition = 0
        crossAxisSizes.fastForEach { size ->
            crossAxisPositions += crossAxisPosition
            crossAxisPosition += size
        }

        layout(crossAxisLayoutSize, mainAxisLayoutSize) {
            sequences.forEachIndexed { sequenceIndex, placeables ->
                val columnCrossAxisSize = crossAxisSizes[sequenceIndex]
                val columnCrossAxisPosition = crossAxisPositions[sequenceIndex]

                // Honour the declared arrangement — both parameters were accepted and ignored, so
                // `verticalArrangement = Arrangement.Center` laid out identically to `Top`.
                val heights = IntArray(placeables.size) { placeables[it].height }
                val positions = IntArray(placeables.size)
                with(verticalArrangement) {
                    arrange(mainAxisLayoutSize, heights, positions)
                }

                placeables.fastForEachIndexed { index, placeable ->
                    placeable.place(
                        x = columnCrossAxisPosition +
                            horizontalAlignment.align(placeable.width, columnCrossAxisSize, layoutDirection),
                        y = positions[index],
                    )
                }
            }
        }
    }
}
