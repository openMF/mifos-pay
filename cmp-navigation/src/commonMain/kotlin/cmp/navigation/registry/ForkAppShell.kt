/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cmp.navigation.LocalAppLocaleHandler
import cmp.navigation.registry.app.ForkAppRoot

/**
 * ForkAppShell — the FORK-OWNED seam for the app root, so a fork never edits the template's
 * `ComposeApp.kt`.
 *
 * Measured across 21 forks (2026-11-02): 9 edited `ComposeApp.kt`, always for one of two reasons —
 * forcing the theme mode (a dark-only game) or replacing the root content (an app root beside the nav
 * graph, extra root callbacks). Both live here. Brand COLOURS do not: they are in
 * `core/designsystem/.../theme/ForkTheme.kt`, which `KptTheme` applies.
 *
 * Ownership: `owner: fork` — a template sync full-copies `ComposeApp.kt` and never touches this file.
 */
object ForkAppShell {
    /** Forces dark (`true`) or light (`false`) regardless of the user's setting; `null` follows it. */
    val darkThemeOverride: Boolean? = null

    /** Forces dynamic (wallpaper) colour on or off; `null` follows the user's setting. */
    val dynamicColorOverride: Boolean? = null

    /**
     * Wraps the whole app, outside the theme: the place for an SDK's composition-time init (ads, a
     * consent UI) and app-wide `CompositionLocalProvider`s. Forks did this by editing `SharedApp.kt`
     * (5 of 21); the default renders the app unchanged.
     */
    val wrap: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() }

    /**
     * The app's root content inside the theme. The default is the template's navigation root; a fork
     * that needs its own root (or extra root callbacks) renders it here and calls
     * `onSplashScreenRemoved` once its first frame is ready.
     */
    val root: @Composable (modifier: Modifier, onSplashScreenRemoved: () -> Unit) -> Unit =
        { modifier, onSplashScreenRemoved ->
            // The wallet's own root: login, passcode, re-auth lock, 401 dialog, the 4-tab shell.
            ForkAppRoot(
                modifier = modifier,
                handleAppLocale = LocalAppLocaleHandler.current,
                onSplashScreenRemoved = onSplashScreenRemoved,
            )
        }
}
