/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.data.user.UserDataRepository
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData

/**
 * [UserDataRepository] fake for the navigation-layer ViewModel tests.
 *
 * Only the members the navigation ViewModels actually observe carry behaviour; every other member
 * throws, so a ViewModel that starts reading a different preference fails loudly here instead of
 * silently picking up a default value that happens to look right.
 */
internal class FakeUserDataRepository(
    initial: UserData = defaultUserData(),
) : UserDataRepository {

    val current = MutableStateFlow(initial)
    val darkThemeConfig = MutableStateFlow(DarkThemeConfig.FOLLOW_SYSTEM)
    val dynamicColor = MutableStateFlow(false)
    val screenCapture = MutableStateFlow(false)
    val analyticsConsent = MutableStateFlow(false)
    val crashReportingConsent = MutableStateFlow(false)
    val language = MutableStateFlow(LanguageConfig.DEFAULT)

    /** Languages written back through [setLanguage] — the app-specific-language action's sink. */
    val languageWrites = mutableListOf<LanguageConfig>()

    override val userData: StateFlow<UserData> get() = current
    override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = darkThemeConfig
    override val observeDynamicColorPreference: Flow<Boolean> get() = dynamicColor
    override val observeScreenCapturePreference: Flow<Boolean> get() = screenCapture
    override val observeAnalyticsConsent: Flow<Boolean> get() = analyticsConsent
    override val observeCrashReportingConsent: Flow<Boolean> get() = crashReportingConsent
    override suspend fun setAnalyticsConsent(granted: Boolean) {
        analyticsConsent.value = granted
    }
    override suspend fun setCrashReportingConsent(granted: Boolean) {
        crashReportingConsent.value = granted
    }
    override val observeLanguage: Flow<LanguageConfig> get() = language

    override suspend fun setLanguage(language: LanguageConfig) {
        languageWrites += language
        this.language.value = language
    }

    override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> = unused()
    override val authToken: String? get() = unused()
    override val passcode: String get() = unused()
    override suspend fun setThemeBrand(themeBrand: ThemeBrand) = unused()
    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) = unused()
    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) = unused()
    override suspend fun setIsAuthenticated(isAuthenticated: Boolean) = unused()
    override suspend fun setIsUnlocked(isUnlocked: Boolean) = unused()
    override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) = unused()
    override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) = unused()
    override suspend fun setShowOnboarding(showOnboarding: Boolean) = unused()
    override suspend fun setFirstTimeState(firstTimeState: Boolean) = unused()
    override suspend fun setPasscode(passcode: String) = unused()
    override suspend fun clearUserData() = unused()

    private fun unused(): Nothing =
        throw AssertionError("navigation ViewModels must not touch this member")
}

/** A fully-onboarded, authenticated, unlocked user — the "nothing special" baseline. */
internal fun defaultUserData(
    activeUserId: String = "user-1",
    firstTimeUser: Boolean = false,
    isAuthenticated: Boolean = true,
    isUnlocked: Boolean = true,
    passcode: String = "1234",
) = UserData(
    activeUserId = activeUserId,
    themeBrand = ThemeBrand.DEFAULT,
    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    useDynamicColor = false,
    appLanguage = LanguageConfig.DEFAULT,
    showOnboarding = false,
    firstTimeUser = firstTimeUser,
    isAuthenticated = isAuthenticated,
    isUnlocked = isUnlocked,
    passcode = passcode,
    enableScreenCapture = false,
    isPasscodeEnabled = true,
    isBiometricsEnabled = false,
)
