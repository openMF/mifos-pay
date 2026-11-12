/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.user.impl

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.infra.StoreCacheManager
import kpt.core.data.user.LogoutEvent
import kpt.core.data.user.LogoutReason
import kpt.core.data.user.UserLogoutManager
import kpt.core.data.util.bufferedMutableSharedFlow
import kpt.core.datastore.prefs.UserPreferencesRepository

/**
 * Default [UserLogoutManager]: clears every Store cache and draft row, then emits the logout event.
 *
 * Cache-clearing is the load-bearing half — a Store that survives logout would serve the previous
 * user's data to the next one.
 */
class UserLogoutManagerImpl(
    private val repository: UserPreferencesRepository,
    private val storeCacheManager: StoreCacheManager,
    dispatcherManager: DispatcherManager,
) : UserLogoutManager {

    private val scope = CoroutineScope(dispatcherManager.unconfined)

    private val mutableLogoutEventFlow: MutableSharedFlow<LogoutEvent> = bufferedMutableSharedFlow()
    override val logoutEventFlow: SharedFlow<LogoutEvent> = mutableLogoutEventFlow.asSharedFlow()

    /**
     * Completely logs out the given [userId], removing all data. The [reason] indicates why the
     * user is being logged out.
     */
    // TODO:: Currently, both methods (logout and softLogout) perform the same action.
    override fun logout(userId: Long, reason: LogoutReason) {
        Logger.d { "User Logout - $userId, $reason" }

        clearUserData()
        mutableLogoutEventFlow.tryEmit(LogoutEvent(userId))
    }

    /**
     * Partially logs out the given [userId]. All data for the given [userId] will be removed with
     * the exception of basic account data. The [reason] indicates why the user is being logged out.
     */
    override fun softLogout(userId: Long, reason: LogoutReason) {
        Logger.d { "User Logout - $userId, $reason" }

        clearUserData()
        mutableLogoutEventFlow.tryEmit(LogoutEvent(userId))
    }

    private fun clearUserData() {
        scope.launch {
            repository.clearUserData()
            storeCacheManager.clearAll()
        }
    }
}
