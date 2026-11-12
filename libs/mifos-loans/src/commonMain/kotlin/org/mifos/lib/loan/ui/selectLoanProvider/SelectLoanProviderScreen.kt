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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkCard
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.ui.fork.utils.EventsEffect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.lib.loan.generated.resources.Res
import org.mifos.lib.loan.generated.resources.feature_select_loan_provider_choose_provider
import org.mifos.lib.loan.generated.resources.feature_select_loan_provider_top_bar_title
/**
 * Entry point for the Select Loan Provider screen — the new first step of the loan-application
 * wizard, shown when the user taps "Apply for Loan".
 *
 * Shows a short list of (currently dummy/placeholder) loan providers; tapping one proceeds into
 * the existing wizard starting at Select Loan Type.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToLoanProviderWebView Callback to proceed into the existing loan-type selection step.
 * @param viewModel The state holder responsible for the (static) provider list.
 */
@Composable
internal fun SelectLoanProviderScreen(
    navigateBack: () -> Unit,
    navigateToLoanProviderWebView: (
        clientId: Long,
        providerId: String,
        url: String,
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SelectLoanProviderViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            SelectLoanProviderEvent.NavigateBack -> navigateBack()

            is SelectLoanProviderEvent.NavigateToLoanProvider -> {
                navigateToLoanProviderWebView(
                    event.clientId,
                    event.providerId,
                    event.url,
                )
            }
        }
    }

    SelectLoanProviderContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun SelectLoanProviderContent(
    state: SelectLoanProviderState,
    onAction: (SelectLoanProviderAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        backPress = { onAction(SelectLoanProviderAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_select_loan_provider_top_bar_title),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(top = KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_select_loan_provider_choose_provider),
                style = KptTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
            )

            LazyColumn(
                contentPadding = PaddingValues(KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                items(state.providers, key = { it.id }) { provider ->
                    LoanProviderCard(
                        provider = provider,
                        onClick = {
                            onAction(
                                SelectLoanProviderAction.ProviderClicked(
                                    providerId = provider.id,
                                    url = provider.url,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanProviderCard(
    provider: LoanProviderOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkCard(
        modifier = modifier,
        shape = KptTheme.shapes.medium,
        elevation = KptTheme.elevation.level2,
        onClick = onClick,
    ) {
        Text(
            text = provider.name,
            style = KptTheme.typography.titleMedium,
            modifier = Modifier.padding(KptTheme.spacing.lg),
        )
    }
}
