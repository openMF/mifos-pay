/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.rootnav

import androidx.lifecycle.viewModelScope
import cmp.navigation.rootnav.RootNavAction.Internal.UserStateUpdateReceive
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.user.UserDataRepository
import kpt.core.model.user.AuthState
import kpt.core.model.user.UserData

/**
 * Decides which top-level destination the app opens on, from auth state and preferences.
 *
 * One place makes the call so the splash → onboarding → auth → lock → app order cannot be re-derived differently by
 * two screens.
 */
class RootNavViewModel(
    userDataRepository: UserDataRepository,
) : BaseViewModel<RootNavState, Unit, RootNavAction>(
    initialState = RootNavState.Splash,
) {

    init {
        userDataRepository.userData.map { userData ->
            UserStateUpdateReceive(
                authState = AuthState.Authenticated("sample-token"),
                userData = userData,
            )
        }.onEach(::handleAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: RootNavAction) {
        when (action) {
            is UserStateUpdateReceive -> handleUserStateUpdateReceive(action)
        }
    }

    private fun handleUserStateUpdateReceive(action: UserStateUpdateReceive) {
        val userData = action.userData

        // TODO:: Configure this based on the user state
        val updatedRootNavState = when {
            userData.firstTimeUser -> RootNavState.ShowOnboarding

            !userData.isAuthenticated -> RootNavState.Auth

            userData.passcode.isEmpty() -> RootNavState.UserLocked

            userData.isUnlocked -> {
                RootNavState.UserUnlocked(userData.activeUserId)
            }

            else -> RootNavState.UserLocked
        }

        mutableStateFlow.update { updatedRootNavState }
    }
}

/** The resolved top-level destination. */
sealed class RootNavState {
    /** Sign-in is required. */
    data object Auth : RootNavState()

    /** Onboarding has not been completed. */
    data object ShowOnboarding : RootNavState()

    /** Still resolving — the initial state, never a resting one. */
    data object Splash : RootNavState()

    /** Signed in, but the app-lock has not been satisfied. */
    data object UserLocked : RootNavState()

    /** Signed in and unlocked — the app proper. */
    data class UserUnlocked(
        /** Who is signed in. */
        val activeUserId: String,
    ) : RootNavState()
}

/** What the root nav can be asked to do. */
sealed class RootNavAction {

    /** Actions raised by the ViewModel's own collectors, never by the UI. */
    sealed class Internal {

        /** Auth state or preferences changed; re-resolve the destination. */
        data class UserStateUpdateReceive(
            /**
             * Auth state at the moment the update was raised. Carried in the action rather than re-read in the
             * reducer, so the destination is decided from one consistent snapshot of auth + preferences.
             */
            val authState: AuthState,
            /** The new preferences. */
            val userData: UserData,
        ) : RootNavAction()
    }
}
