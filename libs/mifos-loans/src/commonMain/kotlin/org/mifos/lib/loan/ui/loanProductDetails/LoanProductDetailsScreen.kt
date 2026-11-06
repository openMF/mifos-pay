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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.ui.ErrorScreenContent
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.utils.EventsEffect
import mifos_pay.libs.mifos_loans.generated.resources.Res
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_title
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_amendments_and_termination
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_apply_loan
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_continue_legal_compliance
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_default
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_documentation
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_get_loan
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_insurance
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_interest_rate
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_interest_rate_in_numbers
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_jurisdiction
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_repayment
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_sanction_and_disbursement
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_security_and_collateral
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_terms_and_conditions
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_up_to
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_amendments_and_termination_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_continue_legal_compliance_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_default_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_documentation_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_insurance_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_interest_rate_description
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_jurisdiction_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_repayment_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_sanction_and_disbursement_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_personal_loan_security_and_collateral_details
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.lib.loan.component.ApplyLoanBottomBar
import org.mifos.lib.loan.component.LoanCard
import org.mifos.lib.loan.component.TermsAndConditionItem
/**
 * Entry point for the Loan Product Details screen.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToLoanApply Callback to proceed to the loan application form with the selected product.
 * @param viewModel The state holder managing the product data and UI state.
 */
@Composable
internal fun LoanProductDetailsScreen(
    navigateBack: () -> Unit,
    navigateToLoanApply: (clientId: Long, productId: Long, providerId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanProductDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            LoanProductDetailsEvent.NavigateBack -> navigateBack()

            is LoanProductDetailsEvent.NavigateToApply -> {
                navigateToLoanApply(event.clientId, event.productId, event.providerId)
            }
        }
    }

    LoanProductDetailsContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun LoanProductDetailsContent(
    state: LoanProductDetailsState,
    onAction: (LoanProductDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewState = state.viewState
    val topBarTitle = if (viewState is LoanProductDetailsState.ViewState.Content) {
        stringResource(Res.string.feature_loan_apply_loan, viewState.productName.lowercase())
    } else {
        stringResource(Res.string.feature_apply_loan_title)
    }

    MifosScaffold(
        modifier = modifier,
        topBar = {
            MifosTopBar(
                topBarTitle = topBarTitle,
                backPress = { onAction(LoanProductDetailsAction.NavigateBack) },
            )
        },
        bottomBar = {
            if (viewState is LoanProductDetailsState.ViewState.Content) {
                ApplyLoanBottomBar(
                    checked = state.agreedToTerms,
                    isEnabled = state.isApplyEnabled,
                    onCheckedChange = { onAction(LoanProductDetailsAction.TermsCheckedChanged(it)) },
                    onApplyClick = { onAction(LoanProductDetailsAction.ApplyClicked) },
                )
            }
        },
    ) { paddingValues ->
        when (viewState) {
            LoanProductDetailsState.ViewState.Loading -> MifosProgressIndicator()

            is LoanProductDetailsState.ViewState.Error -> {
                ErrorScreenContent(
                    subTitle = viewState.message,
                    onClickRetry = { onAction(LoanProductDetailsAction.Retry) },
                )
            }

            is LoanProductDetailsState.ViewState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(KptTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    item {
                        LoanCard(
                            title = stringResource(Res.string.feature_loan_get_loan, viewState.productName),
                            amount = stringResource(
                                Res.string.feature_loan_up_to,
                                "${viewState.currencySymbol}${formatAmount(viewState.maxPrincipal)}",
                            ),
                            interestRate = stringResource(
                                Res.string.feature_loan_interest_rate_in_numbers,
                                formatRate(viewState.minInterest),
                                formatRate(viewState.maxInterest),
                            ),
                        )
                    }

                    item {
                        Text(
                            text = stringResource(Res.string.feature_loan_terms_and_conditions),
                            style = KptTheme.typography.titleSmall,
                        )
                        Spacer(modifier = Modifier.height(KptTheme.spacing.md))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                        ) {
                            TermsAndConditionItem(
                                title = Res.string.feature_loan_sanction_and_disbursement,
                                description = Res.string
                                    .feature_personal_loan_sanction_and_disbursement_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_interest_rate,
                                description = Res.string.feature_personal_loan_interest_rate_description,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_repayment,
                                description = Res.string.feature_personal_loan_repayment_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_security_and_collateral,
                                description = Res.string.feature_personal_loan_security_and_collateral_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_insurance,
                                description = Res.string.feature_personal_loan_insurance_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_default,
                                description = Res.string.feature_personal_loan_default_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_documentation,
                                description = Res.string.feature_personal_loan_documentation_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_continue_legal_compliance,
                                description = Res.string
                                    .feature_personal_loan_continue_legal_compliance_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_amendments_and_termination,
                                description = Res.string
                                    .feature_personal_loan_amendments_and_termination_details,
                            )

                            TermsAndConditionItem(
                                title = Res.string.feature_loan_jurisdiction,
                                description = Res.string.feature_personal_loan_jurisdiction_details,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats a principal amount, dropping the decimal point when the value is a whole number.
 */
private fun formatAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()

/**
 * Formats an interest rate percentage for display, e.g. "12%".
 */
private fun formatRate(rate: Double): String = "${formatAmount(rate)}%"
