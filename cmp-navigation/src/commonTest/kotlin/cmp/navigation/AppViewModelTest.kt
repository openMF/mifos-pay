/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import cmp.navigation.testing.FakeUserDataRepository
import com.mobilebytelabs.kmptoolkit.appreview.AppReviewCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.platform.garbage.GarbageCollectionManager
import kpt.core.base.platform.review.AppReviewManager
import kpt.core.base.platform.update.AppUpdateManager
import kpt.core.base.platform.update.UpdateOutcome
import kpt.core.datastore.prefs.AppReviewPromptState
import kpt.core.datastore.prefs.AppReviewPromptStore
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.platform.config.AppReviewConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the [AppViewModel] contract — the app-root ViewModel that turns preference changes into
 * theme / locale / screen-capture state plus the platform events that apply them.
 *
 * The load-bearing case is the locale one. A language change must update BOTH `state.localeName`
 * AND emit [AppEvent.UpdateAppLocale]: the event drives the per-platform locale switch, while the
 * state drives Compose's `LayoutDirection` at the app root. Dropping the state half is invisible in
 * LTR and silently renders every RTL language's translated strings inside a left-to-right layout
 * on desktop / iOS / web — so [languageChangeUpdatesBothStateAndEvent] asserts both halves.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class RecordingAppUpdateManager : AppUpdateManager {
        var checks = 0
            private set

        override suspend fun checkForAppUpdate(): UpdateOutcome {
            checks++
            return UpdateOutcome.UpToDate
        }

        override suspend fun checkForResumeUpdateState(): UpdateOutcome = UpdateOutcome.UpToDate

        override fun isSupported(): Boolean = true
    }

    private class RecordingAppReviewManager(
        override val canRequestReview: Boolean = true,
    ) : AppReviewManager {
        var prompts = 0
            private set

        override val capabilities: AppReviewCapabilities
            get() = AppReviewCapabilities(nativeInAppReview = canRequestReview, storeListing = canRequestReview)

        override suspend fun promptForReview() {
            prompts++
        }

        override fun promptForCustomReview() = Unit
    }

    /**
     * Returns [state] verbatim rather than counting, so a test can place the app at any point in the
     * policy window without simulating hundreds of launches. Store-side counting is covered by
     * `AppReviewPromptStoreTest`; what belongs here is what the ViewModel DOES with the answer.
     */
    private class FakeAppReviewPromptStore(
        private val state: AppReviewPromptState,
    ) : AppReviewPromptStore {
        var launches = 0
            private set
        var promptsRecorded = 0
            private set

        override fun recordLaunch(): AppReviewPromptState {
            launches++
            return state
        }

        override fun recordPromptShown() {
            promptsRecorded++
        }
    }

    /** Maximally eligible: only [AppReviewConfig.ENABLED] can still veto. */
    private fun eligible() = AppReviewPromptState(Int.MAX_VALUE, Int.MAX_VALUE, null)

    /** Freshly installed: below every threshold a fork could set above zero. */
    private fun brandNew() = AppReviewPromptState(launchCount = 1, daysSinceInstall = 0, daysSinceLastPrompt = null)

    private class RecordingGarbageCollector : GarbageCollectionManager {
        var collections = 0
            private set

        override fun tryCollect() {
            collections++
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.drain() {
        repeat(3) {
            runCurrent()
            advanceUntilIdle()
        }
    }

    @Test
    fun darkThemeConfigDrivesTheDarkThemeFlagAndTheOsEvent() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val gc = RecordingGarbageCollector()
        val events = mutableListOf<AppEvent>()
        val vm = AppViewModel(
            repo,
            gc,
            RecordingAppUpdateManager(),
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        repo.darkThemeConfig.value = DarkThemeConfig.DARK
        drain()

        assertTrue(vm.stateFlow.value.darkTheme)
        assertTrue(
            events.contains(AppEvent.UpdateAppTheme(osValue = DarkThemeConfig.DARK.osValue)),
            "events were $events",
        )
    }

    @Test
    fun onlyTheDarkConfigCountsAsDark() = runTest(dispatcher) {
        // FOLLOW_SYSTEM and LIGHT are both "not dark" here — the OS resolves FOLLOW_SYSTEM via the
        // emitted osValue, so treating it as dark in state would double-apply the preference.
        val repo = FakeUserDataRepository()
        val vm = AppViewModel(
            repo,
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        drain()

        repo.darkThemeConfig.value = DarkThemeConfig.LIGHT
        drain()
        assertEquals(false, vm.stateFlow.value.darkTheme)

        repo.darkThemeConfig.value = DarkThemeConfig.FOLLOW_SYSTEM
        drain()
        assertEquals(false, vm.stateFlow.value.darkTheme)
    }

    @Test
    fun dynamicColorAndScreenCapturePreferencesReachState() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = AppViewModel(
            repo,
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        drain()

        repo.dynamicColor.value = true
        repo.screenCapture.value = true
        drain()

        assertTrue(vm.stateFlow.value.isDynamicColorsEnabled)
        assertTrue(vm.stateFlow.value.isScreenCaptureAllowed)
    }

    @Test
    fun languageChangeUpdatesBothStateAndEvent() = runTest(dispatcher) {
        // Both halves are required — see the class doc. Asserting only the event would let an RTL
        // layout regression through.
        val repo = FakeUserDataRepository()
        val events = mutableListOf<AppEvent>()
        val vm = AppViewModel(
            repo,
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        repo.language.value = LanguageConfig.HINDI
        drain()

        assertEquals(LanguageConfig.HINDI.localeName, vm.stateFlow.value.localeName)
        assertTrue(
            events.contains(AppEvent.UpdateAppLocale(LanguageConfig.HINDI.localeName)),
            "events were $events",
        )
    }

    @Test
    fun appSpecificLanguageUpdateWritesBackToPreferences() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = AppViewModel(
            repo,
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        drain()

        // ENGLISH, not a translation: LanguageConfig is generated per fork from its enabled locales, and
        // English is the one every fork ships — a fork without Spanish would not compile this source set.
        vm.trySendAction(AppAction.AppSpecificLanguageUpdate(LanguageConfig.ENGLISH))
        drain()

        assertEquals(listOf(LanguageConfig.ENGLISH), repo.languageWrites)
    }

    @Test
    fun userStateChangesRecreateTheUiAndCollectGarbage() = runTest(dispatcher) {
        // Both arms exist so a locale/user switch tears down the old Compose tree AND releases it;
        // emitting Recreate without collecting leaks the previous tree across every switch.
        val gc = RecordingGarbageCollector()
        val events = mutableListOf<AppEvent>()
        val vm = AppViewModel(
            FakeUserDataRepository(),
            gc,
            RecordingAppUpdateManager(),
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        vm.trySendAction(AppAction.Internal.CurrentUserStateChange)
        drain()
        vm.trySendAction(AppAction.Internal.UserUnlockStateChange)
        drain()

        assertEquals(2, events.count { it == AppEvent.Recreate }, "events were $events")
        assertEquals(2, gc.collections)
    }

    @Test
    fun checksForAnUpdateOnEveryLaunch() = runTest(dispatcher) {
        // The whole point of moving this off MainActivity: a fork gets it on every platform by
        // existing, with nothing to call.
        val updates = RecordingAppUpdateManager()
        AppViewModel(
            FakeUserDataRepository(),
            RecordingGarbageCollector(),
            updates,
            RecordingAppReviewManager(),
            FakeAppReviewPromptStore(brandNew()),
        )
        advanceUntilIdle()

        assertEquals(1, updates.checks)
    }

    @Test
    fun promptsForReviewWhenThePolicyIsSatisfied() = runTest(dispatcher) {
        val review = RecordingAppReviewManager(canRequestReview = true)
        val store = FakeAppReviewPromptStore(eligible())
        AppViewModel(
            FakeUserDataRepository(),
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            review,
            store,
        )
        advanceUntilIdle()

        // Asserted against ENABLED rather than a literal: the thresholds are GENERATED from
        // app-profile, so a fork that ships `in_app_review.enabled: false` must see zero prompts and
        // still pass this test unchanged.
        val expected = if (AppReviewConfig.ENABLED) 1 else 0
        assertEquals(expected, review.prompts)
        assertEquals(expected, store.promptsRecorded)
    }

    @Test
    fun doesNotPromptOnAFreshInstall() = runTest(dispatcher) {
        // Launch one, day zero. Unless a fork zeroed every threshold, nothing should fire — this is
        // the case that makes the difference between a policy and a nag.
        val review = RecordingAppReviewManager(canRequestReview = true)
        val store = FakeAppReviewPromptStore(brandNew())
        AppViewModel(
            FakeUserDataRepository(),
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            review,
            store,
        )
        advanceUntilIdle()

        val thresholdsAllZero = AppReviewConfig.MIN_LAUNCHES <= 1 &&
            AppReviewConfig.MIN_DAYS_SINCE_INSTALL == 0
        if (!thresholdsAllZero) assertEquals(0, review.prompts)
        // The LAUNCH is recorded either way — a launch that does not prompt still has to advance the
        // counter, or min_launches could never be reached.
        assertEquals(1, store.launches)
    }

    @Test
    fun neverPromptsWhereReviewIsUnreachable() = runTest(dispatcher) {
        // canRequestReview == false means no native flow AND no configured store listing. Prompting
        // anyway would be a no-op that still burns the cooldown.
        val review = RecordingAppReviewManager(canRequestReview = false)
        val store = FakeAppReviewPromptStore(eligible())
        AppViewModel(
            FakeUserDataRepository(),
            RecordingGarbageCollector(),
            RecordingAppUpdateManager(),
            review,
            store,
        )
        advanceUntilIdle()

        assertEquals(0, review.prompts)
        assertEquals(0, store.promptsRecorded)
    }
}
