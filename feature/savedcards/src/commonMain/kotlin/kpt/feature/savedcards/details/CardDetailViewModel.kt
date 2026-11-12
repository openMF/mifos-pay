/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.details

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.SavedCardRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.savedcards.SavedCard
import kpt.core.ui.fork.utils.BaseViewModel
internal class CardDetailViewModel(
    repository: SavedCardRepository,
    userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CardDetailState, CardDetailEvent, CardDetailAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
        val cardId = requireNotNull(savedStateHandle.get<Long>("cardId"))

        CardDetailState(
            clientId = clientId,
            cardId = cardId,
        )
    },
) {

    companion object {
        private const val KEY_STATE = "card_detail_state"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val cartDetailState = repository.getSavedCard(state.clientId, state.cardId)
        .mapLatest { result ->
            // Fold ScreenState → the existing 3-branch ViewState. Single-item
            // detail flows shouldn't emit Empty (the endpoint returns Content or
            // Error), but we defensively project it to Error so the UI still
            // renders a message rather than sitting on Loading forever.
            when (result) {
                is ScreenState.Loading -> ViewState.Loading

                is ScreenState.Empty -> ViewState.Error("Card not found.")

                is ScreenState.Content -> ViewState.Content(result.data)

                is ScreenState.Error -> ViewState.Error(result.error.message.toString())

                is ScreenState.NoNetwork ->
                    ViewState.Error("No network. Please check your connection.")

                is ScreenState.Unauthenticated ->
                    ViewState.Error("Session expired. Please log in again.")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState.Loading,
        )

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: CardDetailAction) {
        when (action) {
            CardDetailAction.NavigateBack -> {
                sendEvent(CardDetailEvent.OnNavigateBack)
            }
        }
    }
}

@Serializable
internal data class CardDetailState(
    val clientId: Long,
    val cardId: Long,
)

internal sealed interface ViewState {
    data object Loading : ViewState

    data class Error(val message: String) : ViewState

    data class Content(val savedCard: SavedCard) : ViewState {
        val selectedColor = Color(savedCard.backgroundColor.toULong())
    }
}

internal sealed interface CardDetailEvent {
    data object OnNavigateBack : CardDetailEvent
}

internal sealed interface CardDetailAction {
    data object NavigateBack : CardDetailAction
}
