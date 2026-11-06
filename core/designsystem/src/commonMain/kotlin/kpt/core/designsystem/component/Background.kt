/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.GradientColors
import kpt.core.designsystem.theme.LocalBackgroundTheme
import kpt.core.designsystem.theme.LocalGradientColors
import kpt.core.designsystem.theme.fork.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * The main background for the app.
 * Uses [LocalBackgroundTheme] to set the color and tonal elevation of a [Surface].
 *
 * @param modifier Modifier to be applied to the background.
 * @param content The background content.
 */
@Composable
fun MifosBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val color = LocalBackgroundTheme.current.color
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    Surface(
        color = if (color == Color.Unspecified) Color.Transparent else color,
        tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}

/**
 * A gradient background for select screens. Uses [LocalBackgroundTheme] to set the gradient colors
 * of a [Box] within a [Surface].
 *
 * @param modifier Modifier to be applied to the background.
 * @param gradientColors The gradient colors to be rendered.
 * @param content The background content.
 */
@Composable
fun MifosGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    val currentBottomColor by rememberUpdatedState(gradientColors.bottom)
    Surface(
        color = if (gradientColors.container == Color.Unspecified) {
            Color.Transparent
        } else {
            gradientColors.container
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val mainGradient = Brush.linearGradient(
                        colors = listOf(currentTopColor, currentBottomColor),
                    )

                    onDrawBehind {
                        // There is overlap here, so order is important
                        drawRect(mainGradient)
                    }
                },
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun BackgroundDefault() {
    MifosTheme {
        MifosBackground(Modifier.size(100.dp), content = {})
    }
}

@Preview
@Composable
fun BackgroundDynamic() {
    MifosTheme {
        MifosBackground(Modifier.size(100.dp), content = {})
    }
}

@Preview
@Composable
fun BackgroundAndroid() {
    MifosTheme {
        MifosBackground(Modifier.size(100.dp), content = {})
    }
}

@Preview
@Composable
fun GradientBackgroundDefault() {
    MifosTheme {
        MifosGradientBackground(Modifier.size(100.dp), content = {})
    }
}

@Preview
@Composable
fun GradientBackgroundDynamic() {
    MifosTheme {
        MifosGradientBackground(Modifier.size(100.dp), content = {})
    }
}

@Preview
@Composable
fun GradientBackgroundAndroid() {
    MifosTheme {
        MifosGradientBackground(Modifier.size(100.dp), content = {})
    }
}
