/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.createOrUpdate

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.SavedCardRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.savedcards.CardPayload
import kpt.core.model.savedcards.SavedCard
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.savedcards.createOrUpdate.AECardAction.Internal.HandleCardResult
import kpt.feature.savedcards.generated.resources.Res
import kpt.feature.savedcards.generated.resources.feature_savedcards_card_added_successfully
import kpt.feature.savedcards.generated.resources.feature_savedcards_card_updated_successfully
import kpt.feature.savedcards.utils.CardType
import kpt.feature.savedcards.utils.CreditCardUtils.detectCardType
import kpt.feature.savedcards.utils.CreditCardUtils.isValidCVV
import kpt.feature.savedcards.utils.CreditCardUtils.isValidCreditCardNumber
import kpt.feature.savedcards.utils.CreditCardUtils.isValidExpiryDate
import kpt.feature.savedcards.utils.creditCardColors
import org.jetbrains.compose.resources.StringResource
internal class AddEditCardViewModel(
    private val repository: SavedCardRepository,
    userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AECardState, AECardEvent, AECardAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_CARD_STATE_KEY) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
        val type = CardAddEditArgs(savedStateHandle).cardAddEditType

        AECardState(
            clientId = clientId,
            type = type,
        )
    },
) {

    companion object {
        private const val ADD_EDIT_CARD_STATE_KEY = "add_edit_card_state"
    }

    // Template idiom (core-base/store): the one-shot card WRITE (addSavedCard /
    // updateCard) goes through a SubmitHandler instead of a hand-folded DataState
    // result action. The handler owns the Submitting/Submitted/Failed lifecycle;
    // we observe it in `init` to drive this screen's existing Loading/Error dialog
    // + success toast/navigate, so the Screen is unchanged. The existing-card
    // READ (getSavedCard → HandleCardResult) is left on its ScreenState fold.
    private val submitCard = viewModelScope.submitHandler<Unit>()

    init {
        submitCard.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AECardState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        val successMessage = when (state.type) {
                            is CardAddEditType.AddItem ->
                                Res.string.feature_savedcards_card_added_successfully

                            is CardAddEditType.EditItem ->
                                Res.string.feature_savedcards_card_updated_successfully
                        }
                        sendEvent(AECardEvent.ShowToast(successMessage))
                        sendEvent(AECardEvent.OnNavigateBack)
                        submitCard.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = AECardState.DialogState.Error(message))
                        }
                        submitCard.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = ADD_EDIT_CARD_STATE_KEY, value = it) }
            .launchIn(viewModelScope)

        if (state.type is CardAddEditType.EditItem) {
            repository.getSavedCard(state.clientId, state.type.savedCardId!!).onEach {
                sendAction(HandleCardResult(it))
            }.launchIn(viewModelScope)
        }
    }

    override fun handleAction(action: AECardAction) {
        when (action) {
            is AECardAction.FirstNameChanged -> {
                mutableStateFlow.update {
                    it.copy(firstName = action.firstName.uppercase())
                }
            }

            is AECardAction.LastNameChanged -> {
                mutableStateFlow.update {
                    it.copy(lastName = action.lastName.uppercase())
                }
            }

            is AECardAction.CardNumberChanged -> {
                mutableStateFlow.update {
                    it.copy(cardNumber = action.cardNumber)
                }
            }

            is AECardAction.CVVChanged -> {
                mutableStateFlow.update {
                    it.copy(cvv = action.cvv)
                }
            }

            is AECardAction.ExpiryDateChanged -> {
                mutableStateFlow.update {
                    it.copy(expiryDate = action.expiryDate)
                }
            }

            is AECardAction.BackgroundColorChanged -> {
                mutableStateFlow.update {
                    it.copy(backgroundColor = action.backgroundColor)
                }
            }

            AECardAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            AECardAction.NavigateBack -> {
                sendEvent(AECardEvent.OnNavigateBack)
            }

            AECardAction.SaveCard -> initiateSaveCard()

            is HandleCardResult -> handleCardResult(action)
        }
    }

    private fun initiateSaveCard() = when {
        state.firstName.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("First name is required"))
            }
        }

        state.lastName.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("Last name is required"))
            }
        }

        state.cardNumber.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("Card number is required"))
            }
        }

        state.cardNumber.isValidCreditCardNumber().not() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("Invalid card number"))
            }
        }

        state.expiryDate.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("Expiry date is required"))
            }
        }

        state.expiryDateFormatted.isValidExpiryDate().not() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("Invalid expiry date"))
            }
        }

        state.cvv.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("CVV is required"))
            }
        }

        state.cvv.isValidCVV(state.cardType).not() -> {
            mutableStateFlow.update {
                it.copy(dialogState = AECardState.DialogState.Error("Invalid CVV"))
            }
        }

        else -> initiateAddOrEditCard()
    }

    private fun initiateAddOrEditCard() {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository write returns the success message and throws on
        // failure; the handler maps that to Submitted/Failed.
        submitCard.submit {
            when (val type = state.type) {
                is CardAddEditType.AddItem -> repository.addSavedCard(
                    clientId = state.clientId,
                    card = state.cardPayload,
                )

                is CardAddEditType.EditItem -> repository.updateCard(
                    clientId = state.clientId,
                    cardId = type.savedCardId!!,
                    card = state.cardPayload,
                )
            }
        }
    }

    private fun handleCardResult(action: HandleCardResult) {
        // `getSavedCard` was migrated to `Flow<ScreenState<SavedCard>>`.
        // Content prefills the edit form; the various error branches surface
        // the same dialog the prior DataState.Error path used. Empty is
        // defensively surfaced as an error (single-record endpoint shouldn't
        // emit Empty). NoNetwork / Unauthenticated fold into the existing
        // Error dialog until Phase-4 wires per-branch surfaces.
        when (val result = action.result) {
            is ScreenState.Loading -> {
                mutableStateFlow.update {
                    it.copy(dialogState = AECardState.DialogState.Loading)
                }
            }

            is ScreenState.Empty -> {
                mutableStateFlow.update {
                    it.copy(dialogState = AECardState.DialogState.Error("Card not found."))
                }
            }

            is ScreenState.Error -> {
                val message = result.error.message.toString()

                mutableStateFlow.update {
                    it.copy(dialogState = AECardState.DialogState.Error(message))
                }
            }

            is ScreenState.NoNetwork -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = AECardState.DialogState.Error(
                            "No network. Please check your connection.",
                        ),
                    )
                }
            }

            is ScreenState.Unauthenticated -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = AECardState.DialogState.Error(
                            "Session expired. Please log in again.",
                        ),
                    )
                }
            }

            is ScreenState.Content -> {
                mutableStateFlow.update {
                    it.copy(
                        firstName = result.data.firstName,
                        lastName = result.data.lastName,
                        cardNumber = result.data.cardNumber,
                        cvv = result.data.cvv,
                        expiryDate = result.data.expiryDate,
                        backgroundColor = result.data.backgroundColor,
                        dialogState = null,
                    )
                }
            }
        }
    }
}

@Serializable
internal data class AECardState(
    val clientId: Long,
    val type: CardAddEditType,
    val firstName: String = "",
    val lastName: String = "",
    val cardNumber: String = "",
    val cvv: String = "",
    val expiryDate: String = "",
    val backgroundColor: String = creditCardColors.first().value.toString(),
    @Transient
    val dialogState: DialogState? = null,
) {

    val isAddMode: Boolean
        get() = type == CardAddEditType.AddItem

    val title: String get() = if (isAddMode) "Add New Card" else "Update Card Details"

    val buttonText: String get() = if (isAddMode) "Add New Card" else "Update"

    val cardType: CardType get() = cardNumber.detectCardType()

    val fullName: String get() = "$firstName $lastName"

    val expiryDateFormatted: String
        get() = if (expiryDate.length == 4) {
            "${expiryDate.substring(0, 2)}/${expiryDate.substring(2, 4)}"
        } else {
            expiryDate
        }

    val maskedCVV get() = "*".repeat(cardType.cvvLength)

    @Transient
    val backgroundColors = creditCardColors

    @Transient
    val selectedColor = Color(backgroundColor.toULong())

    @Transient
    val cardPayload = CardPayload(
        firstName = firstName,
        lastName = lastName,
        cardNumber = cardNumber,
        cvv = cvv,
        expiryDate = expiryDate,
        backgroundColor = backgroundColor,
    )

    sealed interface DialogState {
        data object Loading : DialogState
        data class Error(val message: String) : DialogState
    }
}

internal sealed interface AECardEvent {
    data object OnNavigateBack : AECardEvent
    data class ShowToast(val message: StringResource) : AECardEvent
}

internal sealed interface AECardAction {
    data class FirstNameChanged(val firstName: String) : AECardAction
    data class LastNameChanged(val lastName: String) : AECardAction
    data class CardNumberChanged(val cardNumber: String) : AECardAction
    data class CVVChanged(val cvv: String) : AECardAction
    data class ExpiryDateChanged(val expiryDate: String) : AECardAction
    data class BackgroundColorChanged(val backgroundColor: String) : AECardAction

    data object DismissDialog : AECardAction
    data object SaveCard : AECardAction
    data object NavigateBack : AECardAction

    sealed interface Internal : AECardAction {
        /**
         * Existing-card load result. Uses [ScreenState] (not [DataState]) —
         * `getSavedCard` was migrated to `Flow<ScreenState<SavedCard>>` in
         * Phase-3. The add/edit save WRITE no longer has a result action: it
         * goes through the [submitCard] SubmitHandler (template idiom), whose
         * SubmitState is observed in `init`.
         */
        data class HandleCardResult(val result: ScreenState<SavedCard>) : Internal
    }
}
