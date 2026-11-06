/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.repository.NotificationRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.notification.Notification
internal class NotificationViewModel(
    repository: NotificationRepository,
    // Injected to source the clientId for the store's cache key. The Fineract
    // `notifications/` endpoint is session-scoped (no clientId parameter), but
    // the store still partitions the Room SoT per client so a logout-then-login
    // as a different client cannot surface the previous user's cached list.
    // `requireNotNull(...)` because the notification screen is only reachable
    // behind an authenticated session.
    userPreferencesRepository: ForkPreferencesRepository,
) : ViewModel() {
    private val clientId: Long = requireNotNull(userPreferencesRepository.clientId.value)

    // Template idiom (core-base/store): hold the native ScreenDataStream and
    // expose its pre-decided `state` straight to the Screen's `ScreenContent`.
    // No fork-ScreenState bridge, no 6→3 `when` fold — DecisionEngine inside the
    // stream owns every Loading / Empty / NoNetwork / Unauthenticated / Error /
    // Content transition, and `refresh()` drives pull-to-refresh + retry.
    private val stream = repository.fetchNotificationsStream(
        clientId = clientId,
        scope = viewModelScope,
    )

    val state: StateFlow<ScreenState<List<Notification>>> = stream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun retry() = stream.refresh()
}
