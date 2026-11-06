/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.StandingInstructionRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.standinginstruction.StandingInstruction
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.standing.instruction.createOrUpdate.SIAddEditType
import mifos_pay.feature.standing_instruction.generated.resources.Res
import mifos_pay.feature.standing_instruction.generated.resources.feature_standing_instruction_delete
import mifos_pay.feature.standing_instruction.generated.resources.feature_standing_instruction_delete_message
import mifos_pay.feature.standing_instruction.generated.resources.feature_standing_instruction_deleted_successfully
import org.jetbrains.compose.resources.StringResource
class StandingInstructionViewModel(
    private val repository: ForkPreferencesRepository,
    private val siRepository: StandingInstructionRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SIUiState, SIEvent, SIAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(repository.clientId.value)

        SIUiState(clientId = clientId)
    },
) {

    companion object {
        private const val KEY_STATE = "standing_instruction_state"
    }

    // Template idiom (core-base/store): the list READ holds the native
    // ScreenDataStream and exposes its pre-decided `state` straight to the
    // Screen's `ScreenContent`. No fork-ScreenState bridge, no 6→4 `when` fold,
    // no read-only `SIViewState` — DecisionEngine inside the stream owns every
    // Loading / Empty / NoNetwork / Unauthenticated / Error / Content transition,
    // and `refresh()` drives pull-to-refresh + retry. The 5-second
    // WhileSubscribed keep-alive survives a Compose tab-away / return round-trip.
    // The delete WRITE below goes through a SubmitHandler (see `submitDelete`).
    private val stream = siRepository.getAllStandingInstructionsStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    val viewState: StateFlow<ScreenState<List<StandingInstruction>>> = stream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun retry() = stream.refresh()

    // Template idiom (core-base/store): the delete WRITE goes through a
    // SubmitHandler instead of a hand-folded DataState result action. The handler
    // owns the Submitting/Submitted/Failed lifecycle; we observe it to drive this
    // screen's existing loading/error dialog + success toast. The list refresh is
    // implicit — the reactive `getAllStandingInstructionsStream` re-emits once the
    // record is gone, exactly as before.
    private val submitDelete = viewModelScope.submitHandler<Unit>()

    init {
        submitDelete.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = SIUiState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(
                            SIEvent.ShowToast(
                                Res.string.feature_standing_instruction_deleted_successfully,
                            ),
                        )
                        submitDelete.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = SIUiState.DialogState.Error(message))
                        }
                        submitDelete.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SIAction) {
        when (action) {
            is SIAction.AddNewSI -> {
                sendEvent(SIEvent.OnAddEditSI(SIAddEditType.AddItem))
            }

            is SIAction.EditSIDetails -> {
                sendEvent(SIEvent.OnAddEditSI(SIAddEditType.EditItem(action.siId)))
            }

            is SIAction.ViewSIDetails -> {
                sendEvent(SIEvent.OnNavigateToSIDetails(action.siId))
            }

            is SIAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is SIAction.OnDeleteSI -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = SIUiState.DialogState.DeleteSI(
                            title = Res.string.feature_standing_instruction_delete,
                            message = Res.string.feature_standing_instruction_delete_message,
                            onConfirm = {
                                trySendAction(SIAction.Internal.DeleteSI(action.siId))
                            },
                        ),
                    )
                }
            }

            is SIAction.Internal.DeleteSI -> deleteSI(action)
        }
    }

    private fun deleteSI(action: SIAction.Internal.DeleteSI) {
        // Submit through the handler — it drives Submitting/Submitted/Failed,
        // observed in `init`. The repository write completes normally on success
        // and throws on failure; the handler maps that to Submitted/Failed.
        submitDelete.submit {
            siRepository.deleteStandingInstruction(action.siId)
        }
    }
}

@Serializable
data class SIUiState(
    val clientId: Long,
    @Transient
    val dialogState: DialogState? = null,
) {

    sealed interface DialogState {
        data object Loading : DialogState

        data class Error(val message: String) : DialogState

        data class DeleteSI(
            val title: StringResource,
            val message: StringResource,
            val onConfirm: () -> Unit,
        ) : DialogState
    }
}

sealed interface SIEvent {
    data class ShowToast(val message: StringResource) : SIEvent
    data class OnNavigateToSIDetails(val siId: Long) : SIEvent
    data class OnAddEditSI(val type: SIAddEditType) : SIEvent
}

sealed interface SIAction {
    data object AddNewSI : SIAction
    data class OnDeleteSI(val siId: Long) : SIAction
    data class EditSIDetails(val siId: Long) : SIAction
    data class ViewSIDetails(val siId: Long) : SIAction

    data object DismissDialog : SIAction

    sealed interface Internal : SIAction {
        data class DeleteSI(val siId: Long) : Internal
    }
}
