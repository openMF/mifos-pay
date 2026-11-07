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
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ErrorScreenContent
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.fork.utils.EventsEffect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.lib.loan.component.LoanCard
import org.mifos.lib.loan.core.model.ProductOptions
import org.mifos.lib.loan.generated.resources.Res
import org.mifos.lib.loan.generated.resources.feature_select_loan_type_choose_loan
import org.mifos.lib.loan.generated.resources.feature_select_loan_type_empty
import org.mifos.lib.loan.generated.resources.feature_select_loan_type_top_bar_title
/**
 * Entry point for the Loan Type Selection screen.
 * Fetches available loan products via the ViewModel and handles navigation to the product details.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToLoanProductDetails Callback to navigate to the details of a selected product.
 * @param viewModel The state holder responsible for fetching and mapping loan products.
 */
@Composable
internal fun SelectLoanTypeScreen(
    navigateBack: () -> Unit,
    navigateToLoanProductDetails: (clientId: Long, productId: Long, providerId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SelectLoanTypeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            SelectLoanTypeEvent.NavigateBack -> navigateBack()

            is SelectLoanTypeEvent.NavigateToProductDetails -> {
                navigateToLoanProductDetails(event.clientId, event.productId, event.providerId)
            }
        }
    }

    SelectLoanTypeContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun SelectLoanTypeContent(
    state: SelectLoanTypeState,
    onAction: (SelectLoanTypeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        backPress = { onAction(SelectLoanTypeAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_select_loan_type_top_bar_title),
    ) { paddingValues ->
        when (val viewState = state.viewState) {
            SelectLoanTypeState.ViewState.Loading -> ForkProgressIndicator()

            is SelectLoanTypeState.ViewState.Error -> {
                ErrorScreenContent(
                    subTitle = viewState.message,
                    onClickRetry = { onAction(SelectLoanTypeAction.Retry) },
                )
            }

            is SelectLoanTypeState.ViewState.Content -> {
                if (viewState.productOptions.isEmpty()) {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_select_loan_type_top_bar_title),
                        subTitle = stringResource(Res.string.feature_select_loan_type_empty),
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(top = KptTheme.spacing.md),
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_select_loan_type_choose_loan),
                            style = KptTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(KptTheme.spacing.md),
                            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                        ) {
                            items(viewState.productOptions, key = { it.id ?: -1 }) { product ->
                                LoanTypeCard(
                                    product = product,
                                    onClick = {
                                        product.id?.let { id ->
                                            onAction(SelectLoanTypeAction.ProductClicked(id))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanTypeCard(
    product: ProductOptions,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LoanCard(
        modifier = modifier,
        title = product.name.orEmpty(),
        amount = "",
        interestRate = "",
        onClick = onClick,
    )
}
