/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.ui.utils.BaseViewModel
class AutoPayScheduleDetailsViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AutoPayScheduleDetailsState, AutoPayScheduleDetailsEvent, AutoPayScheduleDetailsAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AutoPayScheduleDetailsState(
        scheduleId = requireNotNull(savedStateHandle.get<String>("scheduleId")),
    ),
) {

    companion object {
        private const val KEY_STATE = "autopay_schedule_details_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        loadScheduleDetails()
    }

    override fun handleAction(action: AutoPayScheduleDetailsAction) {
        when (action) {
            is AutoPayScheduleDetailsAction.PauseSchedule -> {
                pauseSchedule()
            }
            is AutoPayScheduleDetailsAction.ResumeSchedule -> {
                resumeSchedule()
            }
            is AutoPayScheduleDetailsAction.EditSchedule -> {
                editSchedule()
            }
            is AutoPayScheduleDetailsAction.CancelSchedule -> {
                cancelSchedule()
            }
            is AutoPayScheduleDetailsAction.NavigateBack -> {
                sendEvent(AutoPayScheduleDetailsEvent.NavigateBack)
            }
        }
    }

    private fun loadScheduleDetails() {
        mutableStateFlow.update { it.copy(isLoading = true) }

        // Simulate API call delay
        viewModelScope.launch {
            delay(500)

            // For now, we'll use dummy data
            // In a real implementation, this would fetch from a repository
            val dummySchedule = AutoPaySchedule(
                id = state.scheduleId,
                name = "Monthly Rent Payment",
                amount = 1200.0,
                currency = "USD",
                frequency = "Monthly",
                nextPaymentDate = "2024-02-15",
                status = AutoPayStatus.ACTIVE,
                recipientName = "Landlord Corp",
                accountNumber = "****1234",
            )

            mutableStateFlow.update {
                it.copy(
                    isLoading = false,
                    schedule = dummySchedule,
                )
            }
        }
    }

    private fun pauseSchedule() {
        mutableStateFlow.update {
            it.copy(
                schedule = it.schedule?.copy(status = AutoPayStatus.PAUSED),
            )
        }
        sendEvent(AutoPayScheduleDetailsEvent.SchedulePaused)
    }

    private fun resumeSchedule() {
        mutableStateFlow.update {
            it.copy(
                schedule = it.schedule?.copy(status = AutoPayStatus.ACTIVE),
            )
        }
        sendEvent(AutoPayScheduleDetailsEvent.ScheduleResumed)
    }

    private fun editSchedule() {
        sendEvent(AutoPayScheduleDetailsEvent.NavigateToEdit(state.scheduleId))
    }

    private fun cancelSchedule() {
        mutableStateFlow.update {
            it.copy(
                schedule = it.schedule?.copy(status = AutoPayStatus.CANCELLED),
            )
        }
        sendEvent(AutoPayScheduleDetailsEvent.ScheduleCancelled)
    }
}

@Serializable
data class AutoPayScheduleDetailsState(
    val scheduleId: String,
    val isLoading: Boolean = false,
    val schedule: AutoPaySchedule? = null,
    val error: String? = null,
)

sealed interface AutoPayScheduleDetailsEvent {
    data object NavigateBack : AutoPayScheduleDetailsEvent
    data object SchedulePaused : AutoPayScheduleDetailsEvent
    data object ScheduleResumed : AutoPayScheduleDetailsEvent
    data object ScheduleCancelled : AutoPayScheduleDetailsEvent
    data class NavigateToEdit(val scheduleId: String) : AutoPayScheduleDetailsEvent
}

sealed interface AutoPayScheduleDetailsAction {
    data object PauseSchedule : AutoPayScheduleDetailsAction
    data object ResumeSchedule : AutoPayScheduleDetailsAction
    data object EditSchedule : AutoPayScheduleDetailsAction
    data object CancelSchedule : AutoPayScheduleDetailsAction
    data object NavigateBack : AutoPayScheduleDetailsAction
}
