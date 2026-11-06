/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.loanApply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.ui.DropdownBoxItem
import kpt.core.ui.ErrorScreenContent
import kpt.core.ui.ExposedDropdownBox
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.utils.EventsEffect
import mifos_pay.libs.mifos_loans.generated.resources.Res
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_button_continue
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_hint_applicant_name
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_hint_disbursement_date
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_hint_loan_product
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_hint_principal_amount
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_hint_purpose
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_applicant_name
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_disbursement_date
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_loan_product
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_principal_amount
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_purpose
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_section_fill_details
import mifos_pay.libs.mifos_loans.generated.resources.feature_select_loan_type_network_issue
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/**
 * Entry point for the Loan Application form.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToUploadDocs Callback to proceed to the document upload step with the
 * populated application form data.
 * @param viewModel The state holder managing form validation and business logic.
 */
@Composable
internal fun LoanApplyScreen(
    navigateBack: () -> Unit,
    navigateToUploadDocs: (
        clientId: Long,
        productId: Long,
        applicantName: String,
        loanProductName: String,
        loanPurpose: String,
        disbursementDate: String,
        principalAmount: String,
        providerId: String,
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanApplyViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            LoanApplyEvent.NavigateBack -> navigateBack()

            is LoanApplyEvent.NavigateToUploadDocs -> {
                navigateToUploadDocs(
                    event.clientId,
                    event.productId,
                    event.applicantName,
                    event.loanProductName,
                    event.loanPurpose,
                    event.disbursementDate,
                    event.principalAmount,
                    event.providerId,
                )
            }
        }
    }

    LoanApplyContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun LoanApplyContent(
    state: LoanApplyState,
    onAction: (LoanApplyAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        backPress = { onAction(LoanApplyAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_apply_loan_section_fill_details),
    ) { paddingValues ->
        when (val viewState = state.viewState) {
            LoanApplyState.ViewState.Loading -> MifosProgressIndicator()

            is LoanApplyState.ViewState.Error -> {
                ErrorScreenContent(
                    subTitle = viewState.message,
                    onClickRetry = { onAction(LoanApplyAction.Retry) },
                )
            }

            LoanApplyState.ViewState.NetworkError -> {
                ErrorScreenContent(
                    subTitle = stringResource(Res.string.feature_select_loan_type_network_issue),
                    onClickRetry = { onAction(LoanApplyAction.Retry) },
                )
            }

            LoanApplyState.ViewState.Content -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(KptTheme.spacing.md)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    MifosOutlinedTextField(
                        value = state.applicantName,
                        label = stringResource(Res.string.feature_apply_loan_label_applicant_name),
                        onValueChange = {},
                        readOnly = true,
                        showClearIcon = false,
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.feature_apply_loan_hint_applicant_name),
                            )
                        },
                    )

                    MifosOutlinedTextField(
                        value = state.disbursementDate,
                        label = stringResource(Res.string.feature_apply_loan_label_disbursement_date),
                        onValueChange = {},
                        readOnly = true,
                        showClearIcon = false,
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.feature_apply_loan_hint_disbursement_date),
                            )
                        },
                    )

                    MifosOutlinedTextField(
                        value = state.loanProductName,
                        label = stringResource(Res.string.feature_apply_loan_label_loan_product),
                        onValueChange = {},
                        readOnly = true,
                        showClearIcon = false,
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.feature_apply_loan_hint_loan_product),
                            )
                        },
                    )

                    LoanPurposeDropdown(state = state, onAction = onAction)

                    MifosOutlinedTextField(
                        value = state.principalAmount,
                        label = stringResource(Res.string.feature_apply_loan_label_principal_amount),
                        onValueChange = { onAction(LoanApplyAction.PrincipalAmountChanged(it)) },
                        isError = state.principalAmountError != null,
                        errorMessage = state.principalAmountError?.let { stringResource(it) },
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.feature_apply_loan_hint_principal_amount),
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    )

                    MifosButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isSubmitEnabled,
                        onClick = { onAction(LoanApplyAction.SubmitClicked) },
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_apply_loan_button_continue),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanPurposeDropdown(
    state: LoanApplyState,
    onAction: (LoanApplyAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownBox(
        expanded = expanded,
        label = stringResource(Res.string.feature_apply_loan_label_purpose),
        value = state.selectedLoanPurpose.ifEmpty {
            stringResource(Res.string.feature_apply_loan_hint_purpose)
        },
        onExpandChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        state.loanPurposeOptions.forEach { (_, name) ->
            DropdownBoxItem(
                text = name,
                onClick = {
                    onAction(LoanApplyAction.LoanPurposeSelected(name))
                    expanded = false
                },
            )
        }
    }
}
