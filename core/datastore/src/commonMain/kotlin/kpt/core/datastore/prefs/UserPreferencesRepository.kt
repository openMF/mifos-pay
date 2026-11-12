/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

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

    /**
     * The whole preference aggregate as hot state. A StateFlow, not a Flow, because almost every screen reads it
     * during composition and needs a value immediately.
     */
    val userData: StateFlow<UserData>

    /**
     * The stored credential, or null when signed out. Synchronous read for a caller that already
     * has one in hand; prefer [observeAuthToken] when the value can change under you.
     */
    val authToken: String?

    /**
     * The credential as a stream, re-emitting on sign-in and sign-out.
     *
     * This is what wires `Authorization` automatically: `AuthHeaderBridge` collects it and writes
     * the formatted header into `RuntimeHeaderStore`, so a token restored from disk at startup is in
     * place before the first request and one obtained at login lands the moment it is written.
     */
    val observeAuthToken: Flow<String?>

    /** The app-lock passcode. Synchronous — the lock screen compares it on each keystroke. */
    val passcode: String

    /** Selected language, re-emitting on change so the locale switch takes effect without a restart. */
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

    /**
     * Persist [token] into the ENCRYPTED store, or clear it when null.
     *
     * Call on sign-in with the value the auth endpoint returned (for Basic, the base64 of
     * `user:password`; for OAuth, the access token) and on sign-out with null. Everything downstream
     * — the header, its wire-format prefix — follows from the access point's declared `auth:`.
     */
    suspend fun setAuthToken(token: String?)

    /** Persists the app language. */
    suspend fun setLanguage(language: LanguageConfig)

    /** Persists the colour brand. */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /** Persists the dark-mode preference. */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /** Persists whether to use platform dynamic colour. */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /** Persists session presence. Does not clear the token — use [setAuthToken] for that. */
    suspend fun setIsAuthenticated(isAuthenticated: Boolean)

    /** Persists whether the app-lock is currently satisfied. Reset on every cold start. */
    suspend fun setIsUnlocked(isUnlocked: Boolean)

    /** Turns passcode lock on or off. */
    suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean)

    /**
     * Turns biometric unlock on or off. Biometrics still need a passcode fallback, so this does not imply
     * [setIsPasscodeEnabled].
     */
    suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean)

    /** Sets whether onboarding still has to be shown. */
    suspend fun setShowOnboarding(showOnboarding: Boolean)

    /** Sets the first-run flag. */
    suspend fun setFirstTimeState(firstTimeState: Boolean)

    /** Persists the app-lock passcode into the ENCRYPTED store. */
    suspend fun setPasscode(passcode: String)

    /** Permits or blocks screenshots and screen recording. */
    suspend fun setScreenCapturePreference(isScreenCaptureEnabled: Boolean)

    /** Records the user's analytics consent — given or withdrawn. */
    suspend fun setAnalyticsConsent(granted: Boolean)

    /** Records the user's crash-reporting consent — given or withdrawn. */
    suspend fun setCrashReportingConsent(granted: Boolean)

    /**
     * Resets every preference to `UserData.DEFAULT` and clears the encrypted entries. Called on logout, alongside
     * `StoreCacheManager.clearAll()`.
     */
    suspend fun clearUserData()
}
