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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.datastore.BillerRepository
import kpt.core.model.autopay.Biller
import kpt.core.ui.utils.BaseViewModel
/** View model for the biller list; loads, searches and deletes billers. */
class BillerListViewModel(
    savedStateHandle: SavedStateHandle,
    private val billerRepository: BillerRepository,
) : BaseViewModel<BillerListState, BillerListEvent, BillerListAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: BillerListState(),
) {

    // Template idiom (core-base/store): the one-shot delete goes through a SubmitHandler
    // instead of a hand-folded DataState result. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it to drive this screen's
    // existing loading/success (list-refresh)/error UX, so the Screen is unchanged.
    // The bills list itself is a continuous READ (getAllBillers Flow), left untouched.
    private val submitDelete = viewModelScope.submitHandler<Unit>()

    init {
        submitDelete.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(isLoading = true) }
                    }

                    is SubmitState.Submitted -> {
                        sendEvent(BillerListEvent.BillerDeleted)
                        loadBillers() // Reload the list
                        submitDelete.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to delete biller: ${submitState.error.message}",
                            )
                        }
                        submitDelete.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        loadBillers()
    }

    override fun handleAction(action: BillerListAction) {
        when (action) {
            is BillerListAction.LoadBillers -> {
                loadBillers()
            }
            is BillerListAction.SearchBillers -> {
                searchBillers(action.query)
            }
            is BillerListAction.DeleteBiller -> {
                deleteBiller(action.billerId)
            }
            is BillerListAction.EditBiller -> {
                sendEvent(BillerListEvent.NavigateToEditBiller(action.billerId))
            }
            is BillerListAction.AddNewBiller -> {
                sendEvent(BillerListEvent.NavigateToAddBiller)
            }
        }
    }

    private fun loadBillers() {
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(isLoading = true) }

            try {
                billerRepository.getAllBillers().collect { billers ->
                    mutableStateFlow.update {
                        it.copy(
                            billers = billers,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load billers: ${e.message}",
                    )
                }
            }
        }
    }

    private fun searchBillers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadBillers()
            } else {
                try {
                    val searchResults = billerRepository.searchBillersByName(query)
                    mutableStateFlow.update {
                        it.copy(
                            billers = searchResults,
                            isLoading = false,
                            error = null,
                        )
                    }
                } catch (e: Exception) {
                    mutableStateFlow.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to search billers: ${e.message}",
                        )
                    }
                }
            }
        }
    }

    private fun deleteBiller(billerId: String) {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository throws on failure, which the handler surfaces as Failed.
        submitDelete.submit {
            billerRepository.deleteBiller(billerId)
        }
    }

    /** Holds the saved-state key used to persist the state. */
    companion object {
        private const val KEY_STATE = "biller_list_state"
    }
}

/**
 * UI state of the biller list.
 *
 * @property billers Billers to display.
 * @property isLoading Whether billers are loading.
 * @property error Error message to show, or null.
 */
@Serializable
data class BillerListState(
    val billers: List<Biller> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/** One-off events emitted by the biller list view model. */
sealed interface BillerListEvent {
    /**
     * Navigate to the edit-biller screen.
     *
     * @property billerId Id of the biller to edit.
     */
    data class NavigateToEditBiller(val billerId: String) : BillerListEvent

    /** Navigate to the add-biller screen. */
    data object NavigateToAddBiller : BillerListEvent

    /** A biller was deleted. */
    data object BillerDeleted : BillerListEvent
}

/** User actions handled by the biller list view model. */
sealed interface BillerListAction {
    /** Load the billers. */
    data object LoadBillers : BillerListAction

    /**
     * Search billers.
     *
     * @property query Search text.
     */
    data class SearchBillers(val query: String) : BillerListAction

    /**
     * Delete a biller.
     *
     * @property billerId Id of the biller.
     */
    data class DeleteBiller(val billerId: String) : BillerListAction

    /**
     * Edit a biller.
     *
     * @property billerId Id of the biller.
     */
    data class EditBiller(val billerId: String) : BillerListAction

    /** Add a new biller. */
    data object AddNewBiller : BillerListAction
}
