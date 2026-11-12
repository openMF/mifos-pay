/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData

/**
 * Repository interface for managing user preferences with reactive
 * capabilities.
 *
 * This interface provides reactive access to user preferences including
 * theme settings, dark mode configuration, and dynamic color preferences.
 */
interface UserDataRepository {

    /** The whole preference aggregate as hot state, so a screen has a value at first composition. */
    val userData: StateFlow<UserData>

    /**
     * Store5-backed read of the same preferences, as a [ScreenDataStream].
     *
     * [userData] stays for the many call sites that just want the current value (auth, theme
     * bootstrap). Screens consume THIS instead, so preferences render through `ScreenContent`
     * with real Loading / Content / Error states like every other read surface.
     */
    fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData>

    /**
     * The stored credential, or null when signed out. Synchronous read; prefer a stream when the value can change
     * under you.
     */
    val authToken: String?

    /** The app-lock passcode. */
    val passcode: String

    /** Selected language, re-emitting on change. */
    val observeLanguage: Flow<LanguageConfig>

    /** Dark-mode preference as a stream. */
    val observeDarkThemeConfig: Flow<DarkThemeConfig>

    /** Whether to derive the palette from platform dynamic colour. */
    val observeDynamicColorPreference: Flow<Boolean>

    /** Whether screenshots and screen recording are permitted. */
    val observeScreenCapturePreference: Flow<Boolean>

    /** The user's consent to share usage analytics. */
    val observeAnalyticsConsent: Flow<Boolean>

    /** The user's consent to send crash reports. */
    val observeCrashReportingConsent: Flow<Boolean>

    /** Records the user's analytics consent — given or withdrawn. */
    suspend fun setAnalyticsConsent(granted: Boolean)

    /** Records the user's crash-reporting consent — given or withdrawn. */
    suspend fun setCrashReportingConsent(granted: Boolean)

    /** Persists the app language. */
    suspend fun setLanguage(language: LanguageConfig)

    /** Persists the colour brand. */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /** Persists the dark-mode preference. */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /** Persists whether to use platform dynamic colour. */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /** Persists session presence. */
    suspend fun setIsAuthenticated(isAuthenticated: Boolean)

    /** Persists whether the app-lock is currently satisfied. */
    suspend fun setIsUnlocked(isUnlocked: Boolean)

    /** Turns passcode lock on or off. */
    suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean)

    /** Turns biometric unlock on or off. */
    suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean)

    /** Sets whether onboarding still has to be shown. */
    suspend fun setShowOnboarding(showOnboarding: Boolean)

    /** Sets the first-run flag. */
    suspend fun setFirstTimeState(firstTimeState: Boolean)

    /** Persists the app-lock passcode into the encrypted store. */
    suspend fun setPasscode(passcode: String)

    /** Resets every preference to its default and clears the encrypted entries. Called on logout. */
    suspend fun clearUserData()
}
