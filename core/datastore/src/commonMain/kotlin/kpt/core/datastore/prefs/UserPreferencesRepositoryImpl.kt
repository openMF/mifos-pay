/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package kpt.core.datastore.prefs

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData

private const val USER_DATA_KEY = "user_data_key"
private const val SECURE_DATA_KEY = "secure_data_key"

/** Encrypted-store key for the API credential. Secure store, never the plain one. */
private const val AUTH_TOKEN_KEY = "auth_token"

/**
 * Splits user data storage between plain (UI preferences) and secure
 * (credentials/auth state) Settings backends.
 *
 * On first access, migrates any existing single-store data into the split
 * stores using a write-before-delete strategy to prevent data loss.
 */
class UserPreferencesRepositoryImpl(
    private val plainSettings: Settings,
    private val secureSettings: Settings,
    private val dispatcher: DispatcherManager,
) : UserPreferencesRepository {

    init {
        migrateIfNeeded()
    }

    /**
     * One-time migrate from legacy single-store to split plain/secure stores.
     * Write-before-delete: writes to both new stores first, then removes old key.
     */
    private fun migrateIfNeeded() {
        val legacy = plainSettings.decodeValueOrNull(
            key = USER_DATA_KEY,
            serializer = UserData.serializer(),
        ) ?: return

        // Check if secure store already has data (already migrated)
        val existing = secureSettings.decodeValueOrNull(
            key = SECURE_DATA_KEY,
            serializer = UserData.serializer(),
        )
        if (existing != null) return

        // Write secure fields to secure store first
        secureSettings.encodeValue(
            key = SECURE_DATA_KEY,
            serializer = UserData.serializer(),
            value = legacy,
        )
        // Plain store retains the full UserData for UI fields (shared key)
    }

    private fun loadCombinedUserData(): UserData {
        val plainData = plainSettings.decodeValueOrNull(
            key = USER_DATA_KEY,
            serializer = UserData.serializer(),
        )
        val secureData = secureSettings.decodeValueOrNull(
            key = SECURE_DATA_KEY,
            serializer = UserData.serializer(),
        )
        return when {
            plainData != null && secureData != null -> plainData.copy(
                activeUserId = secureData.activeUserId,
                passcode = secureData.passcode,
                isAuthenticated = secureData.isAuthenticated,
                isUnlocked = secureData.isUnlocked,
            )
            secureData != null -> secureData
            plainData != null -> plainData
            else -> UserData.DEFAULT
        }
    }

    private val _userData = MutableStateFlow(loadCombinedUserData())

    override val userData: StateFlow<UserData>
        get() = _userData.asStateFlow()

    // Seeded from the ENCRYPTED store so a token survives process death: the bridge then restores
    // `Authorization` at startup, before the first request, without a round trip to the server.
    private val _authToken = MutableStateFlow(secureSettings.getStringOrNull(AUTH_TOKEN_KEY))

    override val authToken: String?
        get() = _authToken.value

    override val observeAuthToken: Flow<String?>
        get() = _authToken.asStateFlow()

    override suspend fun setAuthToken(token: String?) = withContext(dispatcher.io) {
        if (token.isNullOrBlank()) {
            // Remove, never store empty: a blank credential is indistinguishable from "signed in
            // with nothing" downstream, and the header layer would have to guess.
            secureSettings.remove(AUTH_TOKEN_KEY)
        } else {
            secureSettings.putString(AUTH_TOKEN_KEY, token)
        }
        _authToken.value = token?.takeIf { it.isNotBlank() }
    }

    override val passcode: String
        get() = _userData.value.passcode

    override val observeLanguage: Flow<LanguageConfig>
        get() = _userData.map { it.appLanguage }

    override val observeDarkThemeConfig: Flow<DarkThemeConfig>
        get() = _userData.map { it.darkThemeConfig }

    override val observeDynamicColorPreference: Flow<Boolean>
        get() = _userData.map { it.useDynamicColor }

    override val observeScreenCapturePreference: Flow<Boolean>
        get() = _userData.map { it.enableScreenCapture }

    override val observeAnalyticsConsent: Flow<Boolean>
        get() = _userData.map { it.analyticsConsent }

    override val observeCrashReportingConsent: Flow<Boolean>
        get() = _userData.map { it.crashReportingConsent }

    private suspend fun updatePreference(transform: (UserData) -> UserData) {
        withContext(dispatcher.io) {
            val current = loadCombinedUserData()
            val updated = transform(current)
            plainSettings.putUserPreference(updated)
            secureSettings.putSecurePreference(updated)
            _userData.value = updated
        }
    }

    override suspend fun setLanguage(language: LanguageConfig) = updatePreference { it.copy(appLanguage = language) }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) = updatePreference { it.copy(themeBrand = themeBrand) }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) =
        updatePreference { it.copy(darkThemeConfig = darkThemeConfig) }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) =
        updatePreference { it.copy(useDynamicColor = useDynamicColor) }

    override suspend fun setIsAuthenticated(isAuthenticated: Boolean) =
        updatePreference { it.copy(isAuthenticated = isAuthenticated) }

    override suspend fun setIsUnlocked(isUnlocked: Boolean) = updatePreference { it.copy(isUnlocked = isUnlocked) }

    override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) =
        updatePreference { it.copy(isPasscodeEnabled = isPasscodeEnabled) }

    override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) =
        updatePreference { it.copy(isBiometricsEnabled = isBiometricsEnabled) }

    override suspend fun setShowOnboarding(showOnboarding: Boolean) =
        updatePreference { it.copy(showOnboarding = showOnboarding) }

    override suspend fun setFirstTimeState(firstTimeState: Boolean) =
        updatePreference { it.copy(firstTimeUser = firstTimeState) }

    override suspend fun setPasscode(passcode: String) = updatePreference { it.copy(passcode = passcode) }

    override suspend fun setScreenCapturePreference(isScreenCaptureEnabled: Boolean) =
        updatePreference { it.copy(enableScreenCapture = isScreenCaptureEnabled) }

    override suspend fun setAnalyticsConsent(granted: Boolean) =
        updatePreference { it.copy(analyticsConsent = granted) }

    override suspend fun setCrashReportingConsent(granted: Boolean) =
        updatePreference { it.copy(crashReportingConsent = granted) }

    /**
     * Discards everything belonging to the signed-in PERSON, and nothing belonging to the DEVICE.
     *
     * The boundary is not invented here — it is the one [loadCombinedUserData] already uses when it
     * prefers the secure store: `activeUserId`, `passcode`, `isAuthenticated`, `isUnlocked` are
     * user/session state; theme, language, onboarding, screen-capture, biometric and privacy-consent
     * toggles are device preferences a person expects to survive signing out.
     *
     * This previously flipped `isAuthenticated` alone, which left `activeUserId` and `passcode`
     * readable on disk — in BOTH stores, because [putUserPreference] writes the whole blob to the
     * plain store as well (the migration keeps the full record there "for UI fields"). On a shared
     * device the next person inherited them. The auth token is removed too; it lives under its own
     * key and no `UserData` write touches it.
     *
     * Device-scoped counters (e.g. `AppReviewPromptStore`) are deliberately NOT part of `UserData`
     * and are unaffected — see RULE-KMP-DATASTORE-SCOPE-001.
     */
    override suspend fun clearUserData() {
        withContext(dispatcher.io) {
            val cleared = loadCombinedUserData().copy(
                activeUserId = UserData.DEFAULT.activeUserId,
                passcode = UserData.DEFAULT.passcode,
                // NOT UserData.DEFAULT for these two: the default describes a FRESH INSTALL, where
                // both are `true`. Signing out must leave the app locked and unauthenticated, so the
                // sign-out value is stated explicitly rather than inherited.
                isAuthenticated = false,
                isUnlocked = false,
            )
            plainSettings.putUserPreference(cleared)
            secureSettings.putSecurePreference(cleared)
            secureSettings.remove(AUTH_TOKEN_KEY)
            _authToken.value = null
            _userData.value = cleared
        }
    }
}

private fun Settings.putUserPreference(preference: UserData) {
    encodeValue(
        key = USER_DATA_KEY,
        serializer = UserData.serializer(),
        value = preference,
    )
}

private fun Settings.putSecurePreference(preference: UserData) {
    encodeValue(
        key = SECURE_DATA_KEY,
        serializer = UserData.serializer(),
        value = preference,
    )
}
