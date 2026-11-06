/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.selectLoanProvider

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kpt.core.ui.utils.BaseViewModel
/**
 * `ViewModel` for the Select Loan Provider screen — the new first step of the loan-application
 * wizard.
 *
 * The list of providers is static placeholder data for now (no backend to source it from — see
 * [LoanProviderOption] kdoc). Selecting a provider simply forwards the client into the existing
 * wizard (Select Loan Type onwards); the chosen provider doesn't yet affect any downstream data.
 *
 * @param savedStateHandle Handle used to read the [SelectLoanProviderRoute] navigation arguments.
 */
internal class SelectLoanProviderViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SelectLoanProviderState, SelectLoanProviderEvent, SelectLoanProviderAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<SelectLoanProviderRoute>()
        SelectLoanProviderState(
            clientId = route.clientId,
            providers = defaultLoanProviders,
        )
    },
) {

    override fun handleAction(action: SelectLoanProviderAction) {
        when (action) {
            SelectLoanProviderAction.NavigateBack -> {
                sendEvent(SelectLoanProviderEvent.NavigateBack)
            }

            is SelectLoanProviderAction.ProviderClicked -> {
                sendEvent(
                    SelectLoanProviderEvent.NavigateToLoanProvider(
                        clientId = state.clientId,
                        providerId = action.providerId,
                        url = action.url,
                    ),
                )
            }
        }
    }
}

/**
 * A dummy/placeholder loan provider (e.g. "HDFC Bank", "SBI") shown on the Select Loan Provider
 * screen.
 *
 * There is no backend for loan providers yet — this is illustrative UI data only, per the product
 * requirement to expose a `loanProvider` selection step ahead of the (currently dummy) product
 * flow. Once a real per-provider backend exists, this should be sourced from a repository instead
 * of hardcoded here.
 *
 * @property id A stable identifier for the provider.
 * @property name The provider's display name.
 * @property url
 */
data class LoanProviderOption(
    val id: String,
    val name: String,
    val url: String,
)

/**
 * The static placeholder list of loan providers. See [LoanProviderOption] for why this is
 * hardcoded rather than fetched.
 */
internal val defaultLoanProviders = listOf(
    LoanProviderOption(id = "hdfc", name = "HDFC Bank", url = "https://applyonline.hdfc.bank.in/personal-loans"),
    LoanProviderOption(id = "sbi", name = "SBI", url = "https://sbi.bank.in/web/personal-banking/loans/personal-loans"),
    LoanProviderOption(id = "icici", name = "ICICI Bank", url = "https://www.icici.bank.in/personal-banking/loans"),
    LoanProviderOption(id = "axis", name = "Axis Bank", url = "https://www.axis.bank.in/loans/personal-loan"),
)

/**
 * UI state for the Select Loan Provider screen.
 *
 * @property clientId The id of the client applying for a loan.
 * @property providers The list of selectable (currently dummy) loan providers.
 */
internal data class SelectLoanProviderState(
    val clientId: Long,
    val providers: List<LoanProviderOption> = defaultLoanProviders,
)

/**
 * One-shot navigation events emitted by [SelectLoanProviderViewModel].
 */
sealed interface SelectLoanProviderEvent {

    data object NavigateBack : SelectLoanProviderEvent

    data class NavigateToLoanProvider(
        val clientId: Long,
        val providerId: String,
        val url: String,
    ) : SelectLoanProviderEvent
}

/**
 * User actions handled by [SelectLoanProviderViewModel].
 */
internal sealed interface SelectLoanProviderAction {
    data object NavigateBack : SelectLoanProviderAction

    data class ProviderClicked(
        val providerId: String,
        val url: String,
    ) : SelectLoanProviderAction
}
