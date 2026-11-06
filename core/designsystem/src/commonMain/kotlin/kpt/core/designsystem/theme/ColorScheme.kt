/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import kpt.core.base.designsystem.toKptColorScheme
import kpt.core.designsystem.theme.fork.backgroundDark
import kpt.core.designsystem.theme.fork.backgroundLight
import kpt.core.designsystem.theme.fork.errorContainerDark
import kpt.core.designsystem.theme.fork.errorContainerLight
import kpt.core.designsystem.theme.fork.errorDark
import kpt.core.designsystem.theme.fork.errorLight
import kpt.core.designsystem.theme.fork.inverseOnSurfaceDark
import kpt.core.designsystem.theme.fork.inverseOnSurfaceLight
import kpt.core.designsystem.theme.fork.inversePrimaryDark
import kpt.core.designsystem.theme.fork.inversePrimaryLight
import kpt.core.designsystem.theme.fork.inverseSurfaceDark
import kpt.core.designsystem.theme.fork.inverseSurfaceLight
import kpt.core.designsystem.theme.fork.onBackgroundDark
import kpt.core.designsystem.theme.fork.onBackgroundLight
import kpt.core.designsystem.theme.fork.onErrorContainerDark
import kpt.core.designsystem.theme.fork.onErrorContainerLight
import kpt.core.designsystem.theme.fork.onErrorDark
import kpt.core.designsystem.theme.fork.onErrorLight
import kpt.core.designsystem.theme.fork.onPrimaryContainerDark
import kpt.core.designsystem.theme.fork.onPrimaryContainerLight
import kpt.core.designsystem.theme.fork.onPrimaryDark
import kpt.core.designsystem.theme.fork.onPrimaryLight
import kpt.core.designsystem.theme.fork.onSecondaryContainerDark
import kpt.core.designsystem.theme.fork.onSecondaryContainerLight
import kpt.core.designsystem.theme.fork.onSecondaryDark
import kpt.core.designsystem.theme.fork.onSecondaryLight
import kpt.core.designsystem.theme.fork.onSurfaceDark
import kpt.core.designsystem.theme.fork.onSurfaceLight
import kpt.core.designsystem.theme.fork.onSurfaceVariantDark
import kpt.core.designsystem.theme.fork.onSurfaceVariantLight
import kpt.core.designsystem.theme.fork.onTertiaryContainerDark
import kpt.core.designsystem.theme.fork.onTertiaryContainerLight
import kpt.core.designsystem.theme.fork.onTertiaryDark
import kpt.core.designsystem.theme.fork.onTertiaryLight
import kpt.core.designsystem.theme.fork.outlineDark
import kpt.core.designsystem.theme.fork.outlineLight
import kpt.core.designsystem.theme.fork.outlineVariantDark
import kpt.core.designsystem.theme.fork.outlineVariantLight
import kpt.core.designsystem.theme.fork.primaryContainerDark
import kpt.core.designsystem.theme.fork.primaryContainerLight
import kpt.core.designsystem.theme.fork.primaryDark
import kpt.core.designsystem.theme.fork.primaryLight
import kpt.core.designsystem.theme.fork.scrimDark
import kpt.core.designsystem.theme.fork.scrimLight
import kpt.core.designsystem.theme.fork.secondaryContainerDark
import kpt.core.designsystem.theme.fork.secondaryContainerLight
import kpt.core.designsystem.theme.fork.secondaryDark
import kpt.core.designsystem.theme.fork.secondaryLight
import kpt.core.designsystem.theme.fork.surfaceBrightDark
import kpt.core.designsystem.theme.fork.surfaceBrightLight
import kpt.core.designsystem.theme.fork.surfaceContainerDark
import kpt.core.designsystem.theme.fork.surfaceContainerHighDark
import kpt.core.designsystem.theme.fork.surfaceContainerHighLight
import kpt.core.designsystem.theme.fork.surfaceContainerHighestDark
import kpt.core.designsystem.theme.fork.surfaceContainerHighestLight
import kpt.core.designsystem.theme.fork.surfaceContainerLight
import kpt.core.designsystem.theme.fork.surfaceContainerLowDark
import kpt.core.designsystem.theme.fork.surfaceContainerLowLight
import kpt.core.designsystem.theme.fork.surfaceContainerLowestDark
import kpt.core.designsystem.theme.fork.surfaceContainerLowestLight
import kpt.core.designsystem.theme.fork.surfaceDark
import kpt.core.designsystem.theme.fork.surfaceDimDark
import kpt.core.designsystem.theme.fork.surfaceDimLight
import kpt.core.designsystem.theme.fork.surfaceLight
import kpt.core.designsystem.theme.fork.surfaceVariantDark
import kpt.core.designsystem.theme.fork.surfaceVariantLight
import kpt.core.designsystem.theme.fork.tertiaryContainerDark
import kpt.core.designsystem.theme.fork.tertiaryContainerLight
import kpt.core.designsystem.theme.fork.tertiaryDark
import kpt.core.designsystem.theme.fork.tertiaryLight

// private val LightDefaultColorScheme = lightColorScheme(
//    primary = md_theme_light_primary,
//    onPrimary = md_theme_light_onPrimary,
//    primaryContainer = md_theme_light_primaryContainer,
//    onPrimaryContainer = md_theme_light_onPrimaryContainer,
//    secondary = md_theme_light_secondary,
//    onSecondary = md_theme_light_onSecondary,
//    secondaryContainer = md_theme_light_secondaryContainer,
//    onSecondaryContainer = md_theme_light_onSecondaryContainer,
//    tertiary = md_theme_light_tertiary,
//    onTertiary = md_theme_light_onTertiary,
//    tertiaryContainer = md_theme_light_tertiaryContainer,
//    onTertiaryContainer = md_theme_light_onTertiaryContainer,
//    error = md_theme_light_error,
//    errorContainer = md_theme_light_errorContainer,
//    onError = md_theme_light_onError,
//    onErrorContainer = md_theme_light_onErrorContainer,
//    background = md_theme_light_background,
//    onBackground = md_theme_light_onBackground,
//    surface = md_theme_light_surface,
//    onSurface = md_theme_light_onSurface,
//    surfaceVariant = md_theme_light_surfaceVariant,
//    onSurfaceVariant = md_theme_light_onSurfaceVariant,
//    outline = md_theme_light_outline,
//    inverseOnSurface = md_theme_light_inverseOnSurface,
//    inverseSurface = md_theme_light_inverseSurface,
//    inversePrimary = md_theme_light_inversePrimary,
//    surfaceTint = md_theme_light_surfaceTint,
//    outlineVariant = md_theme_light_outlineVariant,
//    scrim = md_theme_light_scrim,
// )

private val LightDefaultColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

// private val DarkDefaultColorScheme = darkColorScheme(
//    primary = md_theme_dark_primary,
//    onPrimary = md_theme_dark_onPrimary,
//    primaryContainer = md_theme_dark_primaryContainer,
//    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
//    secondary = md_theme_dark_secondary,
//    onSecondary = md_theme_dark_onSecondary,
//    secondaryContainer = md_theme_dark_secondaryContainer,
//    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
//    tertiary = md_theme_dark_tertiary,
//    onTertiary = md_theme_dark_onTertiary,
//    tertiaryContainer = md_theme_dark_tertiaryContainer,
//    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
//    error = md_theme_dark_error,
//    errorContainer = md_theme_dark_errorContainer,
//    onError = md_theme_dark_onError,
//    onErrorContainer = md_theme_dark_onErrorContainer,
//    background = md_theme_dark_background,
//    onBackground = md_theme_dark_onBackground,
//    surface = md_theme_dark_surface,
//    onSurface = md_theme_dark_onSurface,
//    surfaceVariant = md_theme_dark_surfaceVariant,
//    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
//    outline = md_theme_dark_outline,
//    inverseOnSurface = md_theme_dark_inverseOnSurface,
//    inverseSurface = md_theme_dark_inverseSurface,
//    inversePrimary = md_theme_dark_inversePrimary,
//    surfaceTint = md_theme_dark_surfaceTint,
//    outlineVariant = md_theme_dark_outlineVariant,
//    scrim = md_theme_dark_scrim,
// )

private val DarkDefaultColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val lightKptColorScheme = LightDefaultColorScheme.toKptColorScheme()
val darkKptColorScheme = DarkDefaultColorScheme.toKptColorScheme()
