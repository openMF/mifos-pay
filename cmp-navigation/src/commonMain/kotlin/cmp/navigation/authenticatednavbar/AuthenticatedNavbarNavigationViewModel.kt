/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.model.user.UserData

internal class AuthenticatedNavbarNavigationViewModel :
    BaseViewModel<Unit, AuthenticatedNavBarEvent, AuthenticatedNavBarAction>(
        initialState = Unit,
    ) {

    override fun handleAction(action: AuthenticatedNavBarAction) {
        when (action) {
            AuthenticatedNavBarAction.SettingsTabClick -> handleSettingsTabClicked()
            AuthenticatedNavBarAction.HomeTabClick -> handleHomeTabClicked()
            is AuthenticatedNavBarAction.Internal -> handleInternalAction(action)
        }
    }

    private fun handleInternalAction(action: AuthenticatedNavBarAction.Internal) {
        when (action) {
            is AuthenticatedNavBarAction.Internal.UserStateUpdateReceive -> {
            }
        }
    }

    private fun handleHomeTabClicked() {
        sendEvent(AuthenticatedNavBarEvent.NavigateToHomeScreen)
    }

    private fun handleSettingsTabClicked() {
        sendEvent(AuthenticatedNavBarEvent.NavigateToProfileScreen)
    }
}

internal sealed class AuthenticatedNavBarAction {
    data object HomeTabClick : AuthenticatedNavBarAction()

    data object SettingsTabClick : AuthenticatedNavBarAction()

    sealed class Internal : AuthenticatedNavBarAction() {
        data class UserStateUpdateReceive(val userState: UserData?) : Internal()
    }
}

internal sealed class AuthenticatedNavBarEvent {

    abstract val tab: AuthenticatedNavBarTabItem

    data object NavigateToHomeScreen : AuthenticatedNavBarEvent() {
        override val tab: AuthenticatedNavBarTabItem = AuthenticatedNavBarTabItem.HomeTab
    }

    data object NavigateToProfileScreen : AuthenticatedNavBarEvent() {
        override val tab: AuthenticatedNavBarTabItem = AuthenticatedNavBarTabItem.ProfileTab
    }
}
