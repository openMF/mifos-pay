/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.StandingInstructionRepository
import kpt.core.model.standinginstruction.StandingInstruction
import kpt.core.ui.fork.utils.BaseViewModel
internal class SIDetailViewModel(
    repository: StandingInstructionRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SIDetailState, SIDEvent, SIDAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: SIDetailState(ViewState.Loading),
) {

    companion object {
        private const val KEY_STATE = "sid_details_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        val instructionId = requireNotNull(savedStateHandle.get<Long>("instructionId"))

        // Use BaseViewModel's `observeScreen` bridge to fold the ScreenState
        // stream directly into MVI state. The internal `HandleSIDResult`
        // action (which used to shuttle DataState) is removed — the reducer
        // folds inline.
        repository.getStandingInstruction(instructionId).observeScreen { screenState ->
            mutableStateFlow.update { it.copy(viewState = screenState.toViewState()) }
        }
    }

    override fun handleAction(action: SIDAction) {
        when (action) {
            is SIDAction.NavigateBack -> sendEvent(SIDEvent.OnNavigateBack)
        }
    }
}

/**
 * Fold the 6-branch [ScreenState] into the feature's existing 3-branch
 * [ViewState] (Loading/Error/Content). Detail flows shouldn't emit
 * [ScreenState.Empty] (single-item endpoints return Content or Error) but we
 * defensively route it to Error. NoNetwork / Unauthenticated fold into Error
 * until Phase-4 wires per-branch surfaces.
 */
private fun ScreenState<StandingInstruction>.toViewState(): ViewState = when (this) {
    is ScreenState.Loading -> ViewState.Loading
    is ScreenState.Empty -> ViewState.Error("Standing instruction not found.")
    is ScreenState.Content -> ViewState.Content(data)
    is ScreenState.Error -> ViewState.Error(error.message.toString())
    is ScreenState.NoNetwork -> ViewState.Error("No network. Please check your connection.")
    is ScreenState.Unauthenticated -> ViewState.Error("Session expired. Please log in again.")
}

@Serializable
internal data class SIDetailState(
    val viewState: ViewState,
)

@Serializable
internal sealed interface ViewState {
    @Serializable
    data object Loading : ViewState

    @Serializable
    data class Error(val message: String) : ViewState

    @Serializable
    data class Content(val data: StandingInstruction) : ViewState
}

internal sealed interface SIDEvent {
    data object OnNavigateBack : SIDEvent
}

internal sealed interface SIDAction {
    data object NavigateBack : SIDAction
}
