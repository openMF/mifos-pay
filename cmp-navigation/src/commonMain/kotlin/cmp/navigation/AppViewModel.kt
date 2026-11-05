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

import androidx.lifecycle.viewModelScope
import cmp.navigation.AppAction.Internal.DynamicColorsUpdate
import cmp.navigation.AppAction.Internal.ScreenCaptureUpdate
import cmp.navigation.AppAction.Internal.UnauthorizedFlagChange
import com.mobilebytelabs.kmptoolkit.appreview.AppReview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.platform.garbage.GarbageCollectionManager
import kpt.core.base.platform.review.AppReviewManager
import kpt.core.base.platform.update.AppUpdateManager
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.user.UserDataRepository
import kpt.core.datastore.prefs.AppReviewPromptStore
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.platform.config.AppReviewConfig
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStep
import org.mifospay.core.common.GlobalAuthManager
import org.mifospay.core.data.repository.AppLockRepository
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Root app-shell ViewModel scoped to [ComposeApp].
 *
 * Owns the four kinds of state the shell needs to make its cross-cutting
 * decisions:
 *
 *  - **Theme / display prefs** — the original template concerns
 *    (dark-theme / dynamic-color / screen-capture / locale), unchanged.
 *  - **401 forced-logout flag** ([AppState.showUnauthorizedDialog]) — mirrors
 *    the fork's [GlobalAuthManager.isUnauthorized]. `ComposeApp` reads this
 *    flag and renders [cmp.navigation.security.UnauthorizedDialogGate] when
 *    it flips `true`; on the user's "Ok" tap it calls [forcedLogout], which
 *    resets the flag and emits [AppEvent.LogoutRequested] for the outer
 *    shell (`SharedApp` in `:cmp-shared`) to fan out to the fork's
 *    `MifosPayViewModel.logOut()`.
 *  - **Background-lock timer** ([lastStopMark]) — monotonic-clock backing
 *    for the 15-second re-auth gate re-homed from `MifosPayApp.kt`. The
 *    lifecycle observer inside [cmp.navigation.security.AppLockGate] calls
 *    [markBackgrounded] on `ON_STOP` and [checkBackgroundLock] on `ON_RESUME`;
 *    a qualifying resume emits [AppEvent.ReAuthRequired] which `ComposeApp`
 *    then routes to `navigateToReAuthMifosPasscodeScreen`.
 *  - **Instance-selector overlay** ([AppState.showInstanceSelector]) — one-shot
 *    bottom-sheet flag preserving the fork's `RootNavGraph`
 *    `showInstanceSelector = true/false` pattern (see T5); the multi-tap
 *    gesture on `LoginScreen` calls [showInstanceSelectorSheet] and the
 *    sheet's dismiss callback calls [dismissInstanceSelectorSheet].
 *
 * Behaviors (a) background-lock, (b) 401 forced-logout, (c) start-destination
 * (`cmp.navigation.rootnav.RootNavViewModel` already handles this off
 * `UserPreferencesRepository.userInfo` + `GlobalAuthManager.isUnauthorized`),
 * and (d) biometric composition provider (wrap in
 * [cmp.navigation.security.PlatformAuthenticatorGate]) are the four shell
 * concerns re-homed per Phase 2 T4.
 *
 * DI dependencies added in this rev — [AppLockRepository] and [PasscodeManager]
 * — resolve via the fork's `RepositoryModule` + `MifosPasscodeModule`; Phase 2
 * T6/T7 wire those into `cmp.navigation.di.KoinModules.allModules`. Until then
 * a fresh app start surfaces a Koin `NoBeanDefFound` at composition, which is
 * the correct signal that DI reconciliation is incomplete (not a masked stub —
 * see RULE-IMPL-STUB-DETECTION-001).
 *
 * Template concerns merged in at the 90ff6da6 sync: the active locale ([AppState.localeName], drives the root
 * LayoutDirection), in-app update and the app-review prompt.
 */
@OptIn(ExperimentalTime::class)
class AppViewModel(
    private val settingsRepository: UserDataRepository,
    private val garbageCollectionManager: GarbageCollectionManager,
    private val appUpdateManager: AppUpdateManager,
    private val appReviewManager: AppReviewManager,
    private val appReviewPromptStore: AppReviewPromptStore,
    private val appLockRepository: AppLockRepository,
    private val passcodeManager: PasscodeManager,
) : BaseViewModel<AppState, AppEvent, AppAction>(
    initialState = AppState(
        darkTheme = false,
        isAndroidTheme = false,
        isDynamicColorsEnabled = false,
        isScreenCaptureAllowed = false,
        showUnauthorizedDialog = false,
        showInstanceSelector = false,
    ),
) {
    /**
     * Monotonic-clock mark of the most recent `ON_STOP`. `null` means either
     * "no stop recorded yet" or "already consumed on a prior `ON_RESUME`" —
     * both cases mean the next `ON_RESUME` must skip the re-auth check.
     * Monotonic (not wall-clock) is used to defeat device-clock rewind attacks.
     */
    private var lastStopMark: TimeSource.Monotonic.ValueTimeMark? = null

    init {
        // Configure the store listing ONCE, before anything can ask for a review.
        //
        // `AppReviewManagerImpl`'s KDoc names this as the fork step and nothing performed it, so
        // `canRequestReview` was permanently false on desktop and web (the two targets with no
        // native review flow) while Android/iOS/macOS masked the gap behind theirs. It lives here
        // rather than in each platform entry point because the ids are commonMain data and the
        // toolkit resolves the target itself — an androidMain/iosMain pair would be four copies of
        // one call, three of which a fork would forget on the next platform it adds.
        AppReview.configure(AppReviewConfig.storeListing)

        // App-update check moved off MainActivity so every platform gets it.
        //
        // It ran only on Android, inside `setContent`, keyed on connectivity — so desktop and iOS
        // never checked at all. `checkForAppUpdate()` already returns `UpdateOutcome.NotSupported`
        // where there is no update mechanism, which is the honest answer and costs one call. The
        // Android copy was REMOVED rather than left in place: two checks per launch on one platform
        // is not a safety net, it is a second thing to keep in sync. MainActivity keeps only
        // `checkForResumeUpdateState()` in onResume, which is a real Android lifecycle concern.
        viewModelScope.launch { appUpdateManager.checkForAppUpdate() }

        // Review prompt, on the same automatic footing as the update check.
        //
        // Template-level on purpose: a fork inherits both by existing, with nothing to call. That is
        // the whole point — the previous arrangement configured the store listing here but left the
        // only prompt behind a settings row, so a fork that removed or never rendered that row
        // shipped a review capability that could not fire.
        //
        // Three gates, in widening order of cost to evaluate: the fork's own `enabled` flag and the
        // thresholds (generated from app-profile), then whether this target can reach a review at
        // all, and finally the OS, which rate-limits the native flow independently of anything here.
        // `recordLaunch()` runs regardless, because the counters must advance on launches that do
        // NOT prompt — otherwise `min_launches` could never be reached.
        viewModelScope.launch { maybePromptForReview() }

        settingsRepository
            .observeDarkThemeConfig
            .onEach { trySendAction(AppAction.Internal.ThemeUpdate(it)) }
            .launchIn(viewModelScope)

        settingsRepository
            .observeDynamicColorPreference
            .onEach { trySendAction(DynamicColorsUpdate(it)) }
            .launchIn(viewModelScope)

        settingsRepository
            .observeScreenCapturePreference
            .onEach { trySendAction(ScreenCaptureUpdate(it)) }
            .launchIn(viewModelScope)

        settingsRepository
            .observeLanguage
            .distinctUntilChanged()
            // Mirror the active locale into STATE as well as emitting the platform event.
            // The event drives the per-platform locale switch (setApplicationLocales on Android,
            // Locale.setDefault elsewhere); the state drives Compose's LayoutDirection at the app
            // root. Both are needed: Locale.setDefault does NOT set LayoutDirection, so without
            // this an RTL language renders translated strings inside a left-to-right layout on
            // desktop / iOS / web. Android is already correct via setApplicationLocales.
            .onEach { language -> mutableStateFlow.update { it.copy(localeName = language.localeName) } }
            .map { AppEvent.UpdateAppLocale(it.localeName) }
            .onEach(::sendEvent)
            .launchIn(viewModelScope)

        // Fork's 401 flag → local shell state. Behavior (b), plumbed via a
        // trySendAction so state mutation stays inside handleAction.
        // NOTE: no `.distinctUntilChanged()` — StateFlow already conflates equal values
        // (Operator Fusion; the operator was deprecated to error in Kotlin 2.x on StateFlow).
        GlobalAuthManager.isUnauthorized
            .onEach { trySendAction(UnauthorizedFlagChange(it)) }
            .launchIn(viewModelScope)
    }

    /**
     * Ask for a review if this launch is the one the policy has been waiting for.
     *
     * Silent by design when it declines: every branch here is a legitimate "not now", and a user who
     * is not being asked has nothing to be told.
     */
    private suspend fun maybePromptForReview() {
        val state = appReviewPromptStore.recordLaunch()

        val due = AppReviewConfig.shouldPromptForReview(
            launchCount = state.launchCount,
            daysSinceInstall = state.daysSinceInstall,
            daysSinceLastPrompt = state.daysSinceLastPrompt,
        )
        if (!due || !appReviewManager.canRequestReview) return

        appReviewManager.promptForReview()
        // Stamped even though the native flow may have decided to show nothing. We asked; the
        // cooldown starts. Stamping only on a confirmed display is not an option — neither store
        // reports it — and treating "not shown" as "not asked" would retry on every launch.
        appReviewPromptStore.recordPromptShown()
    }

    override fun handleAction(action: AppAction) {
        when (action) {
            is AppAction.AppSpecificLanguageUpdate -> handleAppSpecificLanguageUpdate(action)

            is ScreenCaptureUpdate -> handleScreenCaptureUpdate(action)

            is AppAction.Internal.ThemeUpdate -> handleAppThemeUpdated(action)

            is DynamicColorsUpdate -> handleDynamicColorsUpdate(action)

            is AppAction.Internal.CurrentUserStateChange -> handleCurrentUserStateChange()

            is AppAction.Internal.UserUnlockStateChange -> handleUserUnlockStateChange()

            is UnauthorizedFlagChange -> handleUnauthorizedFlagChange(action)
        }
    }

    private fun handleAppSpecificLanguageUpdate(action: AppAction.AppSpecificLanguageUpdate) {
        viewModelScope.launch {
            settingsRepository.setLanguage(action.appLanguage)
        }
    }

    private fun handleScreenCaptureUpdate(action: ScreenCaptureUpdate) {
        mutableStateFlow.update { it.copy(isScreenCaptureAllowed = action.isScreenCaptureEnabled) }
    }

    private fun handleAppThemeUpdated(action: AppAction.Internal.ThemeUpdate) {
        mutableStateFlow.update {
            it.copy(darkTheme = action.theme == DarkThemeConfig.DARK)
        }
        sendEvent(AppEvent.UpdateAppTheme(osValue = action.theme.osValue))
    }

    private fun handleDynamicColorsUpdate(action: DynamicColorsUpdate) {
        mutableStateFlow.update { it.copy(isDynamicColorsEnabled = action.isDynamicColorsEnabled) }
    }

    private fun handleUserUnlockStateChange() {
        recreateUiAndGarbageCollect()
    }

    private fun handleCurrentUserStateChange() {
        recreateUiAndGarbageCollect()
    }

    private fun recreateUiAndGarbageCollect() {
        sendEvent(AppEvent.Recreate)
        garbageCollectionManager.tryCollect()
    }

    private fun handleUnauthorizedFlagChange(action: UnauthorizedFlagChange) {
        mutableStateFlow.update { it.copy(showUnauthorizedDialog = action.isUnauthorized) }
    }

    /**
     * Called by [cmp.navigation.security.AppLockGate] on
     * `Lifecycle.Event.ON_STOP`. Records the monotonic-clock instant so the
     * paired [checkBackgroundLock] on the next `ON_RESUME` can compute the
     * elapsed background duration.
     */
    fun markBackgrounded() {
        lastStopMark = TimeSource.Monotonic.markNow()
    }

    /**
     * Called by [cmp.navigation.security.AppLockGate] on
     * `Lifecycle.Event.ON_RESUME`.
     *
     * If **all** of the following hold, emits [AppEvent.ReAuthRequired]:
     *  - elapsed background duration exceeds [thresholdMs] milliseconds,
     *  - [appLockRepository]`.isAppLocked()` returned `false` — session
     *    exists and was actively unlocked (a `null` return means "no session
     *    yet" and must NOT trigger re-auth, matching the fork's
     *    `isAppLocked()?.let { if (!it) ... }` pattern),
     *  - [passcodeManager]`.state.value.passcodeStep == PasscodeStep.Enter`
     *    — the passcode UI is in unlock mode, not creating / changing.
     *
     * The stored mark is cleared unconditionally so a subsequent `ON_RESUME`
     * without a paired `ON_STOP` won't re-fire.
     *
     * @param thresholdMs Background-duration threshold in milliseconds
     *        (default `15_000L` — matches the fork's `MifosPayApp` 15 s gate).
     */
    fun checkBackgroundLock(thresholdMs: Long = 15_000L) {
        val mark = lastStopMark
        lastStopMark = null
        if (mark != null) {
            val elapsedMs = mark.elapsedNow().inWholeMilliseconds
            val appLocked = appLockRepository.isAppLocked()
            if (elapsedMs > thresholdMs &&
                appLocked != null &&
                !appLocked &&
                passcodeManager.state.value.passcodeStep == PasscodeStep.Enter
            ) {
                sendEvent(AppEvent.ReAuthRequired)
            }
        }
    }

    /**
     * Called by [cmp.navigation.security.UnauthorizedDialogGate]'s "Ok" tap.
     * Resets [GlobalAuthManager.isUnauthorized] (which in turn flips
     * [AppState.showUnauthorizedDialog] back to `false` via the observer
     * wired in [init]) and emits [AppEvent.LogoutRequested] so the outer
     * shell (`SharedApp` in `:cmp-shared`, which owns `MifosPayViewModel`) can
     * fan out to `MifosPayViewModel.logOut()` + navigate to the login graph.
     * `RootNavViewModel` independently routes to `RootNavState.Auth` on the
     * same flag change — the event is only responsible for the imperative
     * session-clear + explicit nav pop.
     */
    fun forcedLogout() {
        GlobalAuthManager.reset()
        sendEvent(AppEvent.LogoutRequested)
    }

    /**
     * Dismisses the 401 dialog without logging out (kept for tests /
     * accidental-dismiss handling; the fork's `MifosPayApp` treats dismiss as
     * a no-op).
     */
    fun dismissUnauthorizedDialog() {
        mutableStateFlow.update { it.copy(showUnauthorizedDialog = false) }
    }

    /**
     * Show the fork's Supabase multi-instance selector as a bottom-sheet
     * overlay. Triggered by the login screen's multi-tap gesture (see
     * `feature/auth/.../LoginScreen.kt` `detectMultiTapGesture`) via a
     * lambda plumbed through the login graph.
     */
    fun showInstanceSelectorSheet() {
        mutableStateFlow.update { it.copy(showInstanceSelector = true) }
    }

    /** Called by `InstanceSelectorScreen.onDismiss` — hides the overlay. */
    fun dismissInstanceSelectorSheet() {
        mutableStateFlow.update { it.copy(showInstanceSelector = false) }
    }
}

/** What the app root renders with. */
data class AppState(
    /** Whether to build the dark colour scheme. */
    val darkTheme: Boolean,
    /** Whether the Android-green brand palette is selected. */
    val isAndroidTheme: Boolean,
    /** Whether to derive the palette from platform dynamic colour. */
    val isDynamicColorsEnabled: Boolean,
    /** Whether screenshots and screen recording are permitted. */
    val isScreenCaptureAllowed: Boolean,
    // Fork shell-security state re-homed from MifosPayApp.kt (Phase 2 T4).
    val showUnauthorizedDialog: Boolean = false,
    val showInstanceSelector: Boolean = false,
    /** Active app locale as a BCP-47 tag (null = follow the system). Drives Compose's
     *  LayoutDirection at the app root so RTL languages mirror on desktop / iOS / web,
     *  where Locale.setDefault alone does not. */
    val localeName: String? = null,
)

/** One-shot effects the root acts on — each needs a platform call, not a state change. */
sealed interface AppEvent {
    /** Recreate the activity/window. The only way some platforms apply a locale change. */
    data object Recreate : AppEvent

    /**
     * Show a transient message.
     *
     * @property message the text to show.
     */
    data class ShowToast(val message: String) : AppEvent

    /** Apply a new locale at the platform level. */
    data class UpdateAppLocale(
        /** BCP-47 tag, or null to follow the system. */
        val localeName: String?,
    ) : AppEvent

    /** Hand the OS its own night-mode constant, so the system UI matches the app. */
    data class UpdateAppTheme(
        /** The platform night-mode constant — see `DarkThemeConfig.osValue`. */
        val osValue: Int,
    ) : AppEvent

    /**
     * Emitted when [AppViewModel.checkBackgroundLock] qualifies a resume as
     * "long background + session actively unlocked + passcode in unlock step".
     * The outer shell (or `RootNavScreen`) routes to the re-auth passcode
     * screen.
     */
    data object ReAuthRequired : AppEvent

    /**
     * Emitted from [AppViewModel.forcedLogout] after resetting the 401 flag.
     * The outer shell calls the fork's `MifosPayViewModel.logOut()` and
     * navigates to the login graph in response.
     */
    data object LogoutRequested : AppEvent
}

/** What the root can be asked to do. */
sealed interface AppAction {
    /**
     * The user picked a language.
     *
     * @property appLanguage the language the user picked.
     */
    data class AppSpecificLanguageUpdate(val appLanguage: LanguageConfig) : AppAction

    /** Actions the ViewModel raises for itself from its own collectors — never dispatched by the UI. */
    sealed class Internal : AppAction {

        /** The signed-in user changed. */
        data object CurrentUserStateChange : Internal()

        /**
         * The screen-capture preference changed, so the window flag has to be re-applied.
         *
         * Raised by the preference collector rather than by the settings screen: the flag lives on the app's window,
         * which the settings screen does not own, and it must be re-applied on every process start as well as on every
         * change.
         */
        data class ScreenCaptureUpdate(
            /** The new value. */
            val isScreenCaptureEnabled: Boolean,
        ) : Internal()

        /** The dark-mode preference changed. */
        data class ThemeUpdate(
            /** The new preference. */
            val theme: DarkThemeConfig,
        ) : Internal()

        /** The app-lock was satisfied or re-armed. */
        data object UserUnlockStateChange : Internal()

        /** The dynamic-colour preference changed. */
        data class DynamicColorsUpdate(
            /** The new value. */
            val isDynamicColorsEnabled: Boolean,
        ) : Internal()

        /**
         * Mirrors [GlobalAuthManager.isUnauthorized] into
         * [AppState.showUnauthorizedDialog].
         */
        data class UnauthorizedFlagChange(
            val isUnauthorized: Boolean,
        ) : Internal()
    }
}
