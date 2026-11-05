/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the bottom-nav tab dispatch in [AuthenticatedNavbarNavigationViewModel].
 *
 * Each tab action must emit its OWN event, and each event carries the [AuthenticatedNavBarTabItem]
 * that the navbar highlights. Collapsing the two arms — easy, since both are `data object` actions
 * handled by one-line private methods — sends the user to one screen while the other tab lights up,
 * which is precisely the kind of break that looks fine in a screenshot test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticatedNavbarNavigationViewModelTest {

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
    fun homeTabNavigatesHomeAndHighlightsTheHomeTab() = runTest(dispatcher) {
        val vm = AuthenticatedNavbarNavigationViewModel()
        val events = mutableListOf<AuthenticatedNavBarEvent>()
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        vm.trySendAction(AuthenticatedNavBarAction.HomeTabClick)
        drain()

        assertEquals(listOf<AuthenticatedNavBarEvent>(AuthenticatedNavBarEvent.NavigateToHomeScreen), events)
        assertEquals(AuthenticatedNavBarTabItem.HomeTab, events.single().tab)
    }

    @Test
    fun settingsTabNavigatesToProfileAndHighlightsTheProfileTab() = runTest(dispatcher) {
        val vm = AuthenticatedNavbarNavigationViewModel()
        val events = mutableListOf<AuthenticatedNavBarEvent>()
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        vm.trySendAction(AuthenticatedNavBarAction.SettingsTabClick)
        drain()

        assertEquals(listOf<AuthenticatedNavBarEvent>(AuthenticatedNavBarEvent.NavigateToProfileScreen), events)
        assertEquals(AuthenticatedNavBarTabItem.ProfileTab, events.single().tab)
    }

    @Test
    fun theInternalUserStateActionEmitsNothing() = runTest(dispatcher) {
        // Deliberately inert today. Asserting the silence keeps a future implementation from
        // quietly emitting a navigation event on every user-data tick, which would yank the user
        // off whichever tab they were on.
        val vm = AuthenticatedNavbarNavigationViewModel()
        val events = mutableListOf<AuthenticatedNavBarEvent>()
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        vm.trySendAction(AuthenticatedNavBarAction.Internal.UserStateUpdateReceive(userState = null))
        drain()

        assertEquals(emptyList<AuthenticatedNavBarEvent>(), events)
    }
}
