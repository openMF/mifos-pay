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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme

/**
 * Hero card — the dashboard's first impression. Renders a vertical gradient from
 * [MaterialTheme.colorScheme.primary] (top) to [MaterialTheme.colorScheme.primaryContainer]
 * (bottom) and locks [LocalContentColor] to [MaterialTheme.colorScheme.onPrimary] so any
 * Text inside reads cleanly without per-call color wiring.
 *
 * Use for the single most-important block at the top of a screen: account balance,
 * portfolio total, today's headline rate.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    // KptTheme.spacing.lg == 24.dp — value-equivalent to the legacy MaterialTheme.spacing.xl (24dp).
    contentPadding: PaddingValues = PaddingValues(KptTheme.spacing.lg),
    gradientStart: Color = MaterialTheme.colorScheme.primary,
    gradientEnd: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            // KptTheme.elevation.level2 == 3.dp — value-equivalent to the legacy MaterialTheme.elevation.medium (3dp).
            defaultElevation = KptTheme.elevation.level2,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(gradientStart, gradientEnd),
                    ),
                )
                .padding(contentPadding),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
            ) {
                content()
            }
        }
    }
}
