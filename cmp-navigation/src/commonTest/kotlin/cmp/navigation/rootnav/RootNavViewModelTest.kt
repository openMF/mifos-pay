/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.rootnav

import cmp.navigation.testing.FakeUserDataRepository
import cmp.navigation.testing.defaultUserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the [RootNavViewModel] gate ordering.
 *
 * This ViewModel decides, on every user-data emission, whether the app shows onboarding, the auth
 * screen, the lock screen, or the authenticated graph. The `when` is ORDERED and the order IS the
 * policy — each branch is asserted with the LATER branches' conditions also satisfied, so a
 * reordering cannot pass. The passcode case is the sharp one: an empty passcode must land on
 * `UserLocked` even when `isUnlocked` is true, because otherwise a user with no passcode set walks
 * straight into the authenticated graph.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RootNavViewModelTest {

    private val dispatcher = StandardTestDispatcher()

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
    fun startsOnSplashBeforeAnyUserDataArrives() = runTest(dispatcher) {
        val vm = RootNavViewModel(FakeUserDataRepository())

        assertEquals(RootNavState.Splash, vm.stateFlow.value)
    }

    @Test
    fun firstTimeUserOutranksEveryOtherGate() = runTest(dispatcher) {
        // Authenticated + unlocked + passcode set — everything that would otherwise route to the
        // authenticated graph. Onboarding must still win.
        val repo = FakeUserDataRepository(defaultUserData(firstTimeUser = true))
        val vm = RootNavViewModel(repo)
        drain()

        assertEquals(RootNavState.ShowOnboarding, vm.stateFlow.value)
    }

    @Test
    fun unauthenticatedUserGoesToAuthEvenWhenUnlocked() = runTest(dispatcher) {
        val repo = FakeUserDataRepository(
            defaultUserData(isAuthenticated = false, isUnlocked = true),
        )
        val vm = RootNavViewModel(repo)
        drain()

        assertEquals(RootNavState.Auth, vm.stateFlow.value)
    }

    @Test
    fun emptyPasscodeLocksTheUserEvenWhenFlaggedUnlocked() = runTest(dispatcher) {
        // The security-relevant ordering: `passcode.isEmpty()` is checked BEFORE `isUnlocked`.
        // Swap those two branches and a user with no passcode reaches the authenticated graph.
        val repo = FakeUserDataRepository(defaultUserData(passcode = "", isUnlocked = true))
        val vm = RootNavViewModel(repo)
        drain()

        assertEquals(RootNavState.UserLocked, vm.stateFlow.value)
    }

    @Test
    fun unlockedUserReachesTheAuthenticatedGraphCarryingTheirId() = runTest(dispatcher) {
        val repo = FakeUserDataRepository(defaultUserData(activeUserId = "user-42"))
        val vm = RootNavViewModel(repo)
        drain()

        assertEquals(RootNavState.UserUnlocked("user-42"), vm.stateFlow.value)
    }

    @Test
    fun lockedUserFallsThroughToUserLocked() = runTest(dispatcher) {
        val repo = FakeUserDataRepository(defaultUserData(isUnlocked = false))
        val vm = RootNavViewModel(repo)
        drain()

        assertEquals(RootNavState.UserLocked, vm.stateFlow.value)
    }

    @Test
    fun reEvaluatesWhenUserDataChanges() = runTest(dispatcher) {
        // Locking is not a one-shot decision at startup — a lock event mid-session must move the
        // root graph back, or the authenticated UI stays on screen after the app locks.
        val repo = FakeUserDataRepository(defaultUserData(activeUserId = "user-7"))
        val vm = RootNavViewModel(repo)
        drain()
        assertEquals(RootNavState.UserUnlocked("user-7"), vm.stateFlow.value)

        repo.current.value = defaultUserData(activeUserId = "user-7", isUnlocked = false)
        drain()

        assertEquals(RootNavState.UserLocked, vm.stateFlow.value)
    }
}
