/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kpt.core.base.store.screen.ScreenState
import kpt.core.common.setSerialized
import kpt.core.data.repository.InvoiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.ui.fork.utils.BaseViewModel
/** ViewModel that loads the current client's invoices and emits navigation to invoice detail. */
class InvoicesViewModel(
    invoiceRepository: InvoiceRepository,
    repository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<InvoiceState, InvoiceEvent, InvoiceAction>(
    initialState = savedStateHandle.get(KEY_STATE) ?: run {
        /** Id of the logged-in client, read from the stored fork preferences. */
        val clientId = requireNotNull(repository.clientId.value)

        InvoiceState(clientId = clientId)
    },
) {

    private companion object {
        private const val KEY_STATE = "InvoiceViewModel"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    // Template idiom (core-base/store): hold the native ScreenDataStream and
    // expose its pre-decided `state` straight to the Screen's `ScreenContent`.
    // No fork-ScreenState fold, no 6→4 `when` — DecisionEngine inside the stream
    // owns every Loading / Empty / NoNetwork / Unauthenticated / Error / Content
    // transition, and `refresh()` drives retry. Named `invoiceUiState` (not
    // `state`) because BaseViewModel already owns a `protected val state: S`.
    private val stream = invoiceRepository.getInvoicesStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    /** The client's invoices, as a screen state. */
    val invoiceUiState: StateFlow<ScreenState<List<Invoice>>> = stream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    /** Reloads the invoices. */
    fun retry() = stream.refresh()

    override fun handleAction(action: InvoiceAction) {
        when (action) {
            is InvoiceAction.InvoiceClicked -> {
                sendEvent(InvoiceEvent.NavigateToInvoiceDetail(action.invoiceId))
            }
        }
    }
}

/**
 * Persisted UI state of the invoices screen.
 *
 * @property clientId Id of the client whose invoices are shown.
 */
@Serializable
data class InvoiceState(
    val clientId: Long,
)

/** User actions on the invoices screen. */
sealed interface InvoiceAction {
    /**
     * The user tapped the invoice with the given [invoiceId].
     *
     * @property invoiceId Id of the tapped invoice.
     */
    data class InvoiceClicked(val invoiceId: Long) : InvoiceAction
}

/** One-off events emitted by the invoices screen. */
sealed interface InvoiceEvent {
    /**
     * Navigate to the detail screen of an invoice.
     *
     * @property invoiceId Id of the invoice to open.
     */
    data class NavigateToInvoiceDetail(val invoiceId: Long) : InvoiceEvent
}
