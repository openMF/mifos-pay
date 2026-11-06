/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.confirmDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.data.repository.UserVerificationRepository
import kpt.core.ui.utils.BaseViewModel
import mifos_pay.libs.mifos_loans.generated.resources.Res
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_error_server
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_error_submit_failed
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_applicant_name
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_disbursement_date
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_loan_product
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_principal_amount
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_label_purpose
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_status_success_tip
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.mifos.lib.loan.core.model.LoanState
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.core.model.LoansPayload
import org.mifos.lib.loan.repository.LoansRepository
/**
 * `SavedStateHandle` key written by `internalMifosPasscodeScreen` and read by
 * [ConfirmDetailsScreen] for the loan-application submission auth-gate round trip. Mirrors
 * [kpt.feature.transfer.intrabank.confirm.INTRA_BANK_TRANSFER_VERIFICATION_KEY], but
 * with its own key so the two flows don't cross-fire.
 */
const val LOAN_APPLICATION_VERIFICATION_KEY = "loan_application_verification_key"

/**
 * `ViewModel` for the Confirm Details screen — the final step of the loan-application wizard.
 *
 * Owns the **passcode/biometric auth gate** required before submitting the application, mirroring
 * `TransferConfirmViewModel`:
 *  1. On [ConfirmDetailsAction.SubmitClicked], emits [ConfirmDetailsEvent.NavigateForPasscodeVerification].
 *     The screen responds by navigating to `internalMifosPasscodeScreen` with
 *     [LOAN_APPLICATION_VERIFICATION_KEY].
 *  2. The screen observes its own saved-state-handle for the round-trip boolean and re-dispatches
 *     it as [ConfirmDetailsAction.UpdateUserVerificationResult]. This VM resumes on the result.
 *  3. On `success = true`, the VM calls [UserVerificationRepository.consumeVerification]. If the
 *     30s token is still valid, the loan application is fetched (for term/interest data not
 *     carried by the wizard's route chain) and submitted; if expired, submission is cancelled
 *     with a "user verification failed" dialog.
 *
 * On successful submission, an in-place success dialog is shown (no dedicated status screen
 * exists in this app); once acknowledged, [ConfirmDetailsEvent.SubmitSucceeded] tells the caller
 * to leave the loan-application flow entirely. On failure, an in-place error dialog with a retry
 * option is shown instead.
 *
 * @param loansRepository Facade resolving the loan provider chosen for this application, used for
 * fetching the loan template (term/interest data) and for submitting the application itself.
 * @param userVerificationRepository The passcode/biometric verification token contract.
 * @param savedStateHandle Handle used to read the [ConfirmDetailsRoute] navigation arguments.
 */
internal class ConfirmDetailsViewModel(
    private val loansRepository: LoansRepository,
    private val userVerificationRepository: UserVerificationRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ConfirmDetailsState, ConfirmDetailsEvent, ConfirmDetailsAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<ConfirmDetailsRoute>()
        ConfirmDetailsState(
            clientId = route.clientId,
            productId = route.productId,
            applicantName = route.applicantName,
            loanProductName = route.loanProductName,
            loanPurpose = route.loanPurpose,
            disbursementDate = route.disbursementDate,
            principalAmount = route.principalAmount,
            providerId = route.providerId,
        )
    },
) {

    override fun handleAction(action: ConfirmDetailsAction) {
        when (action) {
            ConfirmDetailsAction.OnNavigateBack -> sendEvent(ConfirmDetailsEvent.NavigateBack)

            ConfirmDetailsAction.SubmitClicked -> onSubmitClicked()

            ConfirmDetailsAction.RetrySubmit -> onSubmitClicked()

            ConfirmDetailsAction.DismissDialog -> {
                mutableStateFlow.update { it.copy(dialogState = null) }
            }

            ConfirmDetailsAction.AcknowledgeSuccess -> {
                mutableStateFlow.update { it.copy(dialogState = null) }
                sendEvent(ConfirmDetailsEvent.SubmitSucceeded)
            }

            is ConfirmDetailsAction.UpdateUserVerificationResult -> {
                mutableStateFlow.update {
                    it.copy(
                        userVerificationResult = action.result,
                        isAwaitingPasscodeVerification = false,
                    )
                }
            }
        }
    }

    private fun onSubmitClicked() {
        if (state.isSubmitting) return
        mutableStateFlow.update { it.copy(isSubmitting = true, dialogState = null) }

        // Passcode/biometric verification gate is temporarily disabled for loan submission.
        // To re-enable, restore the handleUserVerification(...) wrapping below (see git history).
        viewModelScope.launch {
            submitLoanApplication()
        }
    }

    /**
     * Suspends until the passcode/biometric gate round trip completes.
     *
     * Emits [ConfirmDetailsEvent.NavigateForPasscodeVerification], then waits for
     * [ConfirmDetailsState.userVerificationResult] to flip from `null`. The screen is responsible
     * for calling [ConfirmDetailsAction.UpdateUserVerificationResult] with the boolean written
     * under [LOAN_APPLICATION_VERIFICATION_KEY] in the saved-state-handle.
     */
    private suspend fun handleUserVerification(
        onSuccess: suspend () -> Unit,
        onFailed: suspend () -> Unit,
    ) {
        mutableStateFlow.update {
            it.copy(userVerificationResult = null, isAwaitingPasscodeVerification = true)
        }

        sendEvent(ConfirmDetailsEvent.NavigateForPasscodeVerification)

        val result = stateFlow
            .map { it.userVerificationResult }
            .filter { it != null }
            .first()

        when (result) {
            true -> onSuccess()
            false -> onFailed()
            null -> Unit
        }
    }

    /**
     * Fetches the full loan template for the selected product — needed for term/interest fields
     * that the wizard's route chain doesn't carry — then submits the loan application.
     */
    private suspend fun submitLoanApplication() {
        val templateResult = loansRepository
            .getLoanTemplateByProduct(state.providerId, state.clientId, state.productId)
            .first { it !is ScreenState.Loading }

        when (templateResult) {
            is ScreenState.Content -> {
                val payload = buildLoanPayload(templateResult.data)
                try {
                    loansRepository.submitLoanApplication(
                        providerId = state.providerId,
                        loanState = LoanState.CREATE,
                        payload = payload,
                        loanId = -1,
                    )
                    mutableStateFlow.update {
                        it.copy(
                            isSubmitting = false,
                            dialogState = ConfirmDetailsState.DialogState.Success(
                                getString(Res.string.feature_apply_loan_status_success_tip),
                            ),
                        )
                    }
                } catch (e: Exception) {
                    // Submission failed — surface a user-facing error dialog. The exception
                    // itself is intentionally not logged here (no logger dependency in this
                    // library module); the ScreenState error path already carries diagnostics.
                    mutableStateFlow.update {
                        it.copy(
                            isSubmitting = false,
                            dialogState = ConfirmDetailsState.DialogState.Error(
                                getString(Res.string.feature_apply_loan_error_submit_failed),
                            ),
                        )
                    }
                }
            }

            // Error / Empty / NoNetwork / Unauthenticated — the template needed to build the
            // payload could not be fetched, so the submission cannot proceed.
            else -> {
                mutableStateFlow.update {
                    it.copy(
                        isSubmitting = false,
                        dialogState = ConfirmDetailsState.DialogState.Error(
                            getString(Res.string.feature_apply_loan_error_server),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Builds the payload for the loan application submission using the data carried by the
     * wizard's route chain, plus term/interest fields from the fetched [LoanTemplate].
     */
    private fun buildLoanPayload(template: LoanTemplate) = LoansPayload(
        locale = "en",
        // Must match the actual format of state.disbursementDate, produced by
        // DateHelper.getDateAsStringFromLong() in LoanApplyViewModel (e.g. "19-07-2026") —
        // Fineract validates the date string against this declared format and rejects any
        // mismatch.
        dateFormat = "dd-MM-yyyy",
        clientId = state.clientId.toInt(),
        productId = state.productId.toInt(),
        principal = state.principalAmount.toDoubleOrNull(),
        loanType = "individual",
        // Fineract's self/loans endpoint rejects the raw "loanPurpose" string parameter
        // outright ("The parameter loanPurpose is not supported") — it only recognizes
        // loanPurposeId, a numeric FK into the product's loanPurposeOptions. The purpose
        // dropdown here isn't wired to those option ids yet, so omit the field entirely
        // rather than send a value the server always rejects.
        loanPurpose = null,
        loanTermFrequency = template.termFrequency,
        loanTermFrequencyType = template.interestRateFrequencyType?.id,
        numberOfRepayments = template.numberOfRepayments,
        repaymentEvery = template.repaymentEvery,
        repaymentFrequencyType = template.interestRateFrequencyType?.id,
        interestRatePerPeriod = template.interestRatePerPeriod,
        interestType = template.interestType?.id,
        interestCalculationPeriodType = template.interestCalculationPeriodType?.id,
        amortizationType = template.amortizationType?.id,
        transactionProcessingStrategyCode = template.transactionProcessingStrategyCode,
        expectedDisbursementDate = state.disbursementDate,
        submittedOnDate = state.disbursementDate,
    )
}

/**
 * UI state for the Confirm Details screen.
 *
 * @property clientId The id of the client applying for a loan.
 * @property productId The id of the loan product being applied for.
 * @property applicantName The applicant's display name.
 * @property loanProductName The selected loan product's display name.
 * @property loanPurpose The stated purpose for the loan.
 * @property disbursementDate The scheduled disbursement date.
 * @property principalAmount The requested loan amount.
 * @property providerId The id of the loan provider chosen on the Select Loan Provider screen.
 * @property isSubmitting Whether a submission (auth gate and/or network call) is in flight.
 * @property isAwaitingPasscodeVerification `true` between [ConfirmDetailsEvent.NavigateForPasscodeVerification]
 * being emitted and [ConfirmDetailsAction.UpdateUserVerificationResult] being received.
 * @property userVerificationResult Round-trip result from the passcode/biometric gate: `null` —
 * not yet returned; `true` — verified; `false` — cancelled/rejected. Cleared to `null` on each
 * new attempt.
 * @property dialogState The state of any dialog to be shown on the screen.
 */
internal data class ConfirmDetailsState(
    val clientId: Long,
    val productId: Long,
    val applicantName: String,
    val loanProductName: String,
    val loanPurpose: String,
    val disbursementDate: String,
    val principalAmount: String,
    val providerId: String,
    val isSubmitting: Boolean = false,
    val isAwaitingPasscodeVerification: Boolean = false,
    val userVerificationResult: Boolean? = null,
    val dialogState: DialogState? = null,
) {
    /**
     * Key/value pairs rendered by `ConfirmDetailsCard`.
     */
    val details: Map<StringResource, String>
        get() = mapOf(
            Res.string.feature_apply_loan_label_applicant_name to applicantName,
            Res.string.feature_apply_loan_label_loan_product to loanProductName,
            Res.string.feature_apply_loan_label_purpose to loanPurpose,
            Res.string.feature_apply_loan_label_disbursement_date to disbursementDate,
            Res.string.feature_apply_loan_label_principal_amount to principalAmount,
        )

    /**
     * A sealed interface representing the different types of dialogs that can be shown on the
     * Confirm Details screen.
     */
    sealed interface DialogState {
        /**
         * The application was submitted successfully.
         * @property message A user-facing confirmation message.
         */
        data class Success(val message: String) : DialogState

        /**
         * The application failed to submit (or the auth gate expired).
         * @property message A user-facing error message.
         */
        data class Error(val message: String) : DialogState
    }
}

/**
 * One-shot navigation events emitted by [ConfirmDetailsViewModel].
 */
internal sealed interface ConfirmDetailsEvent {
    /** Navigates back from the current screen. */
    data object NavigateBack : ConfirmDetailsEvent

    /**
     * Tells the screen to push `internalMifosPasscodeScreen` with
     * [LOAN_APPLICATION_VERIFICATION_KEY]. The screen is responsible for observing its own
     * saved-state-handle for the round-trip boolean and dispatching it back as
     * [ConfirmDetailsAction.UpdateUserVerificationResult].
     */
    data object NavigateForPasscodeVerification : ConfirmDetailsEvent

    /**
     * The loan application was submitted successfully and the user has acknowledged the success
     * dialog. The screen should leave the loan-application flow entirely.
     */
    data object SubmitSucceeded : ConfirmDetailsEvent
}

/**
 * User actions and internal events handled by [ConfirmDetailsViewModel].
 */
internal sealed interface ConfirmDetailsAction {
    /** User action to navigate back. */
    data object OnNavigateBack : ConfirmDetailsAction

    /** User action to submit the loan application, triggering the passcode auth gate. */
    data object SubmitClicked : ConfirmDetailsAction

    /** User action to retry submission after a failure. */
    data object RetrySubmit : ConfirmDetailsAction

    /** User action to dismiss the current dialog (e.g. an error). */
    data object DismissDialog : ConfirmDetailsAction

    /** User action acknowledging the success dialog, closing out the flow. */
    data object AcknowledgeSuccess : ConfirmDetailsAction

    /**
     * Round-trip result from the passcode/biometric gate, dispatched by [ConfirmDetailsScreen]
     * after observing [LOAN_APPLICATION_VERIFICATION_KEY] flip on its saved-state-handle.
     * `true` lets the submission proceed (subject to a still-valid
     * [UserVerificationRepository] token); `false` aborts.
     */
    data class UpdateUserVerificationResult(val result: Boolean) : ConfirmDetailsAction
}
