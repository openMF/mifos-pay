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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import cmp.navigation.registry.ForkAppShell
import cmp.navigation.rootnav.RootNavScreen
import cmp.navigation.security.AppLockGate
import cmp.navigation.security.PlatformAuthenticatorGate
import cmp.navigation.security.UnauthorizedDialogGate
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.theme.KptTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Top-level entry composable for the template-shape `:cmp-navigation` shell.
 *
 * Hosts four shell-security concerns re-homed from `cmp-shared/.../MifosPayApp.kt`
 * per Phase 2 T4 (`02-topology-reconciliation.md`):
 *
 *  (a) **Background re-auth lock** — [AppLockGate] observes lifecycle
 *      `ON_STOP`/`ON_RESUME`, calls [AppViewModel.markBackgrounded] and
 *      [AppViewModel.checkBackgroundLock] (which reads `AppLockRepository`
 *      + `PasscodeManager` snapshots internally) and emits
 *      [AppEvent.ReAuthRequired]; the [EventsEffect] below forwards to
 *      [onReAuthRequired] (typically wired by the outer shell to
 *      `navController.navigateToReAuthMifosPasscodeScreen()`).
 *  (b) **401 forced-logout dialog** — [UnauthorizedDialogGate] renders
 *      when `state.showUnauthorizedDialog` is `true` (mirrored from
 *      `GlobalAuthManager.isUnauthorized`); the "Ok" tap calls
 *      [AppViewModel.forcedLogout] which resets the flag and emits
 *      [AppEvent.LogoutRequested]. The outer shell's [onSessionLogOut]
 *      callback fans out to the fork's `MifosPayViewModel.logOut()`.
 *  (c) **Start-destination from `userState`** — handled by
 *      `cmp.navigation.rootnav.RootNavViewModel`, which combines the fork's
 *      `UserPreferencesRepository.userInfo` + `GlobalAuthManager.isUnauthorized`
 *      into `RootNavState.{Splash|Auth|UserUnlocked(...)}`. `RootNavScreen`
 *      navigates on state changes. No additional wiring is needed at the
 *      `ComposeApp` layer.
 *  (d) **Biometric composition provider** — [PlatformAuthenticatorGate]
 *      wraps the render subtree in
 *      `org.mifos.authenticator.biometrics.PlatformAuthenticatorCompositionProvider`,
 *      supplying the `LocalBiometricAuthenticator` (aliased) /
 *      `platformAvailableAuthenticationOption` composition locals that every
 *      biometric-consuming descendant (passcode, settings biometrics toggle,
 *      transfer auth gate) reads.
 *
 * Fork-side callbacks ([onSessionLogOut], [onReAuthRequired],
 * [instanceSelectorOverlay]) are supplied by `SharedApp` (in `:cmp-shared`)
 * to keep `:cmp-navigation` free of a reverse-direction dependency on
 * `:cmp-shared` — `cmp-shared` already depends on `cmp-navigation`
 * (`SharedApp` → `ComposeApp`), so composing the fork's `MifosPayViewModel`
 * / `InstanceSelectorScreen` at this layer would create a Gradle cycle.
 * Callbacks let the outer shell inject those without importing back.
 *
 * @param onSessionLogOut Called when [AppEvent.LogoutRequested] fires; wire to
 *        the fork's `MifosPayViewModel.logOut()` (which fan-outs
 *        `UserPreferencesRepository.logOut` + `AppLockRepository.deleteLock`
 *        + `PasscodeManager.logOut`). Default is a no-op so previews /
 *        template-only variants still compile.
 * @param onReAuthRequired Called when [AppEvent.ReAuthRequired] fires; wire to
 *        `navController.navigateToReAuthMifosPasscodeScreen()`. Default is a
 *        no-op; the fork wire-up happens in `SharedApp`.
 * @param instanceSelectorOverlay Composable rendered when
 *        `state.showInstanceSelector` is `true`. Preserves the fork's
 *        `RootNavGraph` bottom-sheet overlay pattern (see T5). Default is
 *        an empty composable so the template-shell has no visible artifact.
 * @param authGraphBuilder NavGraphBuilder-receiver lambda that `SharedApp`
 *        fills with the fork's `loginNavGraph(navController, onShowInstanceSelector = ...)`.
 *        Injected as a builder callback (rather than importing the graph
 *        directly) so `:cmp-navigation` avoids a reverse-direction Gradle
 *        dependency on `:cmp-shared` — same pattern already used by
 *        [instanceSelectorOverlay]. Default no-op keeps template-only variants
 *        (previews / desktop demo) compiling without a login perimeter.
 * @param authGraphRoute Route string the [cmp.navigation.rootnav.RootNavState.Auth]
 *        transition targets — supplied by `SharedApp` as
 *        `org.mifospay.shared.navigation.MifosNavGraph.LOGIN_GRAPH`. Default
 *        `""` = "no login graph wired" (skip the Auth navigation).
 * @param authenticatedContent Composable slot rendered inside the
 *        `AuthenticatedNavbarRoute` bridge — `SharedApp` fills with the fork's
 *        `MifosApp(...)`. The fork composable owns its own Scaffold + 4-tab
 *        bottom nav (HOME/PAYMENTS/FINANCE/HISTORY per
 *        `org.mifospay.shared.utils.TopLevelDestination`) + top-app-bar +
 *        `MifosNavHost` (all ~60 feature destinations wholesale-bridged).
 *        See `authenticatednavbar/AuthenticatedNavbarNavigation.kt` KDoc for
 *        the bridge rationale + per-feature typed-route follow-up. Default
 *        empty composable so the template shell doesn't crash on missing
 *        content.
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
    onSessionLogOut: () -> Unit = {},
    onReAuthRequired: () -> Unit = {},
    instanceSelectorOverlay: @Composable (onDismiss: () -> Unit) -> Unit = { _ -> },
    authGraphBuilder: NavGraphBuilder.(NavController) -> Unit = {},
    authGraphRoute: String = "",
    authenticatedContent: @Composable () -> Unit = {},
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

            // (a) Background re-auth lock — fan out to the outer shell's
            // navigate-to-reauth callback (typically
            // `navController.navigateToReAuthMifosPasscodeScreen()`).
            is AppEvent.ReAuthRequired -> onReAuthRequired()

            // (b) 401 forced-logout — clear the fork session (via the outer
            // shell's `MifosPayViewModel.logOut()` proxy); `RootNavViewModel`
            // separately routes to `RootNavState.Auth` on the same 401 flag.
            is AppEvent.LogoutRequested -> onSessionLogOut()
        }
    }

    // Bottom-nav tab-switch retention (per-tab back-stack + scroll) is handled by
    // Navigation's own `saveState = true` / `restoreState = true` in the bottom-nav
    // NavHost; rotation and system-initiated process death ride Android's standard
    // saved-instance-state Bundle. No app-root SaveableStateRegistry override — the
    // platform default is used, so Navigation's Bundle-typed back-stack state is
    // never rejected. Feature modules carry zero retention code.
    //
    // RTL layout direction (template): `handleAppLocale` switches the platform locale, which mirrors
    // the layout on Android only; providing LocalLayoutDirection from the SAME locale makes every
    // target agree.
    val layoutDirection = if (isRtlLanguage(uiState.localeName)) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        // Fork-owned ForkAppShell seam (template) — wrap + theme overrides. The ROOT stays the fork's
        // RootNavScreen call below: it takes the fork shell's auth-graph / content / logout callbacks,
        // which ForkAppShell.root's (modifier, onSplashScreenRemoved) signature cannot carry.
        ForkAppShell.wrap {
            // (d) Biometric composition provider MUST wrap the theme + nav tree so
            // every descendant that reads `LocalBiometricAuthenticator` (aliased in
            // `PlatformAuthenticatorGate`) resolves — otherwise
            // passcode/settings-biometrics/transfer-auth crashes at compose time.
            PlatformAuthenticatorGate {
                KptTheme(
                    darkTheme = ForkAppShell.darkThemeOverride ?: uiState.darkTheme,
                    androidTheme = uiState.isAndroidTheme,
                    useDynamicColor = ForkAppShell.dynamicColorOverride ?: uiState.isDynamicColorsEnabled,
                ) {
                    // (a) Background re-auth lock — the gate installs a lifecycle
                    // observer that calls `AppViewModel.markBackgrounded()` on
                    // `ON_STOP` and `AppViewModel.checkBackgroundLock()` on
                    // `ON_RESUME`. The VM (not the composable) evaluates the three
                    // qualifying conditions and emits `AppEvent.ReAuthRequired` which
                    // the EventsEffect above routes.
                    AppLockGate(
                        onBackgrounded = { viewModel.markBackgrounded() },
                        onForegrounded = { viewModel.checkBackgroundLock(thresholdMs = 15_000L) },
                    )

                    // (b) 401 forced-logout dialog — render is state-driven so the
                    // gate compiles down to a no-op when the flag is `false`.
                    UnauthorizedDialogGate(
                        showUnauthorizedDialog = uiState.showUnauthorizedDialog,
                        onForcedLogout = { viewModel.forcedLogout() },
                        onDismiss = { viewModel.dismissUnauthorizedDialog() },
                    )

                    RootNavScreen(
                        modifier = modifier,
                        // Same AppViewModel instance here and inside RootNavScreen, so both
                        // EventsEffects subscribe to one SharedFlow. LogoutRequested is handled
                        // ONLY here (onSessionLogOut); ReAuthRequired ONLY in RootNavScreen.
                        appViewModel = viewModel,
                        onSplashScreenRemoved = onSplashScreenRemoved,
                        authGraphBuilder = authGraphBuilder,
                        authGraphRoute = authGraphRoute,
                        authenticatedContent = authenticatedContent,
                        onSessionLogOut = onSessionLogOut,
                    )

                    // T5 — Supabase multi-instance selector overlay, supplied by the outer shell.
                    if (uiState.showInstanceSelector) {
                        instanceSelectorOverlay { viewModel.dismissInstanceSelectorSheet() }
                    }
                }
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
