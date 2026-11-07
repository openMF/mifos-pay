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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.ui.fork.utils.EventsEffect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.lib.loan.generated.resources.Res
import org.mifos.lib.loan.generated.resources.feature_loan_provider_web_view_top_bar_title
/**
 * Entry point for the Loan Provider Website screen — shown right after a provider is picked on
 * the Select Loan Provider screen, rendering that provider's website in an embedded web view.
 *
 * @param navigateBack Callback to return to the Select Loan Provider screen.
 * @param viewModel The state holder responsible for resolving which url to render.
 */
@Composable
internal fun LoanProviderWebViewScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanProviderWebViewViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            LoanProviderWebViewEvent.NavigateBack -> navigateBack()
        }
    }

    LoanProviderWebViewContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun LoanProviderWebViewContent(
    state: LoanProviderWebViewState,
    onAction: (LoanProviderWebViewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        backPress = { onAction(LoanProviderWebViewAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_loan_provider_web_view_top_bar_title),
    ) { paddingValues ->
        PlatformWebView(
            url = state.url,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        )
    }
}
