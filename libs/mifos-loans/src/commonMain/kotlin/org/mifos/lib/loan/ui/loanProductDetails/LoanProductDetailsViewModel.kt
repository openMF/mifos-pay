/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.loanProductDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.common.errorOrNull
import kpt.core.ui.fork.utils.BaseViewModel
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.repository.LoansRepository
/**
 * `ViewModel` for the Loan Product Details screen.
 *
 * Fetches the loan template scoped to a specific product so the screen can show the product's
 * terms (principal range, interest rate, currency) and gate the "Apply" action behind a
 * terms-and-conditions acknowledgement.
 *
 * @param savedStateHandle Handle used to read the [LoanProductDetailsRoute] navigation arguments.
 * @param loansRepository Facade resolving the loan provider chosen for this application.
 */
internal class LoanProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val loansRepository: LoansRepository,
) : BaseViewModel<LoanProductDetailsState, LoanProductDetailsEvent, LoanProductDetailsAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<LoanProductDetailsRoute>()
        LoanProductDetailsState(
            clientId = route.clientId,
            productId = route.productId,
            providerId = route.providerId,
        )
    },
) {

    init {
        loadTemplate()
    }

    override fun handleAction(action: LoanProductDetailsAction) {
        when (action) {
            LoanProductDetailsAction.NavigateBack -> sendEvent(LoanProductDetailsEvent.NavigateBack)

            is LoanProductDetailsAction.TermsCheckedChanged -> {
                mutableStateFlow.update { it.copy(agreedToTerms = action.checked) }
            }

            LoanProductDetailsAction.ApplyClicked -> {
                if (state.agreedToTerms) {
                    sendEvent(
                        LoanProductDetailsEvent.NavigateToApply(
                            clientId = state.clientId,
                            productId = state.productId,
                            providerId = state.providerId,
                        ),
                    )
                }
            }

            LoanProductDetailsAction.Retry -> loadTemplate()

            is LoanProductDetailsAction.Internal.TemplateLoaded -> handleTemplateLoaded(action.result)
        }
    }

    private fun loadTemplate() {
        mutableStateFlow.update { it.copy(viewState = LoanProductDetailsState.ViewState.Loading) }
        viewModelScope.launch {
            loansRepository
                .getLoanTemplateByProduct(state.providerId, state.clientId, state.productId)
                .collect { result ->
                    sendAction(LoanProductDetailsAction.Internal.TemplateLoaded(result))
                }
        }
    }

    private fun handleTemplateLoaded(result: ScreenState<LoanTemplate>) {
        when (result) {
            is ScreenState.Loading -> {
                mutableStateFlow.update { it.copy(viewState = LoanProductDetailsState.ViewState.Loading) }
            }

            is ScreenState.Content -> {
                val template = result.data
                val product = template.product

                val minPrincipal = product?.minPrincipal ?: template.principal ?: 0.0
                val maxPrincipal = product?.maxPrincipal ?: template.principal ?: 0.0
                val minInterest = product?.minInterestRatePerPeriod ?: template.annualInterestRate ?: 0.0
                val maxInterest = product?.maxInterestRatePerPeriod ?: template.annualInterestRate ?: 0.0

                mutableStateFlow.update {
                    it.copy(
                        viewState = LoanProductDetailsState.ViewState.Content(
                            productName = template.loanProductName ?: product?.name.orEmpty(),
                            currencySymbol = template.currency?.displaySymbol.orEmpty(),
                            minPrincipal = minPrincipal,
                            maxPrincipal = maxPrincipal,
                            minInterest = minInterest,
                            maxInterest = maxInterest,
                        ),
                    )
                }
            }

            // Error / Empty / NoNetwork / Unauthenticated — surface the failure. Only Error
            // carries a message; the others are unreachable for the wizard's dummy provider.
            else -> {
                mutableStateFlow.update {
                    it.copy(
                        viewState = LoanProductDetailsState.ViewState.Error(
                            result.errorOrNull?.message.orEmpty(),
                        ),
                    )
                }
            }
        }
    }
}

/**
 * UI state for the Loan Product Details screen.
 *
 * @property clientId The id of the client applying for a loan.
 * @property productId The id of the loan product being viewed.
 * @property providerId The id of the loan provider chosen on the Select Loan Provider screen.
 * @property agreedToTerms Whether the user has checked the terms-and-conditions checkbox.
 * @property viewState The current content/loading/error state of the screen.
 */
internal data class LoanProductDetailsState(
    val clientId: Long,
    val productId: Long,
    val providerId: String,
    val agreedToTerms: Boolean = false,
    val viewState: ViewState = ViewState.Loading,
) {
    val isApplyEnabled: Boolean
        get() = agreedToTerms && viewState is ViewState.Content

    sealed interface ViewState {
        data object Loading : ViewState

        data class Content(
            val productName: String,
            val currencySymbol: String,
            val minPrincipal: Double,
            val maxPrincipal: Double,
            val minInterest: Double,
            val maxInterest: Double,
        ) : ViewState

        data class Error(val message: String) : ViewState
    }
}

/**
 * One-shot navigation events emitted by [LoanProductDetailsViewModel].
 */
internal sealed interface LoanProductDetailsEvent {
    data object NavigateBack : LoanProductDetailsEvent

    data class NavigateToApply(
        val clientId: Long,
        val productId: Long,
        val providerId: String,
    ) : LoanProductDetailsEvent
}

/**
 * User actions and internal events handled by [LoanProductDetailsViewModel].
 */
internal sealed interface LoanProductDetailsAction {
    data object NavigateBack : LoanProductDetailsAction

    data object Retry : LoanProductDetailsAction

    data class TermsCheckedChanged(val checked: Boolean) : LoanProductDetailsAction

    data object ApplyClicked : LoanProductDetailsAction

    sealed interface Internal : LoanProductDetailsAction {
        data class TemplateLoaded(val result: ScreenState<LoanTemplate>) : Internal
    }
}
