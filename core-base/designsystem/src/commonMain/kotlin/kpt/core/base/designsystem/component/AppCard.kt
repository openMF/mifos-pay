/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme

/**
 * Material 3 elevated card for grouping related content (loan rows, form sections,
 * dashboard tiles).
 *
 * **Design choices:**
 *  - [ElevatedCard] with a real 6dp shadow for crisp depth — beats a grey background +
 *    border combo, which reads as flat and amateur.
 *  - Container: [MaterialTheme.colorScheme.surfaceContainerLowest] (cleanest, lifts off
 *    the page background without competing with content).
 *  - 20dp rounded corners — modern fintech feel without going overly playful.
 *  - **Optional** left-edge accent stripe (4dp) in any color — pass [accentColor] to
 *    highlight a card's status (e.g. primary for "selected", secondary for "success",
 *    tertiary for "warning"). When `null`, no stripe is drawn so default cards stay
 *    clean. This is the only "color" the card carries — content (StatusChip, MoneyText,
 *    HeroCard) is where the palette lives, by design.
 *  - **No border.** Elevation + content do the lifting. A border on top of a shadow on
 *    top of a tonal lift is the visual equivalent of three exclamation marks.
 *
 * Prefer [AppCard] over raw [androidx.compose.material3.Card] / Surface — it enforces
 * consistent depth, shape, and accent treatment so screens look unified.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(KptTheme.spacing.md),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    accentColor: Color? = null,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
    ) {
        if (accentColor != null) {
            // Stripe + content row. IntrinsicSize.Min forces the Row to take the
            // height of its tallest child (the Column), so the Box stripe fills
            // matching height — otherwise fillMaxHeight() inside a wrap-content
            // Row collapses to 0.
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accentColor),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                ) {
                    content()
                }
            }
        } else {
            Column(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}
