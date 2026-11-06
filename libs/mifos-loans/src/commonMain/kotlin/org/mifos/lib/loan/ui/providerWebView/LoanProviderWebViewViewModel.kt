/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.providerWebView

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kpt.core.ui.utils.BaseViewModel
/**
 * `ViewModel` for the Loan Provider Website screen — renders the chosen provider's website
 * (see [urlForLoanProvider]) in an embedded web view.
 *
 * @param savedStateHandle Handle used to read the [LoanProviderWebViewRoute] navigation arguments.
 */
internal class LoanProviderWebViewViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoanProviderWebViewState, LoanProviderWebViewEvent, LoanProviderWebViewAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<LoanProviderWebViewRoute>()
        LoanProviderWebViewState(
            clientId = route.clientId,
            providerId = route.providerId,
            url = route.url,
        )
    },
) {

    override fun handleAction(action: LoanProviderWebViewAction) {
        when (action) {
            LoanProviderWebViewAction.NavigateBack -> {
                sendEvent(LoanProviderWebViewEvent.NavigateBack)
            }
        }
    }
}

/**
 * UI state for the Loan Provider Website screen.
 *
 * @property clientId The id of the client applying for a loan.
 * @property providerId The id of the chosen loan provider.
 * @property url The provider's website to render.
 */
internal data class LoanProviderWebViewState(
    val clientId: Long,
    val providerId: String,
    val url: String,
)

/**
 * One-shot navigation events emitted by [LoanProviderWebViewViewModel].
 */
internal sealed interface LoanProviderWebViewEvent {
    data object NavigateBack : LoanProviderWebViewEvent
}

/**
 * User actions handled by [LoanProviderWebViewViewModel].
 */
internal sealed interface LoanProviderWebViewAction {
    data object NavigateBack : LoanProviderWebViewAction
}
