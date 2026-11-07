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
import kpt.core.common.setSerialized
import kpt.core.datastore.BillRepository
import kpt.core.model.autopay.Bill
import kpt.core.ui.fork.utils.BaseViewModel
/** View model for the bill list; loads bills and handles deletion. */
class BillListViewModel(
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
) : BaseViewModel<BillListState, BillListEvent, BillListAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: BillListState(),
) {

    /** Holds the saved-state key used to persist the list state. */
    companion object {
        private const val KEY_STATE = "bill_list_state"
    }

    // Template idiom (core-base/store): the one-shot delete goes through a SubmitHandler
    // instead of a hand-folded DataState result. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it to drive this screen's
    // existing loading/success (list-refresh)/error UX, so the Screen is unchanged.
    // The bills list itself is a continuous READ (getAllBills Flow), left untouched.
    private val submitDelete = viewModelScope.submitHandler<Unit>()

    init {
        submitDelete.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(isLoading = true) }
                    }

                    is SubmitState.Submitted -> {
                        sendEvent(BillListEvent.BillDeleted)
                        loadBills() // Reload the list
                        submitDelete.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to delete bill: ${submitState.error.message}",
                            )
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

        loadBills()
    }

    override fun handleAction(action: BillListAction) {
        when (action) {
            is BillListAction.RefreshBills -> {
                loadBills()
            }
            is BillListAction.AddNewBill -> {
                addNewBill()
            }
            is BillListAction.EditBill -> {
                editBill(action.billId)
            }
            is BillListAction.DeleteBill -> {
                deleteBill(action.billId)
            }
        }
    }

    private fun loadBills() {
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(isLoading = true) }

            try {
                billRepository.getAllBills().collect { bills ->
                    mutableStateFlow.update {
                        it.copy(
                            bills = bills,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load bills: ${e.message}",
                    )
                }
            }
        }
    }

    private fun addNewBill() {
        sendEvent(BillListEvent.NavigateToAddBill)
    }

    private fun editBill(billId: String) {
        sendEvent(BillListEvent.NavigateToEditBill(billId))
    }

    private fun deleteBill(billId: String) {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository throws on failure, which the handler surfaces as Failed.
        submitDelete.submit {
            billRepository.deleteBill(billId)
        }
    }
}

/**
 * UI state of the bill list.
 *
 * @property isLoading Whether bills are loading.
 * @property error Error message to show, or null.
 * @property bills Bills to display.
 */
@Serializable
data class BillListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bills: List<Bill> = emptyList(),
)

/** One-off events emitted by the bill list view model. */
sealed interface BillListEvent {
    /** Navigate to the add-bill screen. */
    data object NavigateToAddBill : BillListEvent

    /**
     * Navigate to the edit-bill screen.
     *
     * @property billId Id of the bill to edit.
     */
    data class NavigateToEditBill(val billId: String) : BillListEvent

    /** A bill was deleted. */
    data object BillDeleted : BillListEvent
}

/** User actions handled by the bill list view model. */
sealed interface BillListAction {
    /** Reload the bills. */
    data object RefreshBills : BillListAction

    /** The user tapped add bill. */
    data object AddNewBill : BillListAction

    /**
     * The user chose to edit a bill.
     *
     * @property billId Id of the bill.
     */
    data class EditBill(val billId: String) : BillListAction

    /**
     * The user chose to delete a bill.
     *
     * @property billId Id of the bill.
     */
    data class DeleteBill(val billId: String) : BillListAction
}
