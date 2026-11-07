/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.deletebeneficiary

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.ui.utils.BaseViewModel
/**
 * ViewModel for handling beneficiary deletion.
 *
 * This is a standalone ViewModel that manages the delete flow independently
 * from the beneficiary list, preventing unwanted list refreshes during the
 * delete confirmation flow.
 */
class DeleteBeneficiaryViewModel(
    private val repository: SelfServiceRepository,
) : BaseViewModel<DeleteBeneficiaryState, DeleteBeneficiaryEvent, DeleteBeneficiaryAction>(
    initialState = DeleteBeneficiaryState(),
) {

    // Public view-state surface renamed from `state` → `deleteState`: BaseViewModel
    // already declares a protected `state: S` (the current-value accessor), so the
    // public StateFlow must use a distinct name. The Screen reads `deleteState`.
    /** The delete screen's state, under the name the screen reads. */
    val deleteState: StateFlow<DeleteBeneficiaryState> get() = stateFlow

    // Template idiom (core-base/store): the delete write goes through a SubmitHandler
    // instead of a hand-folded DataState result. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it here to drive this VM's
    // existing Deleting overlay + success flag + error sheet, so the public state
    // shape and the consuming Screen are unchanged.
    private val submitDelete = viewModelScope.submitHandler<Unit>()

    init {
        submitDelete.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DeleteBeneficiaryState.DialogState.Deleting)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = null,
                                deleteSuccessful = true,
                            )
                        }
                        submitDelete.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message
                            ?: submitState.error.toString()
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = DeleteBeneficiaryState.DialogState.Error(message),
                            )
                        }
                        submitDelete.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: DeleteBeneficiaryAction) {
        when (action) {
            is DeleteBeneficiaryAction.ShowDeleteConfirmation -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = DeleteBeneficiaryState.DialogState.Confirmation(
                            beneficiaryId = action.beneficiaryId,
                            beneficiaryName = action.beneficiaryName,
                        ),
                    )
                }
            }

            is DeleteBeneficiaryAction.ConfirmDelete -> {
                mutableStateFlow.update {
                    it.copy(dialogState = DeleteBeneficiaryState.DialogState.Deleting)
                }

                // Submit through the handler — it drives Submitting/Submitted/Failed,
                // observed in `init`. The repository write returns Unit and throws on
                // failure; the handler maps that to Submitted/Failed.
                submitDelete.submit {
                    repository.deleteBeneficiary(action.beneficiaryId)
                }
            }

            DeleteBeneficiaryAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            DeleteBeneficiaryAction.ConsumeDeleteSuccess -> {
                mutableStateFlow.update {
                    it.copy(deleteSuccessful = false)
                }
            }
        }
    }

    /**
     * Shows the delete confirmation bottom sheet.
     */
    fun showDeleteConfirmation(beneficiaryId: Long, beneficiaryName: String) {
        trySendAction(
            DeleteBeneficiaryAction.ShowDeleteConfirmation(beneficiaryId, beneficiaryName),
        )
    }

    /**
     * Confirms deletion and executes the delete operation.
     */
    fun confirmDelete(beneficiaryId: Long) {
        trySendAction(DeleteBeneficiaryAction.ConfirmDelete(beneficiaryId))
    }

    /**
     * Dismisses the current dialog/bottom sheet.
     */
    fun dismissDialog() {
        trySendAction(DeleteBeneficiaryAction.DismissDialog)
    }

    /**
     * Resets the delete success flag after it has been consumed.
     */
    fun consumeDeleteSuccess() {
        trySendAction(DeleteBeneficiaryAction.ConsumeDeleteSuccess)
    }
}

/**
 * UI state of the delete-beneficiary flow.
 *
 * @property dialogState Dialog currently displayed, or null.
 * @property deleteSuccessful True once the beneficiary was deleted.
 */
data class DeleteBeneficiaryState(
    val dialogState: DialogState? = null,
    val deleteSuccessful: Boolean = false,
) {
    /** Dialogs shown during beneficiary deletion. */
    sealed interface DialogState {
        /**
         * Asks the user to confirm deletion.
         *
         * @property beneficiaryId Id of the beneficiary.
         * @property beneficiaryName Display name of the beneficiary.
         */
        data class Confirmation(
            val beneficiaryId: Long,
            val beneficiaryName: String,
        ) : DialogState

        /** Deletion request in progress. */
        data object Deleting : DialogState

        /**
         * Deletion failed.
         *
         * @property message Error text to display.
         */
        data class Error(val message: String) : DialogState
    }
}

/** One-off events of the delete-beneficiary flow (none currently). */
sealed interface DeleteBeneficiaryEvent

/** Actions handled by the delete-beneficiary view model. */
sealed interface DeleteBeneficiaryAction {
    /**
     * Show the delete confirmation dialog.
     *
     * @property beneficiaryId Id of the beneficiary.
     * @property beneficiaryName Display name of the beneficiary.
     */
    data class ShowDeleteConfirmation(
        val beneficiaryId: Long,
        val beneficiaryName: String,
    ) : DeleteBeneficiaryAction

    /**
     * Confirm the deletion.
     *
     * @property beneficiaryId Id of the beneficiary to delete.
     */
    data class ConfirmDelete(val beneficiaryId: Long) : DeleteBeneficiaryAction

    /** Dismiss the current dialog. */
    data object DismissDialog : DeleteBeneficiaryAction

    /** Mark the success result as consumed. */
    data object ConsumeDeleteSuccess : DeleteBeneficiaryAction
}
