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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kpt.core.base.ui.viewmodel.BaseViewModel
import org.mifospay.core.common.GlobalAuthManager
import org.mifospay.core.datastore.UserPreferencesRepository
import org.mifospay.core.model.user.UserInfo

/**
 * Root navigation gate for the fork.
 *
 * The template's `RootNavViewModel` derived its state from
 * `kpt.core.data.user.UserDataRepository` + `kpt.core.model.user.AuthState`.
 * The fork owns its own auth/session surface — the template's `core/auth`
 * was removed — so this ViewModel is rewired against:
 *
 *  - [UserPreferencesRepository.userInfo] — the persisted user session; a
 *    non-`authenticated` [UserInfo] means "logged out."
 *  - [GlobalAuthManager.isUnauthorized] — a global 401 flag flipped by
 *    `KtorInterceptor` on any HTTP 401; treated as "force back to Auth."
 *
 * The full app-lock / passcode gating (background re-auth, `isPasscodeNotCreated`,
 * `isAppLocked`) is owned by [`org.mifospay.shared.MifosPayViewModel`] at the
 * app-shell layer. RootNav only decides Splash vs Auth vs UserUnlocked; the
 * template's `ShowOnboarding` and `UserLocked` cases are retained on the
 * sealed hierarchy so [RootNavScreen]'s exhaustive `when` still compiles,
 * but the fork never emits them from here.
 */
class RootNavViewModel(
    userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<RootNavState, Unit, RootNavAction>(
    initialState = RootNavState.Splash,
) {

    init {
        combine(
            userPreferencesRepository.userInfo,
            GlobalAuthManager.isUnauthorized,
        ) { userInfo, isUnauthorized ->
            UserStateUpdateReceive(
                userInfo = userInfo,
                isUnauthorized = isUnauthorized,
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
        val userInfo = action.userInfo
        val isUnauthorized = action.isUnauthorized

        val updatedRootNavState = when {
            // Global 401 tripped -> back to Auth regardless of persisted state.
            isUnauthorized -> RootNavState.Auth

            // Persisted session says logged-in.
            userInfo.authenticated -> RootNavState.UserUnlocked(
                activeUserId = userInfo.userId.toString(),
            )

            // No / cleared session.
            else -> RootNavState.Auth
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
            val userInfo: UserInfo,
            val isUnauthorized: Boolean,
        ) : RootNavAction()
    }
}
