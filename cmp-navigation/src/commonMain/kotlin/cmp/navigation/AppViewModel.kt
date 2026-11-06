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

/**
 * App-root ViewModel: the theme, locale and screen-capture policy every screen inherits.
 *
 * It sits above the nav graph because these are process-wide — a theme change has to reach the root `MaterialTheme`,
 * and a locale change has to reach the root `LayoutDirection`, neither of which any single screen owns.
 */
class AppViewModel(
    private val settingsRepository: UserDataRepository,
    private val garbageCollectionManager: GarbageCollectionManager,
    private val appUpdateManager: AppUpdateManager,
    private val appReviewManager: AppReviewManager,
    private val appReviewPromptStore: AppReviewPromptStore,
) : BaseViewModel<AppState, AppEvent, AppAction>(
    initialState = AppState(
        darkTheme = false,
        isAndroidTheme = false,
        isDynamicColorsEnabled = false,
        isScreenCaptureAllowed = false,
    ),
) {
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
    }
}
