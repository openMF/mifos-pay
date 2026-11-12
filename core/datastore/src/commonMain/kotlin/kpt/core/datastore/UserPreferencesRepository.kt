/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
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
interface UserPreferencesRepository {

    /** Current [UserData] (theme, language, auth and onboarding flags). */
    val userData: StateFlow<UserData>

    /** Stored auth token, or null. */
    val authToken: String?

    /** Stored app passcode. */
    val passcode: String

    /** Stream of the selected language. */
    val observeLanguage: Flow<LanguageConfig>

    /** Stream of the dark-theme setting. */
    val observeDarkThemeConfig: Flow<DarkThemeConfig>

    /** Stream of whether dynamic color is enabled. */
    val observeDynamicColorPreference: Flow<Boolean>

    /** Stream of whether screen capture is allowed. */
    val observeScreenCapturePreference: Flow<Boolean>

    /** Persists the selected [language]. */
    suspend fun setLanguage(language: LanguageConfig)

    /** Persists the selected [themeBrand]. */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /** Persists the [darkThemeConfig]. */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /** Persists whether to use dynamic color. */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /** Persists whether the user is authenticated. */
    suspend fun setIsAuthenticated(isAuthenticated: Boolean)

    /** Persists whether the app is currently unlocked. */
    suspend fun setIsUnlocked(isUnlocked: Boolean)

    /** Persists whether passcode protection is enabled. */
    suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean)

    /** Persists whether biometric unlock is enabled. */
    suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean)

    /** Persists whether onboarding should be shown. */
    suspend fun setShowOnboarding(showOnboarding: Boolean)

    /** Persists the first-time-use flag. */
    suspend fun setFirstTimeState(firstTimeState: Boolean)

    /** Persists the app [passcode]. */
    suspend fun setPasscode(passcode: String)

    /** Persists whether screen capture is allowed. */
    suspend fun setScreenCapturePreference(isScreenCaptureEnabled: Boolean)

    /** Clears all stored user data. */
    suspend fun clearUserData()
}
