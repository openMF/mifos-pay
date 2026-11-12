/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards

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
import kpt.core.data.repository.SavedCardRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.savedcards.SavedCard
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.savedcards.createOrUpdate.CardAddEditType
import kpt.feature.savedcards.generated.resources.Res
import kpt.feature.savedcards.generated.resources.feature_savedcards_card_deleted_successfully
import kpt.feature.savedcards.generated.resources.feature_savedcards_confirm_delete_card
import kpt.feature.savedcards.generated.resources.feature_savedcards_delete_card
import org.jetbrains.compose.resources.StringResource
/**
 * ViewModel of the saved cards list screen; loads cards for the signed-in client and handles view, edit, add and
 * delete actions.
 */
class CardsScreenViewModel(
    private val repository: SavedCardRepository,
    userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CardState, CardEvent, CardAction>(
    initialState = savedStateHandle.getSerialized(key = KEY_STATE) ?: run {
        /**
         * Id of the signed-in client, read from the user preferences repository.
         */
        val clientId = requireNotNull(userRepository.clientId.value)

        CardState(clientId = clientId)
    },
) {

    private companion object {
        private const val KEY_STATE = "saved_card_state"
    }

    // Template idiom (core-base/store): hold the native ScreenDataStream for the
    // saved-cards list READ and expose its pre-decided `state` straight to the
    // Screen's `ScreenContent`. No fork-ScreenState fold — DecisionEngine inside
    // the stream owns every Loading / Empty / NoNetwork / Unauthenticated / Error /
    // Content transition, and `refresh()` drives retry. The card WRITE actions
    // (add / edit / delete) + dialog state below are unchanged (BaseViewModel MVI).
    private val stream = repository.getSavedCardsStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    /** The client's saved cards, as a screen state. */
    val cardState: StateFlow<ScreenState<List<SavedCard>>> = stream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    /** Reloads the saved cards. */
    fun retry() = stream.refresh()

    // Template idiom (core-base/store): the one-shot card WRITE (deleteCard) goes
    // through a SubmitHandler instead of a hand-folded DataState result action. The
    // handler owns the Submitting/Submitted/Failed lifecycle; we observe it in `init`
    // to drive the existing Loading/Error dialog + success toast, so the Screen and
    // the delete-confirm dialog (DeleteCardClicked) are unchanged.
    private val submitDelete = viewModelScope.submitHandler<Unit>()

    init {
        submitDelete.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = CardState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(CardEvent.ShowToast(Res.string.feature_savedcards_card_deleted_successfully))
                        submitDelete.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = CardState.DialogState.Error(message))
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

    override fun handleAction(action: CardAction) {
        when (action) {
            is CardAction.AddNewCard -> {
                sendEvent(CardEvent.OnNavigateToAddEdit(CardAddEditType.AddItem))
            }

            is CardAction.ViewCardDetails -> {
                sendEvent(CardEvent.OnNavigateToCardDetails(action.cardId))
            }

            is CardAction.EditCardDetails -> {
                sendEvent(CardEvent.OnNavigateToAddEdit(CardAddEditType.EditItem(action.cardId)))
            }

            is CardAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is CardAction.DeleteCardClicked -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = CardState.DialogState.DeleteCard(
                            title = Res.string.feature_savedcards_delete_card,
                            message = Res.string.feature_savedcards_confirm_delete_card,
                            onConfirm = {
                                trySendAction(CardAction.Internal.DeleteCard(action.cardId))
                            },
                        ),
                    )
                }
            }

            is CardAction.Internal.DeleteCard -> deleteCard(action)
        }
    }

    private fun deleteCard(action: CardAction.Internal.DeleteCard) {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository write returns the success message and throws on
        // failure; the handler maps that to Submitted/Failed.
        submitDelete.submit {
            repository.deleteCard(state.clientId, action.cardId)
        }
    }
}

/**
 * UI state of the saved cards screen.
 * @property clientId Id of the client whose cards are managed.
 * @property dialogState Dialog currently shown over the screen, or null when none (not persisted).
 */
@Serializable
data class CardState(
    val clientId: Long,
    @Transient
    val dialogState: DialogState? = null,
) {

    /**
     * Dialogs that can be shown on the saved cards screen.
     */
    sealed interface DialogState {
        /**
         * Progress dialog shown while a card operation is running.
         */
        data object Loading : DialogState

        /**
         * Error dialog.
         * @property message Error text to display.
         */
        data class Error(val message: String) : DialogState

        /**
         * Confirmation dialog shown before deleting a card.
         * @property title String resource for the dialog title.
         * @property message String resource for the dialog message.
         * @property onConfirm Callback invoked when the user confirms deletion.
         */
        data class DeleteCard(
            val title: StringResource,
            val message: StringResource,
            val onConfirm: () -> Unit,
        ) : DialogState
    }
}

/**
 * One-off events emitted by the saved cards ViewModel to the UI.
 */
sealed interface CardEvent {
    /**
     * Navigate to the details of a card.
     * @property cardId Id of the card to open.
     */
    data class OnNavigateToCardDetails(val cardId: Long) : CardEvent

    /**
     * Navigate to the add/edit card screen.
     * @property type Whether a card is being added or edited.
     */
    data class OnNavigateToAddEdit(val type: CardAddEditType) : CardEvent

    /**
     * Show a toast message.
     * @property message String resource of the message.
     */
    data class ShowToast(val message: StringResource) : CardEvent
}

/**
 * User and internal actions handled by the saved cards ViewModel.
 */
sealed interface CardAction {
    /**
     * User opened the details of a card.
     * @property cardId Id of the selected card.
     */
    data class ViewCardDetails(val cardId: Long) : CardAction

    /**
     * User chose to edit a card.
     * @property cardId Id of the card to edit.
     */
    data class EditCardDetails(val cardId: Long) : CardAction

    /**
     * User tapped delete on a card; shows the confirmation dialog.
     * @property cardId Id of the card to delete.
     */
    data class DeleteCardClicked(val cardId: Long) : CardAction

    /**
     * Dismisses the currently shown dialog.
     */
    data object DismissDialog : CardAction

    /**
     * User chose to add a new card.
     */
    data object AddNewCard : CardAction

    /**
     * Actions dispatched by the ViewModel itself rather than the UI.
     */
    sealed interface Internal : CardAction {
        /**
         * Deletes a card after the user confirmed.
         * @property cardId Id of the card to delete.
         */
        data class DeleteCard(val cardId: Long) : Internal
    }
}
