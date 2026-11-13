/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptThemeProviderImpl
import kpt.core.base.designsystem.theme.LocalMotion
import kpt.core.base.designsystem.theme.Motion
import kpt.core.base.designsystem.toKptColorScheme
import kpt.core.base.designsystem.toKptTypography
import kpt.core.base.ui.screen.LocalScreenStateDefaults
import kpt.core.store.config.appScreenStateDefaults

/**
 * The main theme composable for the application.
 *
 * This composable uses KptMaterialTheme under the hood to provide seamless integration
 * between KptTheme design tokens and Material3 theming system.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system preference.
 * @param androidTheme Whether to use Android-specific theming. Defaults to false.
 * @param useDynamicColor Whether dynamic theming should be displayed. Defaults to false.
 * @param content The composable content that will have access to both KptTheme and MaterialTheme.
 */
@Composable
fun KptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    androidTheme: Boolean = false,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        useDynamicColor -> platformColorScheme(darkTheme, true)
        androidTheme -> if (darkTheme) darkColorScheme() else lightColorScheme()
        else -> if (darkTheme) darkScheme else lightScheme
    }.toKptColorScheme()

    val mifosTypography = Typography().toKptTypography(fontFamily)

    val themeProvider = KptThemeProviderImpl(
        colors = colorScheme,
        typography = mifosTypography,
    )

    val screenStateDefaults = appScreenStateDefaults()
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
        // semantic finance colors, motion specs, spacing scale, and elevation tiers
        // without per-call wiring. Forks override any subset via
        // `CompositionLocalProvider(LocalFinanceColors provides ...)` etc.
        //
        // Also wires LocalScreenStateDefaults so every ScreenContent /
        // PagingScreenContent automatically picks up the fork's branded empty /
        // error / no-network / loading visuals. Customize in
        // core/store/KptScreenStateDefaults.kt — that's the single fork seam.
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
        }
    }
}

/**
 * Resolves the active colour scheme per platform.
 *
 * [dynamicColor] is honoured only where the OS supplies one (Android 12+); every other target
 * falls back to [lightScheme]/[darkScheme], so a caller can request it unconditionally.
 */
@Composable
expect fun platformColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme
