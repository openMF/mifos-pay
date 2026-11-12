/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import kpt.core.base.designsystem.core.KptThemeProvider
import kpt.core.base.designsystem.theme.KptThemeProviderImpl
import kpt.core.base.designsystem.theme.LocalKptColors
import kpt.core.base.designsystem.theme.LocalKptElevation
import kpt.core.base.designsystem.theme.LocalKptShapes
import kpt.core.base.designsystem.theme.LocalKptSpacing
import kpt.core.base.designsystem.theme.LocalKptTypography

/**
 * KptMaterialTheme provides Material3 integration for KptTheme.
 * This composable applies KptTheme values to MaterialTheme automatically,
 * making all Material3 components use KptTheme design tokens.
 *
 * Inside [content], the Material3 accessors and the Kpt ones resolve to the same tokens, so a
 * Material component and a Kpt component placed side by side cannot disagree:
 *
 * ```
 * KptMaterialTheme {
 *     MaterialTheme.colorScheme.primary   // == KptTheme.colorScheme.primary
 *     MaterialTheme.typography.titleLarge // == KptTheme.typography.titleLarge
 *     KptTheme.spacing.md                 // Kpt-only tokens stay reachable
 * }
 * ```
 *
 * @param theme design tokens to apply.
 * @param content content with access to both KptTheme and MaterialTheme.
 */
@Composable
fun KptMaterialTheme(
    theme: KptThemeProvider = KptThemeProviderImpl(),
    content: @Composable () -> Unit,
) {
    // Convert KptTheme values to Material3 equivalents
    val materialColorScheme = theme.colors.toMaterial3ColorScheme()
    val materialTypography = theme.typography.toMaterial3Typography()
    val materialShapes = theme.shapes.toMaterial3Shapes()

    // Provide both KptTheme composition locals and MaterialTheme
    CompositionLocalProvider(
        LocalKptColors provides theme.colors,
        LocalKptTypography provides theme.typography,
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = materialTypography,
            shapes = materialShapes,
            content = content,
        )
    }
}

/**
 * KptMaterialTheme with dark theme support.
 * Provides automatic light/dark theme switching with Material3 integration.
 *
 * ```
 * KptMaterialTheme(
 *     lightTheme = kptTheme { colors { primary = Color.Blue } },
 *     darkThemeProvider = kptTheme { colors { primary = Color.Cyan } },
 * ) { /* switches with the system setting */ }
 * ```
 *
 * @param darkTheme whether to select the dark theme. Defaults to the system setting.
 * @param lightTheme tokens used when [darkTheme] is false.
 * @param darkThemeProvider tokens used when [darkTheme] is true.
 * @param content content with access to both KptTheme and MaterialTheme.
 */
@Composable
fun KptMaterialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightTheme: KptThemeProvider = KptThemeProviderImpl(),
    darkThemeProvider: KptThemeProvider = KptThemeProviderImpl(),
    content: @Composable () -> Unit,
) {
    val selectedTheme = if (darkTheme) darkThemeProvider else lightTheme
    KptMaterialTheme(
        theme = selectedTheme,
        content = content,
    )
}

/**
 * Builds the theme from the dark-mode flag, for a palette that differs by more than a few colours.
 *
 * Prefer the [lightTheme]/[darkThemeProvider] overload when the two themes are independent values;
 * this one earns its keep when both are derived from the same source:
 *
 * ```
 * KptMaterialTheme(themeBuilder = { isDark ->
 *     kptTheme { colors { primary = if (isDark) Color.Cyan else Color.Blue } }
 * }) { /* content */ }
 * ```
 *
 * @param themeBuilder produces the tokens for the given dark-mode flag. Composable, so it may read
 *   other composition state.
 * @param darkTheme the flag handed to [themeBuilder]. Defaults to the system setting.
 * @param content content with access to both KptTheme and MaterialTheme.
 */
@Composable
fun KptMaterialTheme(
    themeBuilder: @Composable (Boolean) -> KptThemeProvider,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val theme = themeBuilder(darkTheme)
    KptMaterialTheme(
        theme = theme,
        content = content,
    )
}
