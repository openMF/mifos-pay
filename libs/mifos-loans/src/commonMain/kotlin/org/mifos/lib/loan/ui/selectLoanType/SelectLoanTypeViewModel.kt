/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.selectLoanType

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.common.errorOrNull
import kpt.core.ui.fork.utils.BaseViewModel
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.core.model.ProductOptions
import org.mifos.lib.loan.repository.LoansRepository
/**
 * `ViewModel` for the Select Loan Type screen.
 *
 * Fetches the loan template for the current client and exposes its [ProductOptions] so the
 * screen can render a selectable list of loan products.
 *
 * @param savedStateHandle Handle used to read the [SelectLoanTypeRoute] navigation arguments.
 * @param loansRepository Facade resolving the loan provider chosen for this application.
 */
internal class SelectLoanTypeViewModel(
    savedStateHandle: SavedStateHandle,
    private val loansRepository: LoansRepository,
) : BaseViewModel<SelectLoanTypeState, SelectLoanTypeEvent, SelectLoanTypeAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<SelectLoanTypeRoute>()
        SelectLoanTypeState(clientId = route.clientId, providerId = route.providerId)
    },
) {

    init {
        loadLoanTemplate()
    }

    override fun handleAction(action: SelectLoanTypeAction) {
        when (action) {
            SelectLoanTypeAction.NavigateBack -> sendEvent(SelectLoanTypeEvent.NavigateBack)

            is SelectLoanTypeAction.ProductClicked -> {
                sendEvent(
                    SelectLoanTypeEvent.NavigateToProductDetails(
                        clientId = state.clientId,
                        productId = action.productId.toLong(),
                        providerId = state.providerId,
                    ),
                )
            }

            SelectLoanTypeAction.Retry -> loadLoanTemplate()

            is SelectLoanTypeAction.Internal.TemplateLoaded -> handleTemplateLoaded(action.result)
        }
    }

    private fun loadLoanTemplate() {
        mutableStateFlow.update { it.copy(viewState = SelectLoanTypeState.ViewState.Loading) }
        viewModelScope.launch {
            loansRepository.getLoanTemplate(state.providerId, state.clientId).collect { result ->
                sendAction(SelectLoanTypeAction.Internal.TemplateLoaded(result))
            }
        }
    }

    private fun handleTemplateLoaded(result: ScreenState<LoanTemplate>) {
        when (result) {
            is ScreenState.Loading -> {
                mutableStateFlow.update { it.copy(viewState = SelectLoanTypeState.ViewState.Loading) }
            }

            is ScreenState.Content -> {
                mutableStateFlow.update {
                    it.copy(
                        viewState = SelectLoanTypeState.ViewState.Content(
                            productOptions = result.data.productOptions,
                        ),
                    )
                }
            }

            // Error / Empty / NoNetwork / Unauthenticated — surface the failure. Only Error
            // carries a message; the others are unreachable for the wizard's dummy provider.
            else -> {
                mutableStateFlow.update {
                    it.copy(
                        viewState = SelectLoanTypeState.ViewState.Error(
                            result.errorOrNull?.message.orEmpty(),
                        ),
                    )
                }
            }
        }
    }
}

/**
 * UI state for the Select Loan Type screen.
 *
 * @property clientId The id of the client applying for a loan.
 * @property providerId The id of the loan provider chosen on the Select Loan Provider screen.
 * @property viewState The current content/loading/error state of the screen.
 */
internal data class SelectLoanTypeState(
    val clientId: Long,
    val providerId: String,
    val viewState: ViewState = ViewState.Loading,
) {
    sealed interface ViewState {
        data object Loading : ViewState
        data class Content(val productOptions: List<ProductOptions>) : ViewState
        data class Error(val message: String) : ViewState
    }
}

/**
 * One-shot navigation events emitted by [SelectLoanTypeViewModel].
 */
internal sealed interface SelectLoanTypeEvent {
    data object NavigateBack : SelectLoanTypeEvent

    data class NavigateToProductDetails(
        val clientId: Long,
        val productId: Long,
        val providerId: String,
    ) : SelectLoanTypeEvent
}

/**
 * User actions and internal events handled by [SelectLoanTypeViewModel].
 */
internal sealed interface SelectLoanTypeAction {
    data object NavigateBack : SelectLoanTypeAction

    data object Retry : SelectLoanTypeAction

    data class ProductClicked(val productId: Int) : SelectLoanTypeAction

    sealed interface Internal : SelectLoanTypeAction {
        data class TemplateLoaded(val result: ScreenState<LoanTemplate>) : Internal
    }
}
