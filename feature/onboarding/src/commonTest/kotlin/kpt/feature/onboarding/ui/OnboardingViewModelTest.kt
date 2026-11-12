/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.data.user.UserDataRepository
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Locks the onboarding contract (docs.yaml#decisions):
 *
 * - both consents start OFF and stay in memory until a finishing button — browsing writes nothing;
 * - Skip lands on the privacy page and never past it, so consent is never skipped silently;
 * - "Allow and get started" is the affirmative one-tap consent (both ON); "Continue …" saves the switches
 *   EXACTLY as shown. Either marks first run done LAST, so RootNav only moves on once consent is stored;
 * - a failed save leaves the user on the privacy page and does NOT mark first run done.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /** Records every write in order; the members onboarding must not touch throw. */
    private class FakeUserDataRepository(private val failWrites: Boolean = false) : UserDataRepository {
        val writes = mutableListOf<String>()

        override suspend fun setAnalyticsConsent(granted: Boolean) = record("analytics=$granted")
        override suspend fun setCrashReportingConsent(granted: Boolean) = record("crashReports=$granted")
        override suspend fun setFirstTimeState(firstTimeState: Boolean) = record("firstTime=$firstTimeState")

        private fun record(write: String) {
            if (failWrites) error("preferences write failed")
            writes += write
        }

        override val userData: StateFlow<UserData> get() = unused()
        override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> = unused()
        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override val observeAnalyticsConsent: Flow<Boolean> get() = unused()
        override val observeCrashReportingConsent: Flow<Boolean> get() = unused()
        override suspend fun setLanguage(language: LanguageConfig) = unused()
        override suspend fun setThemeBrand(themeBrand: ThemeBrand) = unused()
        override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) = unused()
        override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) = unused()
        override suspend fun setIsAuthenticated(isAuthenticated: Boolean) = unused()
        override suspend fun setIsUnlocked(isUnlocked: Boolean) = unused()
        override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) = unused()
        override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) = unused()
        override suspend fun setShowOnboarding(showOnboarding: Boolean) = unused()
        override suspend fun setPasscode(passcode: String) = unused()
        override suspend fun clearUserData() = unused()

        private fun unused(): Nothing = throw AssertionError("onboarding must not touch this member")
    }

    @Test
    fun startsOnTheFirstPageWithBothConsentsOff() {
        val state = OnboardingViewModel(FakeUserDataRepository()).stateFlow.value

        assertEquals(OnboardingPage.Overview, state.page)
        assertFalse(state.analytics)
        assertFalse(state.crashReports)
    }

    @Test
    fun nextWalksThePagesAndStopsAtPrivacy() = runTest(dispatcher) {
        val vm = OnboardingViewModel(FakeUserDataRepository())
        val seen = mutableListOf(vm.stateFlow.value.page)

        repeat(5) {
            vm.trySendAction(OnboardingAction.Next)
            dispatcher.scheduler.advanceUntilIdle()
            seen += vm.stateFlow.value.page
        }

        assertEquals(
            listOf(
                OnboardingPage.Overview,
                OnboardingPage.Track,
                OnboardingPage.Offline,
                OnboardingPage.Privacy,
                OnboardingPage.Privacy,
                OnboardingPage.Privacy,
            ),
            seen,
        )
    }

    @Test
    fun skipLandsOnPrivacyAndBackReturnsToTheIntro() = runTest(dispatcher) {
        val vm = OnboardingViewModel(FakeUserDataRepository())

        vm.trySendAction(OnboardingAction.Skip)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(OnboardingPage.Privacy, vm.stateFlow.value.page)

        vm.trySendAction(OnboardingAction.Previous)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(OnboardingPage.Offline, vm.stateFlow.value.page)
    }

    @Test
    fun choicesStayInMemoryUntilGetStarted() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = OnboardingViewModel(repo)

        vm.trySendAction(OnboardingAction.Skip)
        vm.trySendAction(OnboardingAction.SetAnalytics(true))
        vm.trySendAction(OnboardingAction.SetCrashReports(true))
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.stateFlow.value.analytics)
        assertTrue(vm.stateFlow.value.crashReports)
        assertEquals(emptyList(), repo.writes, "toggling a switch must not persist anything")
    }

    @Test
    fun allowAndGetStartedStoresBothOnThenMarksFirstRunDoneLast() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = OnboardingViewModel(repo)

        // The one-tap affirmative consent: both switches were OFF, the tap itself turns both on.
        vm.trySendAction(OnboardingAction.Skip)
        vm.trySendAction(OnboardingAction.AllowAll)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("analytics=true", "crashReports=true", "firstTime=false"), repo.writes)
        assertTrue(vm.stateFlow.value.analytics)
        assertTrue(vm.stateFlow.value.crashReports)
    }

    @Test
    fun continueSavesTheChoicesExactlyAsShown() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = OnboardingViewModel(repo)

        // Opposite values, so a swapped write cannot pass.
        vm.trySendAction(OnboardingAction.Skip)
        vm.trySendAction(OnboardingAction.SetAnalytics(true))
        vm.trySendAction(OnboardingAction.ContinueWithChoices)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("analytics=true", "crashReports=false", "firstTime=false"), repo.writes)
    }

    @Test
    fun continueWithoutTouchingTheSwitchesStoresBothOff() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = OnboardingViewModel(repo)

        vm.trySendAction(OnboardingAction.Skip)
        vm.trySendAction(OnboardingAction.ContinueWithChoices)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("analytics=false", "crashReports=false", "firstTime=false"), repo.writes)
    }

    @Test
    fun theSecondaryLabelFollowsTheSwitches() = runTest(dispatcher) {
        val vm = OnboardingViewModel(FakeUserDataRepository())
        assertFalse(vm.stateFlow.value.anyShared)

        vm.trySendAction(OnboardingAction.SetCrashReports(true))
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.anyShared)
    }

    @Test
    fun aFailedSaveStaysOnPrivacyAndDoesNotFinishOnboarding() = runTest(dispatcher) {
        val repo = FakeUserDataRepository(failWrites = true)
        val vm = OnboardingViewModel(repo)

        vm.trySendAction(OnboardingAction.Skip)
        vm.trySendAction(OnboardingAction.AllowAll)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.stateFlow.value
        assertEquals(OnboardingPage.Privacy, state.page)
        assertTrue(state.saveFailed)
        assertFalse(state.saving)
        assertFalse(repo.writes.contains("firstTime=false"))
    }
}
