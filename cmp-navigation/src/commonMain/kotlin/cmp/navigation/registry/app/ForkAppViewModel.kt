/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.data.repository.AppLockRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.user.UserInfo
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStorageAdapter
/**
 * Root ViewModel scoped to [cmp.navigation.registry.app.MifosPayApp].
 *
 * Aggregates the three pieces of state the app shell needs to make session
 * decisions:
 *  - the persisted user-info flow ([ForkPreferencesRepository.userInfo]),
 *    surfaced as [userState];
 *  - the passcode storage adapter, used by [isPasscodeNotCreated] for the
 *    "logged in but no passcode yet" branch;
 *  - the app-wide lock flag ([AppLockRepository]) consulted by [isAppLocked]
 *    for the 15-second background-resume re-auth gate.
 *
 * [logOut] is the single fan-out point that clears the user session, the lock
 * flag, and the passcode all at once — every logout path in the app should
 * funnel through here.
 */
class ForkAppViewModel(
    private val userDataRepository: ForkPreferencesRepository,
    private val passcodeManager: PasscodeManager,
    private val appLockRepository: AppLockRepository,
    private val passcodeStorageAdapter: PasscodeStorageAdapter,
) : ViewModel() {
    /**
     * Reactive session state. Starts as [UserState.UnAuthenticated] and
     * transitions to [UserState.Authenticated] once the underlying user-info
     * flow emits — note that the wrapped `UserInfo.authenticated` field can
     * still be `false` on a logged-out account, so callers must check both
     * the [UserState] variant *and* `userData.authenticated`.
     * `WhileSubscribed(5_000)` keeps the upstream alive across short
     * configuration-change gaps without leaking when the screen is gone.
     */
    val userState: StateFlow<UserState> = userDataRepository.userInfo.map {
        UserState.Authenticated(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = UserState.UnAuthenticated,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    /**
     * `true` iff no non-blank passcode is currently saved by the
     * [PasscodeStorageAdapter]. Used by `MifosPayApp` to detect the
     * "authenticated but no passcode yet" half-state and force a logout.
     */
    fun isPasscodeNotCreated(): Boolean {
        return passcodeStorageAdapter.loadPasscode().isNullOrBlank()
    }

    /**
     * Single canonical logout. Clears in this order:
     *  1. user-info (flips `authenticated` to false),
     *  2. app-lock flag (so the next session starts unlocked),
     *  3. passcode (via [PasscodeManager.logOut], which deletes the stored
     *     passcode and resets the manager to the creation step).
     *
     * Fire-and-forget: the work runs on [viewModelScope]; callers don't await
     * completion.
     */
    fun logOut() {
        viewModelScope.launch {
            userDataRepository.logOut()
            appLockRepository.deleteLock()
            passcodeManager.logOut()
        }
    }

    /**
     * Pass-through to [AppLockRepository.isAppLocked].
     *
     * @return `true` if the app is currently locked, `false` if explicitly
     *         unlocked, or `null` if no lock flag has been written yet
     *         (fresh install / post-logout). The `MifosPayApp` re-auth gate
     *         treats `null` as "no session to gate" — see the gate's
     *         `isAppLocked()?.let { ... }` use site.
     */
    fun isAppLocked(): Boolean? {
        return appLockRepository.isAppLocked()
    }
}

/**
 * Session state surfaced by [ForkAppViewModel.userState]. Two states only —
 * the app shell makes routing decisions off the [Authenticated.userData]
 * `authenticated` flag.
 */
sealed class UserState {
    /** Initial value before user-info has been read from preferences. */
    data object UnAuthenticated : UserState()

    /**
     * User-info has loaded. [userData].`authenticated` distinguishes a
     * persisted session from a logged-out state.
     *
     * @property userData The loaded user info.
     */
    data class Authenticated(val userData: UserInfo) : UserState()
}
