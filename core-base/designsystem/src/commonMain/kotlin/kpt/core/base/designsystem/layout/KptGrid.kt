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

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import kpt.core.base.designsystem.theme.KptTheme
import kotlin.math.min

/**
 * Responsive grid whose column count derives from the available width via [BreakpointConfiguration].
 */
@Composable
fun KptGrid(
    modifier: Modifier = Modifier,
    configuration: GridConfiguration = GridConfiguration(
        spacing = KptTheme.spacing.md,
        horizontalPadding = KptTheme.spacing.md,
    ),
    content: @Composable GridScope.() -> Unit,
) {
    val density = LocalDensity.current
    val columns = configuration.getColumnsForCurrentScreen()

    Layout(
        modifier = modifier
            .padding(horizontal = configuration.horizontalPadding)
            .testTag("KptGrid"),
        content = {
            GridScopeImpl(columns, configuration.spacing).content()
        },
    ) { measurables, constraints ->
        val spacing = with(density) { configuration.spacing.roundToPx() }
        val horizontalPadding = with(density) { configuration.horizontalPadding.roundToPx() }

        val availableWidth = constraints.maxWidth - horizontalPadding * 2
        val columnWidth = (availableWidth - spacing * (columns - 1)) / columns

        val placeables = measurables.fastMap { measurable ->
            val gridItem = measurable.parentData as? GridItemData ?: GridItemData()
            val itemColumns = min(gridItem.span, columns)
            val itemWidth = columnWidth * itemColumns + spacing * (itemColumns - 1)

            measurable.measure(
                constraints.copy(
                    minWidth = itemWidth,
                    maxWidth = itemWidth,
                ),
            )
        }

        val rows = mutableListOf<MutableList<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowColumns = 0

        placeables.fastForEach { placeable ->
            val gridItem = placeable.parentData as? GridItemData ?: GridItemData()
            val itemColumns = min(gridItem.span, columns)

            if (currentRowColumns + itemColumns > columns) {
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                    currentRowColumns = 0
                }
            }

            currentRow.add(placeable)
            currentRowColumns += itemColumns
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val rowHeights = rows.fastMap { row ->
            row.fastMaxBy { it.height }?.height ?: 0
        }

        val totalHeight = rowHeights.sum() + spacing * (rows.size - 1).coerceAtLeast(0)

        layout(constraints.maxWidth, totalHeight) {
            var yPosition = 0

            rows.forEachIndexed { rowIndex, row ->
                var xPosition = 0

                row.fastForEach { placeable ->
                    placeable.place(xPosition, yPosition)
                    val gridItem = placeable.parentData as? GridItemData ?: GridItemData()
                    val itemColumns = min(gridItem.span, columns)
                    xPosition += columnWidth * itemColumns + spacing * itemColumns
                }

                yPosition += rowHeights[rowIndex] + spacing
            }
        }
    }
}

/**
 * Receiver for [KptGrid] content — declare items and spans here.
 */
interface GridScope {
    /**
     * Claims [span] columns for this item. Clamped to the grid's column count, so an over-wide span degrades to full
     * width instead of overflowing.
     */
    fun Modifier.gridItem(span: Int = 1): Modifier
}

private class GridScopeImpl(
    private val columns: Int,
    private val spacing: Dp,
) : GridScope {
    override fun Modifier.gridItem(span: Int): Modifier {
        return this.then(
            GridItemModifier(
                span = span.coerceIn(1, columns),
            ),
        )
    }
}

private data class GridItemData(
    val span: Int = 1,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@GridItemData
}

private data class GridItemModifier(
    val span: Int,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return GridItemData(span)
    }
}

/**
 * Spacing and padding for a [KptGrid]; defaults come from the theme spacing scale.
 */
@Immutable
data class GridConfiguration(
    /** Gutter between cells, both axes. */
    val spacing: Dp,
    /** Inset from the grid's own edges. Distinct from [spacing] so the outer margin and the inner gutter can differ. */
    val horizontalPadding: Dp,
    /** Column count at the widest breakpoint. 12 by default — the divisor that supports halves, thirds and quarters. */
    val columns: Int = 12,
    /** Width thresholds that reduce [columns] on a narrow window. */
    val breakpoints: BreakpointConfiguration = BreakpointConfiguration(),
) {
    /**
     * The column count for the CURRENT window width, read from `LocalWindowInfo`.
     *
     * Composable because it observes window size — a resize or a fold recomposes the grid. Thresholds are checked
     * widest-first, so the first match wins.
     */
    @Composable
    fun getColumnsForCurrentScreen(): Int {
        val window = LocalWindowInfo.current
        val screenWidth = window.containerSize.width.dp

        return when {
            screenWidth >= breakpoints.xl -> breakpoints.xlColumns
            screenWidth >= breakpoints.lg -> breakpoints.lgColumns
            screenWidth >= breakpoints.md -> breakpoints.mdColumns
            screenWidth >= breakpoints.sm -> breakpoints.smColumns
            else -> breakpoints.xsColumns
        }
    }
}

/**
 * Width thresholds mapping available width to a column count.
 */
@Immutable
data class BreakpointConfiguration(
    /** Floor threshold — always 0.dp, so the smallest bucket always matches. */
    val xs: Dp = 0.dp,
    /** Small-window threshold; the Material 3 compact/medium boundary. */
    val sm: Dp = 600.dp,
    /** Medium-window threshold — a large phone in landscape, or a small tablet. */
    val md: Dp = 840.dp,
    /** Large-window threshold — a tablet or a small desktop window. */
    val lg: Dp = 1200.dp,
    /** Extra-large threshold — a full desktop window. */
    val xl: Dp = 1600.dp,
    /** Columns below [sm]. Four, because a phone cannot subdivide further and stay legible. */
    val xsColumns: Int = 4,
    /** Columns from [sm] up to [md]. */
    val smColumns: Int = 8,
    /** Columns from [md] up to [lg]. */
    val mdColumns: Int = 12,
    /** Columns from [lg] up to [xl]. */
    val lgColumns: Int = 12,
    /** Columns at [xl] and above. */
    val xlColumns: Int = 12,
)
