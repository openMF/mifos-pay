/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.invoices.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.InvoiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.invoices.details.InvoiceDetailState.ViewState.Content
import kpt.feature.invoices.details.InvoiceDetailState.ViewState.Error
internal class InvoiceDetailViewModel(
    private val preferencesRepository: ForkPreferencesRepository,
    repository: InvoiceRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<InvoiceDetailState, InvoiceDetailEvent, InvoiceDetailAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(preferencesRepository.clientId.value)
        val invoiceId = requireNotNull(savedStateHandle.get<Long>("invoiceId"))

        InvoiceDetailState(
            clientId = clientId,
            invoiceId = invoiceId,
            viewState = InvoiceDetailState.ViewState.Loading,
        )
    },
) {

    companion object {
        private const val KEY_STATE = "invoice_detail_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        // Use BaseViewModel's `observeScreen` bridge to fold the ScreenState
        // stream directly into the feature's MVI state. The internal
        // `InvoiceDetailResultReceived` action (which used to shuttle
        // DataState) is no longer needed — the reducer folds inline.
        repository.getInvoice(state.clientId, state.invoiceId).observeScreen { screenState ->
            mutableStateFlow.update { it.copy(viewState = screenState.toViewState()) }
        }
    }

    override fun handleAction(action: InvoiceDetailAction) {
        when (action) {
            is InvoiceDetailAction.NavigateBack -> {
                sendEvent(InvoiceDetailEvent.OnNavigateBack)
            }
        }
    }
}

/**
 * Fold the 6-branch [ScreenState] into the feature's existing 3-branch
 * [InvoiceDetailState.ViewState] (Loading/Error/Content). Detail flows should
 * never emit [ScreenState.Empty] (single-item endpoints return Content or
 * Error), but we defensively route it to Error so the UI shows a message
 * rather than sitting on Loading forever. [ScreenState.NoNetwork] and
 * [ScreenState.Unauthenticated] fold into Error until Phase-4 differentiates.
 */
private fun ScreenState<Invoice>.toViewState(): InvoiceDetailState.ViewState = when (this) {
    is ScreenState.Loading -> InvoiceDetailState.ViewState.Loading
    is ScreenState.Empty -> Error("Invoice not found.")
    is ScreenState.Content -> Content(data)
    is ScreenState.Error -> Error(error.message.toString())
    is ScreenState.NoNetwork -> Error("No network. Please check your connection.")
    is ScreenState.Unauthenticated -> Error("Session expired. Please log in again.")
}

@Serializable
internal data class InvoiceDetailState(
    val clientId: Long,
    val invoiceId: Long,
    val viewState: ViewState,
) {
    @Serializable
    sealed interface ViewState {
        @Serializable
        data object Loading : ViewState

        @Serializable
        data class Error(val message: String) : ViewState

        @Serializable
        data class Content(val invoice: Invoice) : ViewState
    }
}

internal sealed interface InvoiceDetailEvent {
    data object OnNavigateBack : InvoiceDetailEvent
}

internal sealed interface InvoiceDetailAction {
    data object NavigateBack : InvoiceDetailAction
}
