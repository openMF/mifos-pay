/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp.navigation.registry.ForkAppShell
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.theme.KptTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * The platform's locale switch, as passed to [ComposeApp]. Provided around [ForkAppShell.root] so a
 * fork root with its own language picker can apply a locale the template's `LanguageConfig` does not
 * list; the default does nothing.
 */
val LocalAppLocaleHandler = staticCompositionLocalOf<(locale: String?) -> Unit> { {} }

/**
 * The application root: installs the theme, the locale's layout direction and the nav graph. Every platform entry
 * point calls this and nothing else.
 */
@Composable
fun ComposeApp(
    updateScreenCapture: (isScreenCaptureAllowed: Boolean) -> Unit,
    handleRecreate: () -> Unit,
    handleThemeMode: (osValue: Int) -> Unit,
    handleAppLocale: (locale: String?) -> Unit,
    onSplashScreenRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = koinViewModel(),
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isScreenCaptureAllowed) {
        updateScreenCapture(uiState.isScreenCaptureAllowed)
    }

    EventsEffect(eventFlow = viewModel.eventFlow) { event ->
        when (event) {
            is AppEvent.ShowToast -> {}
            is AppEvent.UpdateAppLocale -> handleAppLocale(event.localeName)
            is AppEvent.UpdateAppTheme -> handleThemeMode(event.osValue)
            is AppEvent.Recreate -> handleRecreate()
        }
    }

    // Bottom-nav tab-switch retention (per-tab back-stack + scroll) is handled by
    // Navigation's own `saveState = true` / `restoreState = true` in the bottom-nav
    // NavHost; rotation and system-initiated process death ride Android's standard
    // saved-instance-state Bundle. No app-root SaveableStateRegistry override — the
    // platform default is used, so Navigation's Bundle-typed back-stack state is
    // never rejected. Feature modules carry zero retention code.
    // RTL layout direction.
    //
    // `handleAppLocale` above switches the platform locale — on Android via
    // AppCompatDelegate.setApplicationLocales, which (with manifest supportsRtl="true") makes the
    // platform mirror the layout for us. On desktop / iOS / web it calls Locale.setDefault, and
    // that does NOT set Compose's LayoutDirection: an Arabic, Hebrew, Urdu or Persian user would
    // get correctly translated strings inside a left-to-right layout — back arrows pointing the
    // wrong way, every `padding(start=)` on the wrong edge. Providing it here, from the SAME
    // locale the rest of the app uses, makes every target agree. Android is unaffected.
    val layoutDirection = if (isRtlLanguage(uiState.localeName)) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppLocaleHandler provides handleAppLocale,
    ) {
        // Theme mode and root content come from the fork-owned ForkAppShell seam — a fork changes them
        // there, never here (this file is template-owned and replaced by every sync).
        ForkAppShell.wrap {
            KptTheme(
                darkTheme = ForkAppShell.darkThemeOverride ?: uiState.darkTheme,
                androidTheme = uiState.isAndroidTheme,
                useDynamicColor = ForkAppShell.dynamicColorOverride ?: uiState.isDynamicColorsEnabled,
            ) {
                ForkAppShell.root(modifier, onSplashScreenRemoved)
            }
        }
    }
}

/** BCP-47 language subtags written right-to-left. */
private val RTL_LANGUAGES = setOf("ar", "he", "iw", "fa", "ur", "ps", "sd", "ckb", "yi", "dv")

/**
 * True when [languageTag] (e.g. "ar", "ar-EG") is a right-to-left language.
 * A null tag means "follow the system", which the platform resolves itself — treated as LTR here
 * because on Android the platform has already mirrored, and on other targets there is no app
 * override to honour.
 */
private fun isRtlLanguage(languageTag: String?): Boolean =
    languageTag != null && RTL_LANGUAGES.contains(languageTag.substringBefore('-').lowercase())
